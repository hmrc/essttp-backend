/*
 * Copyright 2022 HM Revenue & Customs
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
import essttp.journey.model.{Journey, JourneyId, SjRequest, SjResponse}
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, Email, IsEmailAddressRequired, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import essttp.utils.RequestSupport._
import uk.gov.hmrc.http.HttpReads.Implicits.{readUnit => _, _}
import essttp.utils.HttpReadsUnitThrowingException.readUnit
import play.api.libs.json.JsNull

@Singleton
class JourneyConnector(httpClient: HttpClient, baseUrl: String)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) {

  def getJourney(journeyId: JourneyId)(implicit request: RequestHeader): Future[Journey] = {
    httpClient.GET[Journey](s"$baseUrl/essttp-backend/journey/${journeyId.value}")
  }

  def findLatestJourneyBySessionId()(implicit hc: HeaderCarrier): Future[Option[Journey]] = {
    for {
      _ <- Future(require(hc.sessionId.isDefined, "Missing required 'SessionId'"))
      result <- httpClient.GET[Option[Journey]](s"$baseUrl/essttp-backend/journey/find-latest-by-session-id")
    } yield result
  }

  def updateTaxId(journeyId: JourneyId, taxId: TaxId)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[TaxId, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-tax-id", taxId)

  def updateEligibilityCheckResult(journeyId: JourneyId, eligibilityCheckResult: EligibilityCheckResult)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[EligibilityCheckResult, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-eligibility-result", eligibilityCheckResult)

  def updateCanPayUpfront(journeyId: JourneyId, canPayUpfront: CanPayUpfront)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[CanPayUpfront, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-can-pay-upfront", canPayUpfront)

  def updateUpfrontPaymentAmount(journeyId: JourneyId, upfrontPaymentAmount: UpfrontPaymentAmount)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[UpfrontPaymentAmount, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-upfront-payment-amount", upfrontPaymentAmount)

  def updateExtremeDates(journeyId: JourneyId, extremeDatesResponse: ExtremeDatesResponse)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[ExtremeDatesResponse, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-extreme-dates", extremeDatesResponse)

  def updateAffordabilityResult(journeyId: JourneyId, instalmentAmounts: InstalmentAmounts)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[InstalmentAmounts, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordability-result", instalmentAmounts)

  def updateMonthlyPaymentAmount(journeyId: JourneyId, monthlyPaymentAmount: MonthlyPaymentAmount)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[MonthlyPaymentAmount, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-monthly-payment-amount", monthlyPaymentAmount)

  def updateDayOfMonth(journeyId: JourneyId, dayOfMonth: DayOfMonth)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[DayOfMonth, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-day-of-month", dayOfMonth)

  def updateStartDates(journeyId: JourneyId, startDatesResponse: StartDatesResponse)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[StartDatesResponse, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-start-dates", startDatesResponse)

  def updateAffordableQuotes(journeyId: JourneyId, affordableQuotesResponse: AffordableQuotesResponse)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[AffordableQuotesResponse, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-affordable-quotes", affordableQuotesResponse)

  def updateChosenPaymentPlan(journeyId: JourneyId, paymentPlan: PaymentPlan)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[PaymentPlan, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-selected-plan", paymentPlan)

  def updateHasCheckedPaymentPlan(journeyId: JourneyId)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[JsNull.type, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-checked-plan", JsNull)

  def updateDetailsAboutBankAccount(journeyId: JourneyId, detailsAboutBankAccount: DetailsAboutBankAccount)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[DetailsAboutBankAccount, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-details-about-bank-account", detailsAboutBankAccount)

  def updateDirectDebitDetails(journeyId: JourneyId, directDebitDetails: BankDetails)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[BankDetails, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-direct-debit-details", directDebitDetails)

  def updateHasConfirmedDirectDebitDetails(journeyId: JourneyId)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[JsNull.type, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-confirmed-direct-debit-details", JsNull)

  def updateHasAgreedTermsAndConditions(journeyId: JourneyId, emailAddressRequired: IsEmailAddressRequired)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[IsEmailAddressRequired, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-has-agreed-terms-and-conditions", emailAddressRequired)

  def updateSelectedEmailToBeVerified(journeyId: JourneyId, email: Email)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[Email, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-chosen-email", email)

  def updateArrangement(journeyId: JourneyId, arrangementResponse: ArrangementResponse)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[ArrangementResponse, Unit](s"$baseUrl/essttp-backend/journey/${journeyId.value}/update-arrangement", arrangementResponse)

  object Epaye {

    def startJourneyBta(sjRequest: SjRequest.Epaye.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest.Epaye.Simple, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/bta/journey/start",
        body = sjRequest
      )

    def startJourneyEPAYE(sjRequest: SjRequest.Epaye.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest.Epaye.Simple, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/epaye/journey/start",
        body = sjRequest
      )

    def startJourneyDetachedUrl(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/detached-url/journey/start",
        body = sjRequest
      )

    def startJourneyGovUk(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/gov-uk/journey/start",
        body = sjRequest
      )
  }

  object Vat {

    def startJourneyBta(sjRequest: SjRequest.Vat.Simple)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest.Vat.Simple, SjResponse](
        url  = s"$baseUrl/essttp-backend/vat/bta/journey/start",
        body = sjRequest
      )

    def startJourneyDetachedUrl(sjRequest: SjRequest.Vat.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/vat/detached-url/journey/start",
        body = sjRequest
      )

    def startJourneyGovUk(sjRequest: SjRequest.Vat.Empty)(implicit request: RequestHeader): Future[SjResponse] =
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/vat/gov-uk/journey/start",
        body = sjRequest
      )
  }

  @Inject()
  def this(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
