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
import essttp.journey.model.{CanPayWithinSixMonthsAnswers, Journey, JourneyId, SjRequest, SjResponse, WhyCannotPayInFullAnswers}
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import essttp.utils.RequestSupport._
import paymentsEmailVerification.models.EmailVerificationResult
import uk.gov.hmrc.http.HttpReads.Implicits.{readUnit => _, _}
import play.api.libs.json.{JsNull, Json}
import uk.gov.hmrc.http.client.HttpClientV2

@Singleton
class JourneyConnector(httpClient: HttpClientV2, baseUrl: String)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) {

  def getJourney(journeyId: JourneyId)(implicit request: RequestHeader): Future[Journey] =
    httpClient.
      get(url"$baseUrl/essttp-backend/journey/${journeyId.value}")
      .execute[Journey]

  def findLatestJourneyBySessionId()(implicit hc: HeaderCarrier): Future[Option[Journey]] = {
    for {
      _ <- Future(require(hc.sessionId.isDefined, "Missing required 'SessionId'"))
      result <- httpClient.get(url"$baseUrl/essttp-backend/journey/find-latest-by-session-id").execute[Option[Journey]]
    } yield result
  }

  def updateTaxId(journeyId: JourneyId, taxId: TaxId)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-tax-id")
      .withBody(Json.toJson(taxId))
      .execute[Journey]

  def updateEligibilityCheckResult(journeyId: JourneyId, eligibilityCheckResult: EligibilityCheckResult)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-eligibility-result")
      .withBody(Json.toJson(eligibilityCheckResult))
      .execute[Journey]

  def updateWhyCannotPayInFullAnswers(journeyId: JourneyId, answers: WhyCannotPayInFullAnswers)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-why-cannot-pay-in-full")
      .withBody(Json.toJson(answers))
      .execute[Journey]

  def updateCanPayUpfront(journeyId: JourneyId, canPayUpfront: CanPayUpfront)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-pay-upfront")
      .withBody(Json.toJson(canPayUpfront))
      .execute[Journey]

  def updateUpfrontPaymentAmount(journeyId: JourneyId, upfrontPaymentAmount: UpfrontPaymentAmount)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-upfront-payment-amount")
      .withBody(Json.toJson(upfrontPaymentAmount))
      .execute[Journey]

  def updateExtremeDates(journeyId: JourneyId, extremeDatesResponse: ExtremeDatesResponse)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-extreme-dates")
      .withBody(Json.toJson(extremeDatesResponse))
      .execute[Journey]

  def updateAffordabilityResult(journeyId: JourneyId, instalmentAmounts: InstalmentAmounts)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordability-result")
      .withBody(Json.toJson(instalmentAmounts))
      .execute[Journey]

  def updateCanPayWithinSixMonthsAnswers(journeyId: JourneyId, answers: CanPayWithinSixMonthsAnswers)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-pay-within-six-months")
      .withBody(Json.toJson(answers))
      .execute[Journey]

  def updatePegaStartCaseResponse(journeyId: JourneyId, response: StartCaseResponse)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-pega-start-case-response")
      .withBody(Json.toJson(response))
      .execute[Journey]

  def updateMonthlyPaymentAmount(journeyId: JourneyId, monthlyPaymentAmount: MonthlyPaymentAmount)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-monthly-payment-amount")
      .withBody(Json.toJson(monthlyPaymentAmount))
      .execute[Journey]

  def updateDayOfMonth(journeyId: JourneyId, dayOfMonth: DayOfMonth)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-day-of-month")
      .withBody(Json.toJson(dayOfMonth))
      .execute[Journey]

  def updateStartDates(journeyId: JourneyId, startDatesResponse: StartDatesResponse)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-start-dates")
      .withBody(Json.toJson(startDatesResponse))
      .execute[Journey]

  def updateAffordableQuotes(journeyId: JourneyId, affordableQuotesResponse: AffordableQuotesResponse)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordable-quotes")
      .withBody(Json.toJson(affordableQuotesResponse)).execute[Journey]

  def updateChosenPaymentPlan(journeyId: JourneyId, paymentPlan: PaymentPlan)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-selected-plan")
      .withBody(Json.toJson(paymentPlan))
      .execute[Journey]

  def updateHasCheckedPaymentPlan(journeyId: JourneyId)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-checked-plan")
      .withBody(Json.toJson(JsNull))
      .execute[Journey]

  def updateDetailsAboutBankAccount(journeyId: JourneyId, detailsAboutBankAccount: DetailsAboutBankAccount)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-details-about-bank-account")
      .withBody(Json.toJson(detailsAboutBankAccount))
      .execute[Journey]

  def updateDirectDebitDetails(journeyId: JourneyId, directDebitDetails: BankDetails)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-direct-debit-details")
      .withBody(Json.toJson(directDebitDetails))
      .execute[Journey]

  def updateHasConfirmedDirectDebitDetails(journeyId: JourneyId)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-confirmed-direct-debit-details")
      .withBody(Json.toJson(JsNull))
      .execute[Journey]

  def updateHasAgreedTermsAndConditions(journeyId: JourneyId, emailAddressRequired: IsEmailAddressRequired)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-agreed-terms-and-conditions")
      .withBody(Json.toJson(emailAddressRequired))
      .execute[Journey]

  def updateSelectedEmailToBeVerified(journeyId: JourneyId, email: Email)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-chosen-email")
      .withBody(Json.toJson(email))
      .execute[Journey]

  def updateEmailVerificationResult(journeyId: JourneyId, status: EmailVerificationResult)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-email-verification-status")
      .withBody(Json.toJson(status))
      .execute[Journey]

  def updateArrangement(journeyId: JourneyId, arrangementResponse: ArrangementResponse)(implicit request: RequestHeader): Future[Journey] =
    httpClient
      .post(url"$baseUrl/essttp-backend/journey/${journeyId.value}/update-arrangement")
      .withBody(Json.toJson(arrangementResponse))
      .execute[Journey]

  object Epaye {

    def startJourneyBta(sjRequest: SjRequest.Epaye.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyEpayeService(sjRequest: SjRequest.Epaye.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/epaye-service/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/epaye/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]
  }

  object Vat {

    def startJourneyBta(sjRequest: SjRequest.Vat.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyVatService(sjRequest: SjRequest.Vat.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/vat-service/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Vat.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Vat.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyVatPenalties(sjRequest: SjRequest.Vat.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/vat/vat-penalties/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

  }

  object Sa {

    def startJourneyBta(sjRequest: SjRequest.Sa.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/bta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyPta(sjRequest: SjRequest.Sa.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/pta/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyMobile(sjRequest: SjRequest.Sa.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/mobile/journey/start")
        .withBody(Json.toJson(sjRequest))
        .execute[SjResponse]

    def startJourneyDetachedUrl(sjRequest: SjRequest.Sa.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/detached-url/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]

    def startJourneyGovUk(sjRequest: SjRequest.Sa.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient
        .post(url"$baseUrl/essttp-backend/sa/gov-uk/journey/start")
        .withBody(Json.toJson(sjRequest: SjRequest))
        .execute[SjResponse]
  }

  @Inject()
  def this(httpClient: HttpClientV2, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
