/*
 * Copyright 2023 HM Revenue & Customs
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

package essttp.journey

import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{CanPayWithinSixMonthsAnswers, Journey, JourneyId, PaymentPlanAnswers, SjRequest, SjResponse, WhyCannotPayInFullAnswers}
import essttp.rootmodel.bank.{BankDetails, CanSetUpDirectDebit, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.pega.StartCaseResponse
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, Email, IsEmailAddressRequired, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import essttp.utils.RequestSupport.hc
import paymentsEmailVerification.models.EmailVerificationResult
import uk.gov.hmrc.http.HttpReads.Implicits.{readUnit as _, *}
import play.api.libs.json.{JsNull, Json}
import uk.gov.hmrc.http.client.HttpClientV2

@Singleton
class JourneyConnector(httpClient: HttpClientV2, baseUrl: String)(implicit
  ec:           ExecutionContext,
  cryptoFormat: OperationalCryptoFormat
) {

  def getJourney(journeyId: JourneyId)(using RequestHeader): Future[Journey] =
    httpClient
      .get(url"$baseUrl/essttp-backend/journey/${journeyId.value}")
      .execute[Journey]

  def findLatestJourneyBySessionId()(using hc: HeaderCarrier): Future[Option[Journey]] =
    for {
      _      <- Future(require(hc.sessionId.isDefined, "Missing required 'SessionId'"))
      result <- httpClient.get(url"$baseUrl/essttp-backend/journey/find-latest-by-session-id").execute[Option[Journey]]
    } yield result

  def updateTaxId(journeyId: JourneyId, taxId: TaxId)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-tax-id")
      .withBody(Json.toJson(taxId))
      .execute[Journey]

  def updateEligibilityCheckResult(journeyId: JourneyId, eligibilityCheckResult: EligibilityCheckResult)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-eligibility-result")
      .withBody(Json.toJson(eligibilityCheckResult))
      .execute[Journey]

  def updateWhyCannotPayInFullAnswers(journeyId: JourneyId, answers: WhyCannotPayInFullAnswers)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-why-cannot-pay-in-full")
      .withBody(Json.toJson(answers))
      .execute[Journey]

  def updateCanPayUpfront(journeyId: JourneyId, canPayUpfront: CanPayUpfront)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-pay-upfront")
      .withBody(Json.toJson(canPayUpfront))
      .execute[Journey]

  def updateUpfrontPaymentAmount(journeyId: JourneyId, upfrontPaymentAmount: UpfrontPaymentAmount)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-upfront-payment-amount")
      .withBody(Json.toJson(upfrontPaymentAmount))
      .execute[Journey]

  def updateExtremeDates(journeyId: JourneyId, extremeDatesResponse: ExtremeDatesResponse)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-extreme-dates")
      .withBody(Json.toJson(extremeDatesResponse))
      .execute[Journey]

  def updateAffordabilityResult(journeyId: JourneyId, instalmentAmounts: InstalmentAmounts)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordability-result")
      .withBody(Json.toJson(instalmentAmounts))
      .execute[Journey]

  def updateCanPayWithinSixMonthsAnswers(journeyId: JourneyId, answers: CanPayWithinSixMonthsAnswers)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-pay-within-six-months")
      .withBody(Json.toJson(answers))
      .execute[Journey]

  def updatePegaStartCaseResponse(journeyId: JourneyId, response: StartCaseResponse)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-pega-start-case-response")
      .withBody(Json.toJson(response))
      .execute[Journey]

  def updateMonthlyPaymentAmount(journeyId: JourneyId, monthlyPaymentAmount: MonthlyPaymentAmount)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-monthly-payment-amount")
      .withBody(Json.toJson(monthlyPaymentAmount))
      .execute[Journey]

  def updateDayOfMonth(journeyId: JourneyId, dayOfMonth: DayOfMonth)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-day-of-month")
      .withBody(Json.toJson(dayOfMonth))
      .execute[Journey]

  def updateStartDates(journeyId: JourneyId, startDatesResponse: StartDatesResponse)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-start-dates")
      .withBody(Json.toJson(startDatesResponse))
      .execute[Journey]

  def updateAffordableQuotes(journeyId: JourneyId, affordableQuotesResponse: AffordableQuotesResponse)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordable-quotes")
      .withBody(Json.toJson(affordableQuotesResponse))
      .execute[Journey]

  def updateChosenPaymentPlan(journeyId: JourneyId, paymentPlan: PaymentPlan)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-selected-plan")
      .withBody(Json.toJson(paymentPlan))
      .execute[Journey]

  def updateHasCheckedPaymentPlan(journeyId: JourneyId, paymentPlanAnswers: PaymentPlanAnswers)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-checked-plan")
      .withBody(Json.toJson(paymentPlanAnswers))
      .execute[Journey]

  def updateCanSetUpDirectDebit(journeyId: JourneyId, canSetUpDirectDebit: CanSetUpDirectDebit)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-set-up-direct-debit")
      .withBody(Json.toJson(canSetUpDirectDebit))
      .execute[Journey]

  def updateTypeOfBankAccount(journeyId: JourneyId, typeofBankAccount: TypeOfBankAccount)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-type-of-bank-account")
      .withBody(Json.toJson(typeofBankAccount))
      .execute[Journey]

  def updateDirectDebitDetails(journeyId: JourneyId, directDebitDetails: BankDetails)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-direct-debit-details")
      .withBody(Json.toJson(directDebitDetails))
      .execute[Journey]

  def updateHasConfirmedDirectDebitDetails(journeyId: JourneyId)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-confirmed-direct-debit-details")
      .withBody(Json.toJson(JsNull))
      .execute[Journey]

  def updateHasAgreedTermsAndConditions(journeyId: JourneyId, emailAddressRequired: IsEmailAddressRequired)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-agreed-terms-and-conditions")
      .withBody(Json.toJson(emailAddressRequired))
      .execute[Journey]

  def updateSelectedEmailToBeVerified(journeyId: JourneyId, email: Email)(using RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-chosen-email")
      .withBody(Json.toJson(email))
      .execute[Journey]

  def updateEmailVerificationResult(journeyId: JourneyId, status: EmailVerificationResult)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-email-verification-status")
      .withBody(Json.toJson(status))
      .execute[Journey]

  def updateArrangement(journeyId: JourneyId, arrangementResponse: ArrangementResponse)(using
    RequestHeader
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-arrangement")
      .withBody(Json.toJson(arrangementResponse))
      .execute[Journey]

  object Epaye {

    def startJourneyBta(sjRequest: SjRequest.Epaye.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyEpayeService(
      sjRequest: SjRequest.Epaye.Simple
    )(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/epaye-service/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Epaye.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Epaye.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]
  }

  object Vat {

    def startJourneyBta(sjRequest: SjRequest.Vat.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyVatService(sjRequest: SjRequest.Vat.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/vat-service/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Vat.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Vat.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyVatPenalties(sjRequest: SjRequest.Vat.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/vat-penalties/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

  }

  object Sa {

    def startJourneyBta(sjRequest: SjRequest.Sa.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyPta(sjRequest: SjRequest.Sa.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/pta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyMobile(sjRequest: SjRequest.Sa.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/mobile/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Sa.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Sa.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyItsaViewAndChange(
      sjRequest: SjRequest.Sa.Simple
    )(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/itsa/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]
  }

  object Simp {

    def startJourneyPta(sjRequest: SjRequest.Simp.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/simp/pta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyMobile(sjRequest: SjRequest.Simp.Simple)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/simp/mobile/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Simp.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/simp/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Simp.Empty)(using RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/simp/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]
  }

  @Inject()
  def this(httpClient: HttpClientV2, servicesConfig: ServicesConfig)(using ExecutionContext, OperationalCryptoFormat) =
    this(
      httpClient,
      servicesConfig.baseUrl("essttp-backend")
    )
}
