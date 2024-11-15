/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import essttp.crypto.CryptoFormat
import essttp.journey.model.{Journey, UpfrontPaymentAnswers, WhyCannotPayInFullAnswers}
import essttp.rootmodel._
import essttp.rootmodel.pega.{GetCaseResponse, StartCaseResponse}
import models.pega.PegaStartCaseResponse
import org.apache.pekko.Done
import org.apache.pekko.stream.Materializer
import play.api.cache.AsyncCacheApi
import play.api.http.Status.CREATED
import play.api.test.Helpers._
import repository.JourneyByTaxIdRepo.JourneyWithTaxId
import repository.{JourneyByTaxIdRepo, JourneyRepo}
import testsupport.ItSpec
import testsupport.stubs.PegaStub
import testsupport.testdata.TdBase
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.{Clock, Instant}

class PegaControllerSpec extends ItSpec with TdBase {

  lazy val journeyByTaxIdRepo = app.injector.instanceOf[JourneyByTaxIdRepo]

  lazy val controller = app.injector.instanceOf[PegaController]

  lazy val cacheApi: AsyncCacheApi = app.injector.instanceOf[AsyncCacheApi]

  implicit lazy val mat: Materializer = app.injector.instanceOf[Materializer]

  implicit lazy val cryptoFormat: CryptoFormat = CryptoFormat.OperationalCryptoFormat(testCrypto)

  override def beforeEach(): Unit = {
    super.beforeEach()
    app.injector.instanceOf[JourneyRepo].collection.drop().toFuture().futureValue shouldBe ()
    journeyByTaxIdRepo.collection.drop().toFuture().futureValue shouldBe ()
    cacheApi.remove("pega-Oauth-token")
    ()
  }

  "PegaController when" - {

    "handling requests to start a case must" - {

        def expectedEpayeStartCaseRequestJson(unableToPayReasonsCodes: Seq[String] = Seq("UTPR-3")) =
          s"""
             |{
             |   "caseTypeID": "HMRC-Debt-Work-AffordAssess",
             |   "content":{
             |      "uniqueIdentifier": "864FZ00049",
             |      "uniqueIdentifierType": "EMPREF",
             |      "regime": "PAYE",
             |      "AA": {
             |         "debtAmount": 300000,
             |         "makeUpFrontPayment": true,
             |         "unableToPayReasons":[
             |           ${unableToPayReasonsCodes.map(c => s"""{ "reason": "$c" }""").mkString(", ")}
             |         ],
             |         "mdtpPropertyMapping": {
             |            "customerPostcodes": [
             |               {
             |                  "postcodeDate": "2020-01-01",
             |                  "addressPostcode": "AA11AA"
             |               }
             |            ],
             |            "initialPaymentDate": "2022-01-01",
             |            "channelIdentifier": "eSSTTP",
             |            "debtItemCharges" : [ {
             |              "outstandingDebtAmount" : 100000,
             |              "mainTrans" : "mainTrans",
             |              "subTrans" : "subTrans",
             |              "debtItemChargeId" : "A00000000001",
             |              "interestStartDate" : "2022-05-17",
             |              "debtItemOriginalDueDate" : "2022-05-17"
             |            } ],
             |            "accruedDebtInterest": 1597,
             |            "initialPaymentAmount": 1000,
             |            "paymentPlanFrequency": "Monthly"
             |         }
             |      }
             |   }
             |}
             |""".stripMargin

      "return an error when" - {

          def testException(context: JourneyItTest)(
              expectedErrorMessage: String
          ) = {
            stubCommonActions()

            val exception = intercept[Exception]{
              controller.startCase(context.tdAll.journeyId, recalculationNeeded = true)(context.request).futureValue
            }

            exception.getMessage should include(expectedErrorMessage)
          }

        "no journey can be found for the given journey id" in new JourneyItTest {
          testException(this)("Expected journey to be found")
        }

        "no tax id can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStarted)

          testException(this)("Could not find tax id")
        }

        "no eligibility check result can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterDetermineTaxIds)

          testException(this)("Could not find eligibility check result")
        }

        "the user was not required to answer why they could not pay in full" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired)

          testException(this)("Expected WhyCannotPayInFull reasons but answer was not required")
        }

        "no 'can you pay within 6 months' answers can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterEligibilityCheckEligible)

          testException(this)("Could not find why cannot pay in full answers")
        }

        "no upfront payment amount can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterWhyCannotPayInFullRequired)

          testException(this)("Could not find upfront payment answers")
        }

        "no 'why cannot pay in full' answers can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeDetachedUrl.journeyAfterStarted)

          testException(this)("Could not find tax id")
        }

        "the user was not required to answer if they could pay within 6 months" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeDetachedUrl.journeyAfterWhyCannotPayInFullNotRequired)

          testException(this)("Expected WhyCannotPayInFull reasons but answer was not required")
        }

        "there is an error getting an oauth token" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Left(503))

          testException(this)("returned 503")
        }

        "there is an error calling the start case API (not 401)" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Left(502))

          testException(this)("returned 502")
        }

        "there are two consecutive 401 errors when calling the start case API, when the initial token has expired" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Left(401))

          testException(this)("returned 401")

          PegaStub.verifyOauthCalled("user", "pass", 2)
          PegaStub.verifyStartCaseCalled(tdAll.pegaOauthToken, expectedEpayeStartCaseRequestJson(), 2)
        }

        "there are two consecutive 401 errors when calling the start case API, when there is already a PEGA token in the cache" in new JourneyItTest {
          cacheApi.set("pega-Oauth-token", "tokenInCache").futureValue shouldBe Done

          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Left(401))

          testException(this)("returned 401")

          PegaStub.verifyOauthCalled("user", "pass")
          PegaStub.verifyStartCaseCalled(tdAll.pegaOauthToken, expectedEpayeStartCaseRequestJson())
        }

        "no assignment ID can be found in the start case response" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Right(
            PegaStartCaseResponse(
              "id",
              PegaStartCaseResponse.Data(PegaStartCaseResponse.CaseInfo(List.empty))
            )
          ))

          testException(this)("Could not find assignment ID in PEGA start case response")
        }

      }

      "submit the correct payload and return the correct id's when" - {

          def testSuccess(context: JourneyItTest)(journey: Journey, expectedRequestJson: String) = {
            context.insertJourneyForTest(journey)
            stubCommonActions()
            PegaStub.stubOauthToken(Right(context.tdAll.pegaOauthToken))
            PegaStub.stubStartCase(Right(context.tdAll.pegaStartCaseResponse))

            val result = controller.startCase(context.tdAll.journeyId, recalculationNeeded = true)(context.request)
            status(result) shouldBe CREATED
            contentAsJson(result).as[StartCaseResponse] shouldBe context.tdAll.startCaseResponse

            PegaStub.verifyOauthCalled("user", "pass")
            PegaStub.verifyStartCaseCalled(
              context.tdAll.pegaOauthToken,
              expectedRequestJson
            )
          }

        "the start case API returns a 401 initially and is successfully retried after refreshing the oauth token" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Right(tdAll.pegaStartCaseResponse), expiredToken = true)

          testSuccess(this)(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo.copy(
              whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
            ),
            expectedEpayeStartCaseRequestJson()
          )

          PegaStub.verifyOauthCalled("user", "pass")
          PegaStub.verifyStartCaseCalled(tdAll.pegaOauthToken, expectedEpayeStartCaseRequestJson())
        }

        "the tax regime is" - {

          "EPAYE" in new JourneyItTest {
            testSuccess(this)(
              tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              expectedEpayeStartCaseRequestJson()
            )
          }

          "VAT" in new JourneyItTest {
            testSuccess(this)(
              tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              s"""
                 |{
                 |   "caseTypeID": "HMRC-Debt-Work-AffordAssess",
                 |   "content":{
                 |      "uniqueIdentifier": "101747001",
                 |      "uniqueIdentifierType": "VRN",
                 |      "regime": "VAT",
                 |      "AA":{
                 |         "debtAmount": 300000,
                 |         "makeUpFrontPayment": true,
                 |         "unableToPayReasons" : [
                 |           { "reason": "UTPR-3" }
                 |         ],
                 |         "mdtpPropertyMapping":{
                 |            "customerPostcodes":[
                 |               {
                 |                  "postcodeDate": "2020-01-01",
                 |                  "addressPostcode": "AA11AA"
                 |               }
                 |            ],
                 |            "initialPaymentDate":"2022-01-01",
                 |            "channelIdentifier":"eSSTTP",
                 |            "debtItemCharges" : [ {
                 |              "outstandingDebtAmount" : 100000,
                 |              "mainTrans" : "mainTrans",
                 |              "subTrans" : "subTrans",
                 |              "debtItemChargeId" : "A00000000001",
                 |              "interestStartDate" : "2022-05-17",
                 |              "debtItemOriginalDueDate" : "2022-05-17"
                 |            } ],
                 |            "accruedDebtInterest": 1597,
                 |            "initialPaymentAmount": 1000,
                 |            "paymentPlanFrequency": "Monthly"
                 |         }
                 |      }
                 |   }
                 |}
                 |""".stripMargin
            )
          }

          "SA" in new JourneyItTest {
            testSuccess(this)(
              tdAll.SaBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              s"""
                 |{
                 |   "caseTypeID": "HMRC-Debt-Work-AffordAssess",
                 |   "content":{
                 |      "uniqueIdentifier": "1234567895",
                 |      "uniqueIdentifierType": "SAUTR",
                 |      "regime": "SA",
                 |      "AA":{
                 |         "debtAmount": 300000,
                 |         "makeUpFrontPayment": true,
                 |         "unableToPayReasons" : [
                 |           { "reason": "UTPR-3" }
                 |         ],
                 |         "mdtpPropertyMapping":{
                 |            "customerPostcodes":[
                 |               {
                 |                  "postcodeDate": "2020-01-01",
                 |                  "addressPostcode": "AA11AA"
                 |               }
                 |            ],
                 |            "initialPaymentDate":"2022-01-01",
                 |            "channelIdentifier":"eSSTTP",
                 |            "debtItemCharges" : [ {
                 |              "outstandingDebtAmount" : 100000,
                 |              "mainTrans" : "mainTrans",
                 |              "subTrans" : "subTrans",
                 |              "debtItemChargeId" : "A00000000001",
                 |              "interestStartDate" : "2022-05-17",
                 |              "debtItemOriginalDueDate" : "2022-05-17"
                 |            } ],
                 |            "accruedDebtInterest": 1597,
                 |            "initialPaymentAmount": 1000,
                 |            "paymentPlanFrequency": "Monthly"
                 |         }
                 |      }
                 |   }
                 |}
                 |""".stripMargin
            )
          }

        }

        "the user has decided not to do an upfront payment" in new JourneyItTest {
          testSuccess(this)(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo.copy(
              whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
              upfrontPaymentAnswers     = UpfrontPaymentAnswers.NoUpfrontPayment,
              extremeDatesResponse      = tdAll.extremeDatesWithoutUpfrontPayment
            ),
            s"""
               |{
               |   "caseTypeID": "HMRC-Debt-Work-AffordAssess",
               |   "content":{
               |      "uniqueIdentifier": "864FZ00049",
               |      "uniqueIdentifierType": "EMPREF",
               |      "regime": "PAYE",
               |      "AA":{
               |         "debtAmount": 300000,
               |         "makeUpFrontPayment": false,
               |         "unableToPayReasons" : [
               |           { "reason": "UTPR-3" }
               |         ],
               |         "mdtpPropertyMapping":{
               |            "customerPostcodes":[
               |               {
               |                  "postcodeDate": "2020-01-01",
               |                  "addressPostcode": "AA11AA"
               |               }
               |            ],
               |            "channelIdentifier":"eSSTTP",
               |            "debtItemCharges" : [ {
               |              "outstandingDebtAmount" : 100000,
               |              "mainTrans" : "mainTrans",
               |              "subTrans" : "subTrans",
               |              "debtItemChargeId" : "A00000000001",
               |              "interestStartDate" : "2022-05-17",
               |              "debtItemOriginalDueDate" : "2022-05-17"
               |            } ],
               |            "accruedDebtInterest": 1597,
               |            "paymentPlanFrequency": "Monthly"
               |         }
               |      }
               |   }
               |}
               |""".stripMargin
          )
        }

        "mapping to the correct unable to pay reasons" in new JourneyItTest {
          List(
            (CannotPayReason.UnexpectedReductionOfIncome, "UTPR-1"),
            (CannotPayReason.UnexpectedIncreaseInSpending, "UTPR-2"),
            (CannotPayReason.LostOrReducedAbilityToEarnOrTrade, "UTPR-3"),
            (CannotPayReason.NationalOrLocalDisaster, "UTPR-4"),
            (CannotPayReason.ChangeToPersonalCircumstances, "UTPR-5"),
            (CannotPayReason.NoMoneySetAside, "UTPR-6"),
            (CannotPayReason.WaitingForRefund, "UTPR-7"),
            (CannotPayReason.Other, "UTPR-8")
          ).foreach{
              case (cannotPayReason, expectedCode) =>
                withClue(s"For ${cannotPayReason.toString}") {
                  testSuccess(this)(
                    tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.WhyCannotPayInFull(
                        Set(cannotPayReason)
                      )
                    ),
                    expectedEpayeStartCaseRequestJson(Seq(expectedCode))
                  )
                }
            }
        }

      }

    }

    "handling requests to get a case must" - {

      "return an error when" - {

          def testException(context: JourneyItTest)(
              expectedErrorMessage: String
          ) = {
            stubCommonActions()

            val exception = intercept[Exception]{
              controller.getCase(context.tdAll.journeyId)(context.request).futureValue
            }

            exception.getMessage should include(expectedErrorMessage)
          }

        "no journey can be found for the given journey id" in new JourneyItTest {
          testException(this)("Expected journey to be found")
        }

        "a PEGA case has not been started yet" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo)

          testException(this)("Could not find PEGA case id in journey in state essttp.journey.model.Journey.Epaye.ObtainedCanPayWithinSixMonthsAnswers")
        }

        "the user has checked their payment plan but they are on a non-affordability joureny" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)

          testException(this)("Trying to find case ID on non-affordability journey")
        }

        "there is an error getting an oauth token" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
          PegaStub.stubOauthToken(Left(503))

          testException(this)("returned 503")
        }

        "there is an error calling the get case API (not 401)" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubGetCase(tdAll.pegaCaseId, Left(502))

          testException(this)("returned 502")
        }

        "there are two consecutive 401 errors when calling the get case API, when the initial token has expired" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterStartedPegaCase
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubGetCase(pegaCaseId, Left(401))

          testException(this)("returned 401")

          PegaStub.verifyOauthCalled("user", "pass", 2)
          PegaStub.verifyGetCaseCalled(tdAll.pegaOauthToken, pegaCaseId, 2)
        }

        "there are two consecutive 401 errors when calling the get case API, when there is already a PEGA token in the cache" in new JourneyItTest {
          cacheApi.set("pega-Oauth-token", "tokenInCache").futureValue shouldBe Done

          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterStartedPegaCase
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubGetCase(pegaCaseId, Left(401))

          testException(this)("returned 401")

          PegaStub.verifyOauthCalled("user", "pass")
          PegaStub.verifyGetCaseCalled(tdAll.pegaOauthToken, pegaCaseId)
        }

      }

      "return the payment plan when one is returned by PEGA" in new JourneyItTest {
        val paymentPlan = tdAll.paymentPlan(2)

        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()
        PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
        PegaStub.stubGetCase(tdAll.pegaCaseId, Right(tdAll.pegaGetCaseResponse(tdAll.dayOfMonth, paymentPlan)))

        val result = controller.getCase(tdAll.journeyId)(request)
        status(result) shouldBe OK
        contentAsJson(result).as[GetCaseResponse] shouldBe GetCaseResponse(tdAll.dayOfMonth, paymentPlan)

        PegaStub.verifyOauthCalled("user", "pass")
        PegaStub.verifyGetCaseCalled(
          tdAll.pegaOauthToken,
          tdAll.pegaCaseId
        )

      }

      "returns the payment plan when the GetCase call initially fails and is successfully retried after " +
        "refreshing the oauth token " in new JourneyItTest {
          val paymentPlan = tdAll.paymentPlan(2)

          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
          stubCommonActions()
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubGetCase(tdAll.pegaCaseId, Right(tdAll.pegaGetCaseResponse(tdAll.dayOfMonth, paymentPlan)), expiredToken = true)

          val result = controller.getCase(tdAll.journeyId)(request)
          status(result) shouldBe OK
          contentAsJson(result).as[GetCaseResponse] shouldBe GetCaseResponse(tdAll.dayOfMonth, paymentPlan)

          PegaStub.verifyOauthCalled("user", "pass", 2)
          PegaStub.verifyGetCaseCalled(
            tdAll.pegaOauthToken,
            tdAll.pegaCaseId,
            2
          )

        }

    }

    "handling a request to save a journey must" - {

      "return an error when" - {
        "no journey can be found for the given journey id" in new JourneyItTest {
          stubCommonActions()

          val exception = intercept[Exception](await(controller.saveJourney(tdAll.journeyId)(request)))
          exception.getMessage should include("Expected journey to be found")

        }

        "no tax id has been determined yet in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStarted)
          stubCommonActions()

          val exception = intercept[Exception](await(controller.saveJourney(tdAll.journeyId)(request)))
          exception.getMessage should include("Cannot save journey when no tax ID has been computed yet")
        }
      }

      "save a journey if one can be found in an appropriate state" in new JourneyItTest {
        val journey = tdAll.EpayeBta.journeyAfterCanPayUpfrontNo
        insertJourneyForTest(journey)
        stubCommonActions()

        val result = controller.saveJourney(tdAll.journeyId)(request)
        status(result) shouldBe OK

        val document = journeyByTaxIdRepo.findById(journey.taxId).futureValue
        document.map(_.journey) shouldBe Some(journey)
        document.map(_.taxId) shouldBe Some(journey.taxId)
      }

    }

    "handling a request to recreate a session for tax regime" - {

      "EPAYE must" - {

        "return an error when" - {

            def testError(enrolments: Set[Enrolment], expectedResponseStatus: Int, expectedResponseMessage: String)(context: JourneyItTest) = {
              stubCommonActions(Enrolments(enrolments))

              val exception = intercept[UpstreamErrorResponse](
                await(controller.recreateSession(TaxRegime.Epaye)(context.request))
              )
              exception.statusCode shouldBe expectedResponseStatus
              exception.getMessage shouldBe expectedResponseMessage
            }

          "no IR-PAYE enrolment can be found" in new JourneyItTest {
            testError(Set.empty, FORBIDDEN, "No enrolment found for tax regime Epaye: EnrolmentNotFound()")(this)
          }

          "no TaxOfficeNumber can be found" in new JourneyItTest {
            testError(
              Set(Enrolment("IR-PAYE", Seq(EnrolmentIdentifier("TaxOfficeReference", "t")), "activated")),
              FORBIDDEN,
              "No enrolment found for tax regime Epaye: IdentifierNotFound(Set(EnrolmentDef(IR-PAYE,TaxOfficeNumber)))"
            )(this)
          }

          "no TaxOfficeReference can be found" in new JourneyItTest {
            testError(
              Set(Enrolment("IR-PAYE", Seq(EnrolmentIdentifier("TaxOfficeNumber", "t")), "activated")),
              FORBIDDEN,
              "No enrolment found for tax regime Epaye: IdentifierNotFound(Set(EnrolmentDef(IR-PAYE,TaxOfficeReference)))"
            )(this)
          }

          "the IR-PAYE enrolment is inactive" in new JourneyItTest {
            testError(
              Set(
                Enrolment(
                  "IR-PAYE",
                  Seq(EnrolmentIdentifier("TaxOfficeNumber", "t"), EnrolmentIdentifier("TaxOfficeReference", "t")),
                  "something that's not activated"
                )
              ),
              FORBIDDEN,
              "No enrolment found for tax regime Epaye: Inactive()"
            )(this)
          }

          "no journey can be found for the tax id found from the enrolments" in new JourneyItTest {
            testError(
              Set(
                Enrolment(
                  "IR-PAYE",
                  Seq(EnrolmentIdentifier("TaxOfficeNumber", "12345"), EnrolmentIdentifier("TaxOfficeReference", "67890")),
                  "Activated"
                )
              ),
              NOT_FOUND,
              "Journey not found for tax regime Epaye"
            )(this)
          }

        }

        "save the journey in the JourneyRepo if one can be found and return it in the response" in new JourneyItTest {
          val journey = tdAll.EpayeBta.journeyAfterStartedPegaCase.copy(taxId = EmpRef("1234567890"))
          await(journeyByTaxIdRepo.upsert(JourneyWithTaxId(journey.taxId, journey, Instant.now(Clock.systemUTC())))) shouldBe ()

          stubCommonActions(Enrolments(
            Set(
              Enrolment(
                "IR-PAYE",
                Seq(EnrolmentIdentifier("TaxOfficeNumber", "12345"), EnrolmentIdentifier("TaxOfficeReference", "67890")),
                "Activated"
              )
            )
          ))

          val result = controller.recreateSession(TaxRegime.Epaye)(request)
          status(result) shouldBe OK
          contentAsJson(result).validate[Journey].get shouldBe journey

          journeyRepo.findById(tdAll.journeyId).futureValue shouldBe Some(journey)
        }

      }

      "VAT must" - {

        val mtdVatEnrolment = Enrolment(
          "HMRC-MTD-VAT",
          Seq(EnrolmentIdentifier("VRN", "12345678")),
          "Activated"
        )

        val vatVarEnrolment = Enrolment(
          "HMCE-VATVAR-ORG",
          Seq(EnrolmentIdentifier("VATRegNo", "23456789")),
          "Activated"
        )

        val vatDecEnrolment = Enrolment(
          "HMCE-VATDEC-ORG",
          Seq(EnrolmentIdentifier("VATRegNo", "34567890")),
          "Activated"
        )

        "return an error when" - {

            def testError(enrolments: Set[Enrolment], expectedResponseStatus: Int, expectedResponseMessage: String)(context: JourneyItTest) = {
              stubCommonActions(Enrolments(enrolments))

              val exception = intercept[UpstreamErrorResponse](
                await(controller.recreateSession(TaxRegime.Vat)(context.request))
              )
              exception.statusCode shouldBe expectedResponseStatus
              exception.getMessage shouldBe expectedResponseMessage
            }

          "no relevant enrolment can be found" in new JourneyItTest {
            testError(Set.empty, FORBIDDEN, "No enrolment found for tax regime Vat: EnrolmentNotFound()")(this)
          }

          "an identifier for HMRC-MTD-VAT cannot be found" in new JourneyItTest {
            testError(
              Set(mtdVatEnrolment.copy(identifiers = Seq.empty)),
              FORBIDDEN,
              "No enrolment found for tax regime Vat: IdentifierNotFound(Set(EnrolmentDef(HMRC-MTD-VAT,VRN)))"
            )(this)
          }

          "an identifier for HMCE-VATDEC-ORG cannot be found" in new JourneyItTest {
            testError(
              Set(vatDecEnrolment.copy(identifiers = Seq.empty)),
              FORBIDDEN,
              "No enrolment found for tax regime Vat: IdentifierNotFound(Set(EnrolmentDef(HMCE-VATDEC-ORG,VATRegNo)))"
            )(this)
          }

          "an identifier for HMCE-VATVAR-ORG cannot be found" in new JourneyItTest {
            testError(
              Set(vatVarEnrolment.copy(identifiers = Seq.empty)),
              FORBIDDEN,
              "No enrolment found for tax regime Vat: IdentifierNotFound(Set(EnrolmentDef(HMCE-VATVAR-ORG,VATRegNo)))"
            )(this)
          }

          "no active enrolment can be found" in new JourneyItTest {
            testError(
              Set(
                vatVarEnrolment.copy(state = "uh oh"),
                vatDecEnrolment.copy(state = "oh no"),
                mtdVatEnrolment.copy(state = "aahhh")
              ),
              FORBIDDEN,
              "No enrolment found for tax regime Vat: Inactive()"
            )(this)
          }

          "no journey can be found for the tax id found from the enrolments" in new JourneyItTest {
            testError(Set(mtdVatEnrolment), NOT_FOUND, "Journey not found for tax regime Vat")(this)
          }

        }

        "save the journey in the JourneyRepo if one can be found and return it in the response when" - {
            def test(enrolments: Set[Enrolment], expectedVrn: String)(context: JourneyItTest): Unit = {
              val journey = context.tdAll.VatBta.journeyAfterStartedPegaCase.copy(taxId = Vrn(expectedVrn))
              await(journeyByTaxIdRepo.upsert(JourneyWithTaxId(journey.taxId, journey, Instant.now(Clock.systemUTC())))) shouldBe ()

              stubCommonActions(Enrolments(enrolments))

              val result = controller.recreateSession(TaxRegime.Vat)(context.request)
              status(result) shouldBe OK
              contentAsJson(result).validate[Journey].get shouldBe journey

              context.journeyRepo.findById(context.tdAll.journeyId).futureValue shouldBe Some(journey)
              ()
            }

          "there are active MtdVat, VatVar and VatDec enrolments" in new JourneyItTest {
            test(
              Set(mtdVatEnrolment, vatVarEnrolment, vatDecEnrolment),
              "12345678"
            )(this)
          }

          "there are active MtdVat, VatVar enrolments" in new JourneyItTest {
            test(
              Set(mtdVatEnrolment, vatVarEnrolment),
              "12345678"
            )(this)
          }

          "there are active MtdVat, VatDec enrolments" in new JourneyItTest {
            test(
              Set(mtdVatEnrolment, vatDecEnrolment),
              "12345678"
            )(this)
          }

          "there are active VatVar and VatDec enrolments" in new JourneyItTest {
            test(
              Set(vatVarEnrolment, vatDecEnrolment),
              "23456789"
            )(this)
          }

          "there is only an active MtdVat enrolment" in new JourneyItTest {
            test(
              Set(mtdVatEnrolment),
              "12345678"
            )(this)
          }

          "there is only an active VatVar enrolment" in new JourneyItTest {
            test(
              Set(vatVarEnrolment),
              "23456789"
            )(this)
          }

          "there is only an active VatDec enrolment" in new JourneyItTest {
            test(
              Set(vatDecEnrolment),
              "34567890"
            )(this)
          }

        }

      }

      "SA must" - {

        val saEnrolment = Enrolment(
          "IR-SA",
          Seq(EnrolmentIdentifier("UTR", "1234567895")),
          "Activated"
        )

        "return an error when" - {

            def testError(enrolments: Set[Enrolment], expectedResponseStatus: Int, expectedResponseMessage: String)(context: JourneyItTest) = {
              stubCommonActions(Enrolments(enrolments))

              val exception = intercept[UpstreamErrorResponse](
                await(controller.recreateSession(TaxRegime.Sa)(context.request))
              )
              exception.statusCode shouldBe expectedResponseStatus
              exception.getMessage shouldBe expectedResponseMessage
            }

          "no IR-SA enrolment can be found" in new JourneyItTest {
            testError(Set.empty, FORBIDDEN, "No enrolment found for tax regime Sa: EnrolmentNotFound()")(this)
          }

          "no UTR can be found" in new JourneyItTest {
            testError(
              Set(saEnrolment.copy(identifiers = Seq.empty)),
              FORBIDDEN,
              "No enrolment found for tax regime Sa: IdentifierNotFound(Set(EnrolmentDef(IR-SA,UTR)))"
            )(this)
          }

          "the IR-SA enrolment is inactive" in new JourneyItTest {
            testError(
              Set(saEnrolment.copy(state = "broken")),
              FORBIDDEN,
              "No enrolment found for tax regime Sa: Inactive()"
            )(this)
          }

          "no journey can be found for the tax id found from the enrolments" in new JourneyItTest {
            testError(
              Set(saEnrolment),
              NOT_FOUND,
              "Journey not found for tax regime Sa"
            )(this)
          }
        }

        "save the journey in the JourneyRepo if one can be found and return it in the response" in new JourneyItTest {
          val journey = tdAll.SaBta.journeyAfterStartedPegaCase.copy(
            taxId     = SaUtr("1234567895"),
            sessionId = SessionId("old-session-id")
          )
          await(journeyByTaxIdRepo.upsert(JourneyWithTaxId(journey.taxId, journey, Instant.now(Clock.systemUTC())))) shouldBe ()

          stubCommonActions(Enrolments(Set(saEnrolment)))

          val result = controller.recreateSession(TaxRegime.Sa)(request)
          status(result) shouldBe OK
          contentAsJson(result).validate[Journey].get shouldBe journey.copy(sessionId = tdAll.sessionId)

          journeyRepo.findById(tdAll.journeyId).futureValue shouldBe Some(journey.copy(sessionId = tdAll.sessionId))
        }

      }

    }

  }

}
