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

package essttp.journey.model

import essttp.crypto.CryptoFormat
import essttp.journey.model.SjRequest.{Epaye, Sa, Simp, Vat}
import essttp.rootmodel.*
import essttp.rootmodel.bank.{BankDetails, CanSetUpDirectDebit, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.pega.{PegaCaseId, StartCaseResponse}
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.utils.DerivedJson
import essttp.utils.DerivedJson.Circe.formatToCodec
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.libs.json.{JsObject, JsValue, Json, OFormat, OWrites}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}

sealed trait JourneyStage

@SuppressWarnings(Array("org.wartremover.warts.Sealed"))
object JourneyStage {

  sealed trait BeforeComputedTaxId extends JourneyStage

  sealed trait AfterComputedTaxId extends JourneyStage {
    def taxId: TaxId
  }

  sealed trait BeforeEligibilityChecked extends JourneyStage

  sealed trait AfterEligibilityChecked extends JourneyStage {
    def eligibilityCheckResult: EligibilityCheckResult
  }

  sealed trait BeforeWhyCannotPayInFullAnswers extends JourneyStage

  sealed trait AfterWhyCannotPayInFullAnswers extends JourneyStage {
    def whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers
  }

  sealed trait BeforeAnsweredCanPayUpfront extends JourneyStage

  sealed trait AfterAnsweredCanPayUpfront extends JourneyStage {
    def canPayUpfront: CanPayUpfront
  }

  sealed trait BeforeEnteredUpfrontPaymentAmount extends JourneyStage

  sealed trait AfterEnteredUpfrontPaymentAmount extends JourneyStage {
    def upfrontPaymentAmount: UpfrontPaymentAmount
  }

  sealed trait BeforeUpfrontPaymentAnswers extends JourneyStage

  sealed trait AfterUpfrontPaymentAnswers extends JourneyStage {
    def upfrontPaymentAnswers: UpfrontPaymentAnswers
  }

  sealed trait BeforeExtremeDatesResponse extends JourneyStage

  sealed trait AfterExtremeDatesResponse extends JourneyStage {
    def extremeDatesResponse: ExtremeDatesResponse
  }

  sealed trait BeforeRetrievedAffordabilityResult extends JourneyStage

  sealed trait AfterRetrievedAffordabilityResult extends JourneyStage {
    def instalmentAmounts: InstalmentAmounts
  }

  sealed trait BeforeCanPayWithinSixMonthsAnswers extends JourneyStage

  sealed trait AfterCanPayWithinSixMonthsAnswers extends JourneyStage {
    def canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers
  }

  sealed trait BeforeStartedPegaCase extends JourneyStage

  sealed trait AfterStartedPegaCase extends JourneyStage {
    def startCaseResponse: StartCaseResponse
  }

  sealed trait BeforeEnteredMonthlyPaymentAmount extends JourneyStage

  sealed trait AfterEnteredMonthlyPaymentAmount extends JourneyStage {
    def monthlyPaymentAmount: MonthlyPaymentAmount
  }

  sealed trait BeforeEnteredDayOfMonth extends JourneyStage

  sealed trait AfterEnteredDayOfMonth extends JourneyStage {
    def dayOfMonth: DayOfMonth
  }

  sealed trait BeforeStartDatesResponse extends JourneyStage

  sealed trait AfterStartDatesResponse extends JourneyStage {
    def startDatesResponse: StartDatesResponse
  }

  sealed trait BeforeAffordableQuotesResponse extends JourneyStage

  sealed trait AfterAffordableQuotesResponse extends JourneyStage {
    def affordableQuotesResponse: AffordableQuotesResponse
  }

  sealed trait BeforeSelectedPaymentPlan extends JourneyStage

  sealed trait AfterSelectedPaymentPlan extends JourneyStage {
    def selectedPaymentPlan: PaymentPlan
  }

  sealed trait BeforeCheckedPaymentPlan extends JourneyStage

  sealed trait AfterCheckedPaymentPlan extends JourneyStage {
    def paymentPlanAnswers: PaymentPlanAnswers
  }

  sealed trait BeforeEnteredCanYouSetUpDirectDebit extends JourneyStage

  sealed trait AfterEnteredCanYouSetUpDirectDebit extends JourneyStage {
    def canSetUpDirectDebitAnswer: CanSetUpDirectDebit
  }

  sealed trait BeforeChosenTypeOfBankAccount extends JourneyStage

  sealed trait AfterChosenTypeOfBankAccount extends JourneyStage {
    def typeOfBankAccount: TypeOfBankAccount
  }

  sealed trait BeforeEnteredDirectDebitDetails extends JourneyStage

  sealed trait AfterEnteredDirectDebitDetails extends JourneyStage {
    def directDebitDetails: BankDetails
  }

  sealed trait BeforeConfirmedDirectDebitDetails extends JourneyStage

  sealed trait AfterConfirmedDirectDebitDetails extends JourneyStage

  sealed trait BeforeAgreedTermsAndConditions extends JourneyStage

  sealed trait AfterAgreedTermsAndConditions extends JourneyStage {
    def isEmailAddressRequired: IsEmailAddressRequired
  }

  sealed trait BeforeEmailAddressSelectedToBeVerified extends JourneyStage

  sealed trait AfterEmailAddressSelectedToBeVerified extends JourneyStage {
    def emailToBeVerified: Email
  }

  sealed trait BeforeEmailAddressVerificationResult extends JourneyStage

  sealed trait AfterEmailAddressVerificationResult extends JourneyStage {
    def emailVerificationResult: EmailVerificationResult
  }

  sealed trait BeforeEmailVerificationPhase extends JourneyStage

  sealed trait AfterEmailVerificationPhase extends JourneyStage {
    def emailVerificationAnswers: EmailVerificationAnswers
  }

  sealed trait BeforeArrangementSubmitted extends JourneyStage

  sealed trait AfterArrangementSubmitted extends JourneyStage {
    def arrangementResponse: ArrangementResponse
  }

}

sealed trait Journey derives CanEqual { this: JourneyStage =>
  def _id: JourneyId
  def origin: Origin
  def createdOn: Instant

  def sjRequest: SjRequest
  def sessionId: SessionId
  def taxRegime: TaxRegime = origin.taxRegime
  def correlationId: CorrelationId
  def affordabilityEnabled: Option[Boolean]
  def pegaCaseId: Option[PegaCaseId]
  def redirectToLegacySaService: Option[Boolean]

  /* derived stuff: */

  def id: JourneyId        = _id
  def journeyId: JourneyId = _id
  val traceId: TraceId     = TraceId.fromJourneyId(journeyId)

  def name: String = {
    val className   = getClass.getName
    val packageName = getClass.getPackage.getName
    className
      .replaceAll(s"\\$packageName.", "")
      .replaceAll("\\$", ".")
  }

  val (backUrl, returnUrl): (Option[BackUrl], Option[ReturnUrl]) = sjRequest match
    case Epaye.Simple(returnUrl, backUrl) => Some(backUrl) -> Some(returnUrl)
    case Epaye.Empty()                    => None          -> None
    case Vat.Simple(returnUrl, backUrl)   => Some(backUrl) -> Some(returnUrl)
    case Vat.Empty()                      => None          -> None
    case Sa.Simple(returnUrl, backUrl)    => Some(backUrl) -> Some(returnUrl)
    case Sa.Empty()                       => None          -> None
    case Simp.Simple(returnUrl, backUrl)  => Some(backUrl) -> Some(returnUrl)
    case Simp.Empty()                     => None          -> None

}

object Journey {

  import JourneyStage._

  extension (j: Journey) {

    def json(implicit cryptoFormat: CryptoFormat): JsValue = Json.toJson(j)

    def stage: String = j.getClass.getSimpleName

  }

  /** [[Journey]] after started
    */
  final case class Started(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        BeforeComputedTaxId,
        BeforeEligibilityChecked,
        BeforeWhyCannotPayInFullAnswers,
        BeforeAnsweredCanPayUpfront,
        BeforeEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after computed TaxIds
    */
  final case class ComputedTaxId(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        BeforeWhyCannotPayInFullAnswers,
        BeforeEligibilityChecked,
        BeforeAnsweredCanPayUpfront,
        BeforeEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after EligibilityCheck
    */
  final case class EligibilityChecked(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        BeforeWhyCannotPayInFullAnswers,
        BeforeAnsweredCanPayUpfront,
        BeforeEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after WhyCannotPayInFullAnswers
    */
  final case class ObtainedWhyCannotPayInFullAnswers(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        BeforeAnsweredCanPayUpfront,
        BeforeEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after CanPayUpfront
    */
  final case class AnsweredCanPayUpfront(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
    override val canPayUpfront:             CanPayUpfront,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterAnsweredCanPayUpfront,
        BeforeEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after UpfrontPaymentAmount
    */
  final case class EnteredUpfrontPaymentAmount(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
    override val canPayUpfront:             CanPayUpfront,
    override val upfrontPaymentAmount:      UpfrontPaymentAmount,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterAnsweredCanPayUpfront,
        AfterEnteredUpfrontPaymentAmount,
        BeforeUpfrontPaymentAnswers,
        BeforeExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeStartedPegaCase,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Extreme dates request to esstp-dates
    */
  final case class RetrievedExtremeDates(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
    override val extremeDatesResponse:      ExtremeDatesResponse,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        BeforeRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Affordability request to tpp
    */
  final case class RetrievedAffordabilityResult(
    override val _id:                       JourneyId,
    override val origin:                    Origin,
    override val createdOn:                 Instant,
    override val sjRequest:                 SjRequest,
    override val sessionId:                 SessionId,
    override val correlationId:             CorrelationId,
    override val affordabilityEnabled:      Option[Boolean],
    override val taxId:                     TaxId,
    override val eligibilityCheckResult:    EligibilityCheckResult,
    override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
    override val extremeDatesResponse:      ExtremeDatesResponse,
    override val instalmentAmounts:         InstalmentAmounts,
    override val pegaCaseId:                Option[PegaCaseId],
    override val redirectToLegacySaService: Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        BeforeCanPayWithinSixMonthsAnswers,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeStartedPegaCase,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after answers to CanPayWithinSixMonths if needed
    */
  final case class ObtainedCanPayWithinSixMonthsAnswers(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        BeforeEnteredMonthlyPaymentAmount,
        BeforeStartedPegaCase,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after started a PEGA case
    */
  final case class StartedPegaCase(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val startCaseResponse:            StartCaseResponse,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterStartedPegaCase,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after MonthlyPaymentAmount
    */
  final case class EnteredMonthlyPaymentAmount(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val monthlyPaymentAmount:         MonthlyPaymentAmount,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterEnteredMonthlyPaymentAmount,
        BeforeEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Day of month
    */
  final case class EnteredDayOfMonth(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val monthlyPaymentAmount:         MonthlyPaymentAmount,
    override val dayOfMonth:                   DayOfMonth,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterEnteredMonthlyPaymentAmount,
        AfterEnteredDayOfMonth,
        BeforeStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Start dates api call
    */
  final case class RetrievedStartDates(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val monthlyPaymentAmount:         MonthlyPaymentAmount,
    override val dayOfMonth:                   DayOfMonth,
    override val startDatesResponse:           StartDatesResponse,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterEnteredMonthlyPaymentAmount,
        AfterEnteredDayOfMonth,
        AfterStartDatesResponse,
        BeforeAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Affordable quotes call to ttp
    */
  final case class RetrievedAffordableQuotes(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val monthlyPaymentAmount:         MonthlyPaymentAmount,
    override val dayOfMonth:                   DayOfMonth,
    override val startDatesResponse:           StartDatesResponse,
    override val affordableQuotesResponse:     AffordableQuotesResponse,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterEnteredMonthlyPaymentAmount,
        AfterEnteredDayOfMonth,
        AfterStartDatesResponse,
        AfterAffordableQuotesResponse,
        BeforeSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Payment plan has been chosen
    */
  final case class ChosenPaymentPlan(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val monthlyPaymentAmount:         MonthlyPaymentAmount,
    override val dayOfMonth:                   DayOfMonth,
    override val startDatesResponse:           StartDatesResponse,
    override val affordableQuotesResponse:     AffordableQuotesResponse,
    override val selectedPaymentPlan:          PaymentPlan,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterEnteredMonthlyPaymentAmount,
        AfterEnteredDayOfMonth,
        AfterStartDatesResponse,
        AfterAffordableQuotesResponse,
        AfterSelectedPaymentPlan,
        BeforeCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Payment plan has been checked
    */
  final case class CheckedPaymentPlan(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        BeforeEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after details about bank account
    */
  final case class EnteredCanYouSetUpDirectDebit(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        BeforeChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after type of bank account has been chosen
    */
  final case class ChosenTypeOfBankAccount(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        BeforeEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after bank details have been entered
    */
  final case class EnteredDirectDebitDetails(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        AfterEnteredDirectDebitDetails,
        BeforeConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after bank details have been confirmed
    */
  final case class ConfirmedDirectDebitDetails(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        AfterEnteredDirectDebitDetails,
        AfterConfirmedDirectDebitDetails,
        BeforeAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Agreeing terms and conditions
    */
  final case class AgreedTermsAndConditions(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val isEmailAddressRequired:       IsEmailAddressRequired,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        AfterEnteredDirectDebitDetails,
        AfterConfirmedDirectDebitDetails,
        AfterAgreedTermsAndConditions,
        BeforeEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Selecting email address to be verified
    */
  final case class SelectedEmailToBeVerified(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val isEmailAddressRequired:       IsEmailAddressRequired,
    override val emailToBeVerified:            Email,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        AfterEnteredDirectDebitDetails,
        AfterConfirmedDirectDebitDetails,
        AfterAgreedTermsAndConditions,
        AfterEmailAddressSelectedToBeVerified,
        BeforeEmailAddressVerificationResult,
        BeforeEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after email verification status journey is complete
    */
  final case class EmailVerificationComplete(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val isEmailAddressRequired:       IsEmailAddressRequired,
    override val emailToBeVerified:            Email,
    override val emailVerificationResult:      EmailVerificationResult,
    override val emailVerificationAnswers:     EmailVerificationAnswers,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterChosenTypeOfBankAccount,
        AfterEnteredDirectDebitDetails,
        AfterConfirmedDirectDebitDetails,
        AfterAgreedTermsAndConditions,
        AfterEmailAddressSelectedToBeVerified,
        AfterEmailAddressVerificationResult,
        AfterEmailVerificationPhase,
        BeforeArrangementSubmitted

  /** [[Journey]] after Submission of their arrangement to the enact api
    */
  final case class SubmittedArrangement(
    override val _id:                          JourneyId,
    override val origin:                       Origin,
    override val createdOn:                    Instant,
    override val sjRequest:                    SjRequest,
    override val sessionId:                    SessionId,
    override val correlationId:                CorrelationId,
    override val affordabilityEnabled:         Option[Boolean],
    override val taxId:                        TaxId,
    override val eligibilityCheckResult:       EligibilityCheckResult,
    override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
    override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
    override val extremeDatesResponse:         ExtremeDatesResponse,
    override val instalmentAmounts:            InstalmentAmounts,
    override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
    override val paymentPlanAnswers:           PaymentPlanAnswers,
    override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
    override val typeOfBankAccount:            TypeOfBankAccount,
    override val directDebitDetails:           BankDetails,
    override val isEmailAddressRequired:       IsEmailAddressRequired,
    override val arrangementResponse:          ArrangementResponse,
    override val emailVerificationAnswers:     EmailVerificationAnswers,
    override val pegaCaseId:                   Option[PegaCaseId],
    override val redirectToLegacySaService:    Option[Boolean]
  ) extends Journey,
        AfterComputedTaxId,
        AfterEligibilityChecked,
        AfterWhyCannotPayInFullAnswers,
        AfterUpfrontPaymentAnswers,
        AfterExtremeDatesResponse,
        AfterRetrievedAffordabilityResult,
        AfterCanPayWithinSixMonthsAnswers,
        AfterCheckedPaymentPlan,
        AfterEnteredCanYouSetUpDirectDebit,
        AfterEnteredDirectDebitDetails,
        AfterConfirmedDirectDebitDetails,
        AfterChosenTypeOfBankAccount,
        AfterAgreedTermsAndConditions,
        AfterEmailVerificationPhase,
        AfterArrangementSubmitted

  given format(using cryptoFormat: CryptoFormat): OFormat[Journey] = {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    val defaultFormat: OFormat[Journey] = DerivedJson.Circe.format(deriveCodec[Journey])

    // we need to write some extra fields on the top of the structure so it's
    // possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId"     -> j.sessionId,
        "createdAt"     -> MongoJavatimeFormats.instantFormat.writes(j.createdOn),
        "lastUpdated"   -> MongoJavatimeFormats.instantFormat.writes(Instant.now(Clock.systemUTC())),
        "correlationId" -> j.correlationId
      )
    )
    OFormat(
      defaultFormat.preprocess { case j: JsObject =>
        j - "sessionId" - "createdAt" - "lastUpdated" - "correlationId" - "_id"
      },
      customWrites
    )
  }
}
