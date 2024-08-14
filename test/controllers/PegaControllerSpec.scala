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

import essttp.journey.model.{Journey, UpfrontPaymentAnswers}
import essttp.rootmodel.pega.{GetCaseResponse, StartCaseResponse}
import models.pega.{PegaGetCaseResponse, PegaStartCaseResponse}
import org.apache.pekko.stream.Materializer
import play.api.http.Status.CREATED
import play.api.test.Helpers._
import repository.JourneyRepo
import testsupport.ItSpec
import testsupport.stubs.PegaStub

class PegaControllerSpec extends ItSpec {

  lazy val journeyRepo = app.injector.instanceOf[JourneyRepo]

  lazy val controller = app.injector.instanceOf[PegaController]

  implicit lazy val mat: Materializer = app.injector.instanceOf[Materializer]

  override def beforeEach(): Unit = {
    super.beforeEach()
    journeyRepo.collection.drop().toFuture().futureValue shouldBe ()
    ()
  }

  "PegaController when" - {

    "handling requests to start a case must" - {

      "return an error when" - {

          def testException(context: JourneyItTest)(
              expectedErrorMessage: String
          ) = {
            stubCommonActions()

            val exception = intercept[Exception]{
              controller.startCase(context.tdAll.journeyId)(context.request).futureValue
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

        "there is an error calling the start case API" in new JourneyItTest {
          insertJourneyForTest(
            tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo
              .copy(whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)
          )
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubStartCase(Left(502))

          testException(this)("returned 502")
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

            val result = controller.startCase(context.tdAll.journeyId)(context.request)
            status(result) shouldBe CREATED
            contentAsJson(result).as[StartCaseResponse] shouldBe context.tdAll.startCaseResponse

            PegaStub.verifyOauthCalled("user", "pass")
            PegaStub.verifyStartCaseCalled(
              context.tdAll.pegaOauthToken,
              expectedRequestJson
            )
          }

        "the tax regime is" - {

          "EPAYE" in new JourneyItTest {
            testSuccess(this)(
              tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              """{
                |  "caseTypeID" : "HMRC-Debt-Work-AffordAssess",
                |  "processID" : "",
                |  "parentCaseID" : "",
                |  "content" : {
                |    "UniqueIdentifier" : "864FZ00049",
                |    "UniqueIdentifierType" : "EMPREF",
                |    "Regime" : "PAYE",
                |    "DebtAmount" : 300000,
                |    "MDTPPropertyMapping" : {
                |      "customerPostcodes" : [ {
                |        "addressPostcode" : "AA11AA",
                |        "postcodeDate" : "2020-01-01"
                |      } ],
                |      "initialPaymentDate" : "2022-01-01",
                |      "channelIdentifier" : "eSSTTP",
                |      "debtItemCharges" : [ {
                |        "outstandingDebtAmount" : 100000,
                |        "mainTrans" : "mainTrans",
                |        "subTrans" : "subTrans",
                |        "debtItemChargeId" : "A00000000001",
                |        "interestStartDate" : "2022-05-17",
                |        "debtItemOriginalDueDate" : "2022-05-17"
                |      } ],
                |      "accruedInterest" : 1597,
                |      "initialPaymentAmount" : 1000,
                |      "paymentPlanFrequency" : "Monthly",
                |      "UnableToPayReason" : [ "Bankrupt" ],
                |      "MakeUpfrontPayment" : true,
                |      "CanDebtBePaidIn6Months" : false
                |    }
                |  }
                |}""".stripMargin
            )
          }

          "VAT" in new JourneyItTest {
            testSuccess(this)(
              tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              """{
                |  "caseTypeID" : "HMRC-Debt-Work-AffordAssess",
                |  "processID" : "",
                |  "parentCaseID" : "",
                |  "content" : {
                |    "UniqueIdentifier" : "101747001",
                |    "UniqueIdentifierType" : "VRN",
                |    "Regime" : "VAT",
                |    "DebtAmount" : 300000,
                |    "MDTPPropertyMapping" : {
                |      "customerPostcodes" : [ {
                |        "addressPostcode" : "AA11AA",
                |        "postcodeDate" : "2020-01-01"
                |      } ],
                |      "initialPaymentDate" : "2022-01-01",
                |      "channelIdentifier" : "eSSTTP",
                |      "debtItemCharges" : [ {
                |        "outstandingDebtAmount" : 100000,
                |        "mainTrans" : "mainTrans",
                |        "subTrans" : "subTrans",
                |        "debtItemChargeId" : "A00000000001",
                |        "interestStartDate" : "2022-05-17",
                |        "debtItemOriginalDueDate" : "2022-05-17"
                |      } ],
                |      "accruedInterest" : 1597,
                |      "initialPaymentAmount" : 1000,
                |      "paymentPlanFrequency" : "Monthly",
                |      "UnableToPayReason" : [ "Bankrupt" ],
                |      "MakeUpfrontPayment" : true,
                |      "CanDebtBePaidIn6Months" : false
                |    }
                |  }
                |}""".stripMargin
            )
          }

          "SA" in new JourneyItTest {
            testSuccess(this)(
              tdAll.SaBta.journeyAfterCanPayWithinSixMonthsNo.copy(
                whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired
              ),
              """{
                |  "caseTypeID" : "HMRC-Debt-Work-AffordAssess",
                |  "processID" : "",
                |  "parentCaseID" : "",
                |  "content" : {
                |    "UniqueIdentifier" : "1234567895",
                |    "UniqueIdentifierType" : "SAUTR",
                |    "Regime" : "SA",
                |    "DebtAmount" : 300000,
                |    "MDTPPropertyMapping" : {
                |      "customerPostcodes" : [ {
                |        "addressPostcode" : "AA11AA",
                |        "postcodeDate" : "2020-01-01"
                |      } ],
                |      "initialPaymentDate" : "2022-01-01",
                |      "channelIdentifier" : "eSSTTP",
                |      "debtItemCharges" : [ {
                |        "outstandingDebtAmount" : 100000,
                |        "mainTrans" : "mainTrans",
                |        "subTrans" : "subTrans",
                |        "debtItemChargeId" : "A00000000001",
                |        "interestStartDate" : "2022-05-17",
                |        "debtItemOriginalDueDate" : "2022-05-17"
                |      } ],
                |      "accruedInterest" : 1597,
                |      "initialPaymentAmount" : 1000,
                |      "paymentPlanFrequency" : "Monthly",
                |      "UnableToPayReason" : [ "Bankrupt" ],
                |      "MakeUpfrontPayment" : true,
                |      "CanDebtBePaidIn6Months" : false
                |    }
                |  }
                |}""".stripMargin
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
            """{
              |  "caseTypeID" : "HMRC-Debt-Work-AffordAssess",
              |  "processID" : "",
              |  "parentCaseID" : "",
              |  "content" : {
              |    "UniqueIdentifier" : "864FZ00049",
              |    "UniqueIdentifierType" : "EMPREF",
              |    "Regime" : "PAYE",
              |    "DebtAmount" : 300000,
              |    "MDTPPropertyMapping" : {
              |      "customerPostcodes" : [ {
              |        "addressPostcode" : "AA11AA",
              |        "postcodeDate" : "2020-01-01"
              |      } ],
              |      "channelIdentifier" : "eSSTTP",
              |      "debtItemCharges" : [ {
              |        "outstandingDebtAmount" : 100000,
              |        "mainTrans" : "mainTrans",
              |        "subTrans" : "subTrans",
              |        "debtItemChargeId" : "A00000000001",
              |        "interestStartDate" : "2022-05-17",
              |        "debtItemOriginalDueDate" : "2022-05-17"
              |      } ],
              |      "accruedInterest" : 1597,
              |      "paymentPlanFrequency" : "Monthly",
              |      "UnableToPayReason" : [ "Bankrupt" ],
              |      "MakeUpfrontPayment" : false,
              |      "CanDebtBePaidIn6Months" : false
              |    }
              |  }
              |}""".stripMargin
          )
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

        "no case id can be found in the journey" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNo)

          testException(this)("Could not find PEGA case id in journey in state essttp.journey.model.Journey.Epaye.ObtainedCanPayWithinSixMonthsAnswers")
        }

        "there is an error getting an oauth token" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
          PegaStub.stubOauthToken(Left(503))

          testException(this)("returned 503")
        }

        "there is an error calling the get case API" in new JourneyItTest {
          insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
          PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
          PegaStub.stubGetCase(tdAll.pegaCaseId, Left(502))

          testException(this)("returned 502")
        }

      }

      "return the payment plan when one is returned by PEGA" in new JourneyItTest {
        val paymentPlan = tdAll.paymentPlan(2)

        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()
        PegaStub.stubOauthToken(Right(tdAll.pegaOauthToken))
        PegaStub.stubGetCase(tdAll.pegaCaseId, Right(PegaGetCaseResponse(paymentPlan)))

        val result = controller.getCase(tdAll.journeyId)(request)
        status(result) shouldBe OK
        contentAsJson(result).as[GetCaseResponse] shouldBe GetCaseResponse(paymentPlan)

        PegaStub.verifyOauthCalled("user", "pass")
        PegaStub.verifyGetCaseCalled(
          tdAll.pegaOauthToken,
          tdAll.pegaCaseId
        )

      }

    }

  }

}
