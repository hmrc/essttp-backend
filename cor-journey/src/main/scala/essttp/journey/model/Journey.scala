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
import essttp.rootmodel.*
import essttp.rootmodel.bank.{BankDetails, CanSetUpDirectDebit}
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
import play.api.libs.json.{JsValue, Json, OFormat, OWrites}
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

  sealed trait AfterUpfrontPaymentAnswers extends JourneyStage  {
    def upfrontPaymentAnswers: UpfrontPaymentAnswers
  }

  sealed trait BeforeExtremeDatesResponse extends JourneyStage 

  sealed trait AfterExtremeDatesResponse extends JourneyStage  {
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

  sealed trait AfterEmailAddressSelectedToBeVerified extends JourneyStage  {
    def emailToBeVerified: Email
  }

  sealed trait BeforeEmailAddressVerificationResult extends JourneyStage 

  sealed trait AfterEmailAddressVerificationResult extends JourneyStage  {
    def emailVerificationResult: EmailVerificationResult
  }

  sealed trait BeforeEmailVerificationPhase extends JourneyStage 

  sealed trait AfterEmailVerificationPhase extends JourneyStage  {
    def emailVerificationAnswers: EmailVerificationAnswers
  }

  sealed trait BeforeArrangementSubmitted extends JourneyStage 

  sealed trait AfterArrangementSubmitted extends JourneyStage {
    def arrangementResponse: ArrangementResponse
  }

}

sealed trait JourneyStageView { this: JourneyStage => }

object JourneyStageView {

  import JourneyStage._

  sealed trait Started
    extends JourneyStageView
      with BeforeComputedTaxId
      with BeforeEligibilityChecked
      with BeforeWhyCannotPayInFullAnswers
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeCanPayWithinSixMonthsAnswers
      with BeforeStartedPegaCase
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredCanYouSetUpDirectDebit
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted

  sealed trait ComputedTaxId
    extends JourneyStageView
      with AfterComputedTaxId
      with BeforeWhyCannotPayInFullAnswers
      with BeforeEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeCanPayWithinSixMonthsAnswers
      with BeforeStartedPegaCase
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredCanYouSetUpDirectDebit
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted

  sealed trait EligibilityChecked
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with BeforeWhyCannotPayInFullAnswers
        with BeforeAnsweredCanPayUpfront
        with BeforeEnteredUpfrontPaymentAmount
        with BeforeUpfrontPaymentAnswers
        with BeforeExtremeDatesResponse
        with BeforeRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeStartedPegaCase
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait ObtainedWhyCannotPayInFullAnswers
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with BeforeAnsweredCanPayUpfront
        with BeforeEnteredUpfrontPaymentAmount
        with BeforeUpfrontPaymentAnswers
        with BeforeExtremeDatesResponse
        with BeforeRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeStartedPegaCase
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait AnsweredCanPayUpfront
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterAnsweredCanPayUpfront
        with BeforeEnteredUpfrontPaymentAmount
        with BeforeUpfrontPaymentAnswers
        with BeforeExtremeDatesResponse
        with BeforeRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeStartedPegaCase
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EnteredUpfrontPaymentAmount
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterAnsweredCanPayUpfront
        with AfterEnteredUpfrontPaymentAmount
        with BeforeUpfrontPaymentAnswers
        with BeforeExtremeDatesResponse
        with BeforeRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeStartedPegaCase
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait RetrievedExtremeDates
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with BeforeRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait RetrievedAffordabilityResult
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with BeforeCanPayWithinSixMonthsAnswers
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeStartedPegaCase
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait ObtainedCanPayWithinSixMonthsAnswers
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with BeforeEnteredMonthlyPaymentAmount
        with BeforeStartedPegaCase
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait StartedPegaCase
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterStartedPegaCase
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EnteredMonthlyPaymentAmount
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterEnteredMonthlyPaymentAmount
        with BeforeEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EnteredDayOfMonth
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterEnteredMonthlyPaymentAmount
        with AfterEnteredDayOfMonth
        with BeforeStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait RetrievedStartDates
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterEnteredMonthlyPaymentAmount
        with AfterEnteredDayOfMonth
        with AfterStartDatesResponse
        with BeforeAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait RetrievedAffordableQuotes
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterEnteredMonthlyPaymentAmount
        with AfterEnteredDayOfMonth
        with AfterStartDatesResponse
        with AfterAffordableQuotesResponse
        with BeforeSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait ChosenPaymentPlan
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterEnteredMonthlyPaymentAmount
        with AfterEnteredDayOfMonth
        with AfterStartDatesResponse
        with AfterAffordableQuotesResponse
        with AfterSelectedPaymentPlan
        with BeforeCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait CheckedPaymentPlan
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with BeforeEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EnteredCanYouSetUpDirectDebit
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with BeforeEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EnteredDirectDebitDetails
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with BeforeConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait ConfirmedDirectDebitDetails
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with AfterConfirmedDirectDebitDetails
        with BeforeAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait AgreedTermsAndConditions
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with AfterConfirmedDirectDebitDetails
        with AfterAgreedTermsAndConditions
        with BeforeEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait SelectedEmailToBeVerified
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with AfterConfirmedDirectDebitDetails
        with AfterAgreedTermsAndConditions
        with AfterEmailAddressSelectedToBeVerified
        with BeforeEmailAddressVerificationResult
        with BeforeEmailVerificationPhase
        with BeforeArrangementSubmitted

  sealed trait EmailVerificationComplete
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with AfterConfirmedDirectDebitDetails
        with AfterAgreedTermsAndConditions
        with AfterEmailAddressSelectedToBeVerified
        with AfterEmailAddressVerificationResult
        with AfterEmailVerificationPhase
        with BeforeArrangementSubmitted
  
  sealed trait SubmittedArrangement
      extends JourneyStageView
        with AfterComputedTaxId
        with AfterEligibilityChecked
        with AfterWhyCannotPayInFullAnswers
        with AfterUpfrontPaymentAnswers
        with AfterExtremeDatesResponse
        with AfterRetrievedAffordabilityResult
        with AfterCanPayWithinSixMonthsAnswers
        with AfterCheckedPaymentPlan
        with AfterEnteredCanYouSetUpDirectDebit
        with AfterEnteredDirectDebitDetails
        with AfterConfirmedDirectDebitDetails
        with AfterAgreedTermsAndConditions
        with AfterEmailVerificationPhase
        with AfterArrangementSubmitted

}

@SuppressWarnings(Array("org.wartremover.warts.Sealed"))
sealed trait JourneyRegime {
  def taxRegime: TaxRegime

  def sjRequest: SjRequest

  def origin: Origin

  def backUrl: Option[BackUrl]

  def returnUrl: Option[ReturnUrl]
}

object JourneyRegime {

  /** Marking sealed trait for extracting Epaye [[Journey]]s
    */
  sealed trait Epaye extends JourneyRegime {
    override def taxRegime: TaxRegime.Epaye.type = TaxRegime.Epaye

    override def sjRequest: SjRequest.Epaye

    override def origin: Origins.Epaye

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Epaye.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                         => (None, None)
    }
  }

  /** Marking sealed trait for extracting Vat [[Journey]]s
    */
  sealed trait Vat extends JourneyRegime {
    override def taxRegime: TaxRegime.Vat.type = TaxRegime.Vat

    override def sjRequest: SjRequest.Vat

    override def origin: Origins.Vat

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Vat.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                       => (None, None)
    }
  }

  /** Marking sealed trait for extracting Sa [[Journey]]s
    */
  sealed trait Sa extends JourneyRegime {
    override def taxRegime: TaxRegime.Sa.type = TaxRegime.Sa

    override def sjRequest: SjRequest.Sa

    override def origin: Origins.Sa

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Sa.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                      => (None, None)
    }
  }

  /** Marking sealed trait for extracting Simp [[Journey]]s
    */
  sealed trait Simp extends JourneyRegime {
    override def taxRegime: TaxRegime.Simp.type = TaxRegime.Simp

    override def sjRequest: SjRequest.Simp

    override def origin: Origins.Simp

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Simp.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                        => (None, None)
    }
  }

}

sealed trait Journey { this: JourneyStageView with JourneyRegime =>
  def _id: JourneyId
  def origin: Origin
  def createdOn: Instant

  def sjRequest: SjRequest
  def sessionId: SessionId
  def taxRegime: TaxRegime
  def correlationId: CorrelationId
  def affordabilityEnabled: Option[Boolean]
  def pegaCaseId: Option[PegaCaseId]

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

  def backUrl: Option[BackUrl]
  def returnUrl: Option[ReturnUrl]

}

object Journey {

  implicit class JourneyOps(private val j: Journey) extends AnyVal {

    def json(implicit cryptoFormat: CryptoFormat): JsValue = Json.toJson(j)

    def stage: String = j match {
      case stage: JourneyStageView => stage.getClass.getSimpleName
    }

  }

  object Epaye {

    /** [[Journey]] after started Epaye
      */
    final case class Started(
      override val _id:                  JourneyId,
      override val origin:               Origins.Epaye,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Epaye,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.Started
        with JourneyRegime.Epaye

    /** [[Journey]] after computed TaxIds Epaye
      */
    final case class ComputedTaxId(
      override val _id:                  JourneyId,
      override val origin:               Origins.Epaye,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Epaye,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val taxId:                EmpRef,
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ComputedTaxId
        with JourneyRegime.Epaye

    /** [[Journey]] after EligibilityCheck Epaye
      */
    final case class EligibilityChecked(
      override val _id:                    JourneyId,
      override val origin:                 Origins.Epaye,
      override val createdOn:              Instant,
      override val sjRequest:              SjRequest.Epaye,
      override val sessionId:              SessionId,
      override val correlationId:          CorrelationId,
      override val affordabilityEnabled:   Option[Boolean],
      override val taxId:                  EmpRef,
      override val eligibilityCheckResult: EligibilityCheckResult,
      override val pegaCaseId:             Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EligibilityChecked
        with JourneyRegime.Epaye

    /** [[Journey]] after WhyCannotPayInFullAnswers Epaye
      */
    final case class ObtainedWhyCannotPayInFullAnswers(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Epaye,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Epaye,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     EmpRef,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedWhyCannotPayInFullAnswers
        with JourneyRegime.Epaye

    /** [[Journey]] after CanPayUpfront Epaye
      */
    final case class AnsweredCanPayUpfront(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Epaye,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Epaye,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     EmpRef,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AnsweredCanPayUpfront
        with JourneyRegime.Epaye

    /** [[Journey]] after UpfrontPaymentAmount Epaye
      */
    final case class EnteredUpfrontPaymentAmount(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Epaye,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Epaye,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     EmpRef,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val upfrontPaymentAmount:      UpfrontPaymentAmount,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredUpfrontPaymentAmount
        with JourneyRegime.Epaye

    /** [[Journey]] after Extreme dates request to esstp-dates Epaye
      */
    final case class RetrievedExtremeDates(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Epaye,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Epaye,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     EmpRef,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedExtremeDates
        with JourneyRegime.Epaye

    /** [[Journey]] after Affordability request to tpp Epaye
      */
    final case class RetrievedAffordabilityResult(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Epaye,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Epaye,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     EmpRef,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val instalmentAmounts:         InstalmentAmounts,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordabilityResult
        with JourneyRegime.Epaye

    /** [[Journey]] after answers to CanPayWithinSixMonths if needed Epaye
      */
    final case class ObtainedCanPayWithinSixMonthsAnswers(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedCanPayWithinSixMonthsAnswers
        with JourneyRegime.Epaye

    /** [[Journey]] after started a PEGA case Epaye
      */
    final case class StartedPegaCase(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val startCaseResponse:            StartCaseResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.StartedPegaCase
        with JourneyRegime.Epaye

    /** [[Journey]] after MonthlyPaymentAmount Epaye
      */
    final case class EnteredMonthlyPaymentAmount(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredMonthlyPaymentAmount
        with JourneyRegime.Epaye

    /** [[Journey]] after Day of month Epaye
      */
    final case class EnteredDayOfMonth(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDayOfMonth
        with JourneyRegime.Epaye

    /** [[Journey]] after Start dates api call Epaye
      */
    final case class RetrievedStartDates(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val startDatesResponse:           StartDatesResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedStartDates
        with JourneyRegime.Epaye

    /** [[Journey]] after Affordable quotes call to ttp Epaye
      */
    final case class RetrievedAffordableQuotes(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordableQuotes
        with JourneyRegime.Epaye

    /** [[Journey]] after Payment plan has been chosen Epaye
      */
    final case class ChosenPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ChosenPaymentPlan
        with JourneyRegime.Epaye

    /** [[Journey]] after Payment plan has been checked Epaye
      */
    final case class CheckedPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.CheckedPaymentPlan
        with JourneyRegime.Epaye

    /** [[Journey]] after details about bank account Epaye
      */
    final case class EnteredCanYouSetUpDirectDebit(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredCanYouSetUpDirectDebit
        with JourneyRegime.Epaye

    /** [[Journey]] after bank details have been entered Epaye
      */
    final case class EnteredDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDirectDebitDetails
        with JourneyRegime.Epaye

    /** [[Journey]] after bank details have been confirmed Epaye
      */
    final case class ConfirmedDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ConfirmedDirectDebitDetails
        with JourneyRegime.Epaye

    /** [[Journey]] after Agreeing terms and conditions Epaye
      */
    final case class AgreedTermsAndConditions(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AgreedTermsAndConditions
        with JourneyRegime.Epaye

    /** [[Journey]] after Selecting email address to be verified Epaye
      */
    final case class SelectedEmailToBeVerified(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SelectedEmailToBeVerified
        with JourneyRegime.Epaye

    /** [[Journey]] after email verification status journey is complete Epaye
      */
    final case class EmailVerificationComplete(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val emailVerificationResult:      EmailVerificationResult,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EmailVerificationComplete
        with JourneyRegime.Epaye

    /** [[Journey]] after Submission of their arrangement to the enact api Epaye
      */
    final case class SubmittedArrangement(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Epaye,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Epaye,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        EmpRef,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val arrangementResponse:          ArrangementResponse,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SubmittedArrangement
        with JourneyRegime.Epaye

  }

  object Vat {

    /** [[Journey]] after started VAT
      */
    final case class Started(
      override val _id:                  JourneyId,
      override val origin:               Origins.Vat,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Vat,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.Started
        with JourneyRegime.Vat

    /** [[Journey]] after computed TaxIds VAT
      */
    final case class ComputedTaxId(
      override val _id:                  JourneyId,
      override val origin:               Origins.Vat,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Vat,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val taxId:                Vrn,
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ComputedTaxId
        with JourneyRegime.Vat

    /** [[Journey]] after EligibilityCheck VAT
      */
    final case class EligibilityChecked(
      override val _id:                    JourneyId,
      override val origin:                 Origins.Vat,
      override val createdOn:              Instant,
      override val sjRequest:              SjRequest.Vat,
      override val sessionId:              SessionId,
      override val correlationId:          CorrelationId,
      override val affordabilityEnabled:   Option[Boolean],
      override val taxId:                  Vrn,
      override val eligibilityCheckResult: EligibilityCheckResult,
      override val pegaCaseId:             Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EligibilityChecked
        with JourneyRegime.Vat

    /** [[Journey]] after WhyCannotPayInFullAnswers Vat
      */
    final case class ObtainedWhyCannotPayInFullAnswers(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Vat,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Vat,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Vrn,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedWhyCannotPayInFullAnswers
        with JourneyRegime.Vat

    /** [[Journey]] after CanPayUpfront Vat
      */
    final case class AnsweredCanPayUpfront(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Vat,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Vat,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Vrn,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AnsweredCanPayUpfront
        with JourneyRegime.Vat

    /** [[Journey]] after UpfrontPaymentAmount Vat
      */
    final case class EnteredUpfrontPaymentAmount(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Vat,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Vat,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Vrn,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val upfrontPaymentAmount:      UpfrontPaymentAmount,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredUpfrontPaymentAmount
        with JourneyRegime.Vat

    /** [[Journey]] after Extreme dates request to esstp-dates Vat
      */
    final case class RetrievedExtremeDates(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Vat,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Vat,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Vrn,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedExtremeDates
        with JourneyRegime.Vat

    /** [[Journey]] after Affordability request to tpp Vat
      */
    final case class RetrievedAffordabilityResult(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Vat,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Vat,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Vrn,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val instalmentAmounts:         InstalmentAmounts,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordabilityResult
        with JourneyRegime.Vat

    /** [[Journey]] after answers to CanPayWithinSixMonths if needed Vat
      */
    final case class ObtainedCanPayWithinSixMonthsAnswers(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedCanPayWithinSixMonthsAnswers
        with JourneyRegime.Vat

    /** [[Journey]] after started a PEGA case Vat
      */
    final case class StartedPegaCase(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val startCaseResponse:            StartCaseResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.StartedPegaCase
        with JourneyRegime.Vat

    /** [[Journey]] after MonthlyPaymentAmount Vat
      */
    final case class EnteredMonthlyPaymentAmount(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredMonthlyPaymentAmount
        with JourneyRegime.Vat

    /** [[Journey]] after Day of month Vat
      */
    final case class EnteredDayOfMonth(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDayOfMonth
        with JourneyRegime.Vat

    /** [[Journey]] after Start dates api call Vat
      */
    final case class RetrievedStartDates(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val startDatesResponse:           StartDatesResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedStartDates
        with JourneyRegime.Vat

    /** [[Journey]] after Affordable quotes call to ttp Vat
      */
    final case class RetrievedAffordableQuotes(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordableQuotes
        with JourneyRegime.Vat

    /** [[Journey]] after Payment plan has been chosen Vat
      */
    final case class ChosenPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ChosenPaymentPlan
        with JourneyRegime.Vat

    /** [[Journey]] after Payment plan has been checked Vat
      */
    final case class CheckedPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.CheckedPaymentPlan
        with JourneyRegime.Vat

    /** [[Journey]] after details about bank account Vat
      */
    final case class EnteredCanYouSetUpDirectDebit(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredCanYouSetUpDirectDebit
        with JourneyRegime.Vat

    /** [[Journey]] after bank details have been entered Vat
      */
    final case class EnteredDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDirectDebitDetails
        with JourneyRegime.Vat

    /** [[Journey]] after bank details have been confirmed Vat
      */
    final case class ConfirmedDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ConfirmedDirectDebitDetails
        with JourneyRegime.Vat

    /** [[Journey]] after Agreeing terms and conditions Vat
      */
    final case class AgreedTermsAndConditions(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AgreedTermsAndConditions
        with JourneyRegime.Vat

    /** [[Journey]] after Selecting email address to be verified Vat
      */
    final case class SelectedEmailToBeVerified(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SelectedEmailToBeVerified
        with JourneyRegime.Vat

    /** [[Journey]] after email verification status journey is complete Vat
      */
    final case class EmailVerificationComplete(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val emailVerificationResult:      EmailVerificationResult,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EmailVerificationComplete
        with JourneyRegime.Vat

    /** [[Journey]] after Submission of their arrangement to the enact api Vat
      */
    final case class SubmittedArrangement(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Vat,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Vat,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Vrn,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val arrangementResponse:          ArrangementResponse,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SubmittedArrangement
        with JourneyRegime.Vat
  }

  object Sa {

    /** [[Journey]] after started Sa
      */
    final case class Started(
      override val _id:                  JourneyId,
      override val origin:               Origins.Sa,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Sa,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.Started
        with JourneyRegime.Sa

    /** [[Journey]] after computed TaxIds Sa
      */
    final case class ComputedTaxId(
      override val _id:                  JourneyId,
      override val origin:               Origins.Sa,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Sa,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val taxId:                SaUtr,
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ComputedTaxId
        with JourneyRegime.Sa

    /** [[Journey]] after EligibilityCheck Sa
      */
    final case class EligibilityChecked(
      override val _id:                    JourneyId,
      override val origin:                 Origins.Sa,
      override val createdOn:              Instant,
      override val sjRequest:              SjRequest.Sa,
      override val sessionId:              SessionId,
      override val correlationId:          CorrelationId,
      override val affordabilityEnabled:   Option[Boolean],
      override val taxId:                  SaUtr,
      override val eligibilityCheckResult: EligibilityCheckResult,
      override val pegaCaseId:             Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EligibilityChecked
        with JourneyRegime.Sa

    /** [[Journey]] after WhyCannotPayInFullAnswers Sa
      */
    final case class ObtainedWhyCannotPayInFullAnswers(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Sa,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Sa,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     SaUtr,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedWhyCannotPayInFullAnswers
        with JourneyRegime.Sa

    /** [[Journey]] after CanPayUpfront Sa
      */
    final case class AnsweredCanPayUpfront(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Sa,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Sa,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     SaUtr,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AnsweredCanPayUpfront
        with JourneyRegime.Sa

    /** [[Journey]] after UpfrontPaymentAmount Sa
      */
    final case class EnteredUpfrontPaymentAmount(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Sa,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Sa,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     SaUtr,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val upfrontPaymentAmount:      UpfrontPaymentAmount,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredUpfrontPaymentAmount
        with JourneyRegime.Sa

    /** [[Journey]] after Extreme dates request to esstp-dates Sa
      */
    final case class RetrievedExtremeDates(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Sa,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Sa,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     SaUtr,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedExtremeDates
        with JourneyRegime.Sa

    /** [[Journey]] after Affordability request to tpp Sa
      */
    final case class RetrievedAffordabilityResult(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Sa,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Sa,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     SaUtr,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val instalmentAmounts:         InstalmentAmounts,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordabilityResult
        with JourneyRegime.Sa

    /** [[Journey]] after answers to CanPayWithinSixMonths if needed Sa
      */
    final case class ObtainedCanPayWithinSixMonthsAnswers(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedCanPayWithinSixMonthsAnswers
        with JourneyRegime.Sa

    /** [[Journey]] after started a PEGA case Sa
      */
    final case class StartedPegaCase(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val startCaseResponse:            StartCaseResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.StartedPegaCase
        with JourneyRegime.Sa

    /** [[Journey]] after MonthlyPaymentAmount Sa
      */
    final case class EnteredMonthlyPaymentAmount(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredMonthlyPaymentAmount
        with JourneyRegime.Sa

    /** [[Journey]] after Day of month Sa
      */
    final case class EnteredDayOfMonth(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDayOfMonth
        with JourneyRegime.Sa

    /** [[Journey]] after Start dates api call Sa
      */
    final case class RetrievedStartDates(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val startDatesResponse:           StartDatesResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedStartDates
        with JourneyRegime.Sa

    /** [[Journey]] after Affordable quotes call to ttp Sa
      */
    final case class RetrievedAffordableQuotes(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordableQuotes
        with JourneyRegime.Sa

    /** [[Journey]] after Payment plan has been chosen Sa
      */
    final case class ChosenPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ChosenPaymentPlan
        with JourneyRegime.Sa

    /** [[Journey]] after Payment plan has been checked Sa
      */
    final case class CheckedPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.CheckedPaymentPlan
        with JourneyRegime.Sa

    /** [[Journey]] after details about bank account Sa
      */
    final case class EnteredCanYouSetUpDirectDebit(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredCanYouSetUpDirectDebit
        with JourneyRegime.Sa

    /** [[Journey]] after bank details have been entered Sa
      */
    final case class EnteredDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDirectDebitDetails
        with JourneyRegime.Sa

    /** [[Journey]] after bank details have been confirmed Sa
      */
    final case class ConfirmedDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ConfirmedDirectDebitDetails
        with JourneyRegime.Sa

    /** [[Journey]] after Agreeing terms and conditions Sa
      */
    final case class AgreedTermsAndConditions(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AgreedTermsAndConditions
        with JourneyRegime.Sa

    /** [[Journey]] after Selecting email address to be verified Sa
      */
    final case class SelectedEmailToBeVerified(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SelectedEmailToBeVerified
        with JourneyRegime.Sa

    /** [[Journey]] after email verification status journey is complete Sa
      */
    final case class EmailVerificationComplete(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val emailVerificationResult:      EmailVerificationResult,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EmailVerificationComplete
        with JourneyRegime.Sa

    /** [[Journey]] after Submission of their arrangement to the enact api Sa
      */
    final case class SubmittedArrangement(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Sa,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Sa,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        SaUtr,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val arrangementResponse:          ArrangementResponse,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SubmittedArrangement
        with JourneyRegime.Sa
  }

  object Simp {

    /** [[Journey]] after started Simp
      */
    final case class Started(
      override val _id:                  JourneyId,
      override val origin:               Origins.Simp,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Simp,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.Started
        with JourneyRegime.Simp

    /** [[Journey]] after computed TaxIds Simp
      */
    final case class ComputedTaxId(
      override val _id:                  JourneyId,
      override val origin:               Origins.Simp,
      override val createdOn:            Instant,
      override val sjRequest:            SjRequest.Simp,
      override val sessionId:            SessionId,
      override val correlationId:        CorrelationId,
      override val affordabilityEnabled: Option[Boolean],
      override val taxId:                Nino,
      override val pegaCaseId:           Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ComputedTaxId
        with JourneyRegime.Simp

    /** [[Journey]] after EligibilityCheck Simp
      */
    final case class EligibilityChecked(
      override val _id:                    JourneyId,
      override val origin:                 Origins.Simp,
      override val createdOn:              Instant,
      override val sjRequest:              SjRequest.Simp,
      override val sessionId:              SessionId,
      override val correlationId:          CorrelationId,
      override val affordabilityEnabled:   Option[Boolean],
      override val taxId:                  Nino,
      override val eligibilityCheckResult: EligibilityCheckResult,
      override val pegaCaseId:             Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EligibilityChecked
        with JourneyRegime.Simp

    /** [[Journey]] after WhyCannotPayInFullAnswers Simp
      */
    final case class ObtainedWhyCannotPayInFullAnswers(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Simp,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Simp,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Nino,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedWhyCannotPayInFullAnswers
        with JourneyRegime.Simp

    /** [[Journey]] after CanPayUpfront Simp
      */
    final case class AnsweredCanPayUpfront(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Simp,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Simp,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Nino,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AnsweredCanPayUpfront
        with JourneyRegime.Simp

    /** [[Journey]] after UpfrontPaymentAmount Simp
      */
    final case class EnteredUpfrontPaymentAmount(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Simp,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Simp,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Nino,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val canPayUpfront:             CanPayUpfront,
      override val upfrontPaymentAmount:      UpfrontPaymentAmount,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredUpfrontPaymentAmount
        with JourneyRegime.Simp

    /** [[Journey]] after Extreme dates request to esstp-dates Simp
      */
    final case class RetrievedExtremeDates(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Simp,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Simp,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Nino,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedExtremeDates
        with JourneyRegime.Simp

    /** [[Journey]] after Affordability request to tpp Simp
      */
    final case class RetrievedAffordabilityResult(
      override val _id:                       JourneyId,
      override val origin:                    Origins.Simp,
      override val createdOn:                 Instant,
      override val sjRequest:                 SjRequest.Simp,
      override val sessionId:                 SessionId,
      override val correlationId:             CorrelationId,
      override val affordabilityEnabled:      Option[Boolean],
      override val taxId:                     Nino,
      override val eligibilityCheckResult:    EligibilityCheckResult,
      override val whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:     UpfrontPaymentAnswers,
      override val extremeDatesResponse:      ExtremeDatesResponse,
      override val instalmentAmounts:         InstalmentAmounts,
      override val pegaCaseId:                Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordabilityResult
        with JourneyRegime.Simp

    /** [[Journey]] after answers to CanPayWithinSixMonths if needed Simp
      */
    final case class ObtainedCanPayWithinSixMonthsAnswers(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ObtainedCanPayWithinSixMonthsAnswers
        with JourneyRegime.Simp

    /** [[Journey]] after started a PEGA case Simp
      */
    final case class StartedPegaCase(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val startCaseResponse:            StartCaseResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.StartedPegaCase
        with JourneyRegime.Simp

    /** [[Journey]] after MonthlyPaymentAmount Simp
      */
    final case class EnteredMonthlyPaymentAmount(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredMonthlyPaymentAmount
        with JourneyRegime.Simp

    /** [[Journey]] after Day of month Simp
      */
    final case class EnteredDayOfMonth(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDayOfMonth
        with JourneyRegime.Simp

    /** [[Journey]] after Start dates api call Simp
      */
    final case class RetrievedStartDates(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val monthlyPaymentAmount:         MonthlyPaymentAmount,
      override val dayOfMonth:                   DayOfMonth,
      override val startDatesResponse:           StartDatesResponse,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedStartDates
        with JourneyRegime.Simp

    /** [[Journey]] after Affordable quotes call to ttp Simp
      */
    final case class RetrievedAffordableQuotes(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.RetrievedAffordableQuotes
        with JourneyRegime.Simp

    /** [[Journey]] after Payment plan has been chosen Simp
      */
    final case class ChosenPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
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
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ChosenPaymentPlan
        with JourneyRegime.Simp

    /** [[Journey]] after Payment plan has been checked Simp
      */
    final case class CheckedPaymentPlan(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.CheckedPaymentPlan
        with JourneyRegime.Simp

    /** [[Journey]] after details about bank account Simp
      */
    final case class EnteredCanYouSetUpDirectDebit(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredCanYouSetUpDirectDebit
        with JourneyRegime.Simp

    /** [[Journey]] after bank details have been entered Simp
      */
    final case class EnteredDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EnteredDirectDebitDetails
        with JourneyRegime.Simp

    /** [[Journey]] after bank details have been confirmed Simp
      */
    final case class ConfirmedDirectDebitDetails(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.ConfirmedDirectDebitDetails
        with JourneyRegime.Simp

    /** [[Journey]] after Agreeing terms and conditions Simp
      */
    final case class AgreedTermsAndConditions(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.AgreedTermsAndConditions
        with JourneyRegime.Simp

    /** [[Journey]] after Selecting email address to be verified Simp
      */
    final case class SelectedEmailToBeVerified(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SelectedEmailToBeVerified
        with JourneyRegime.Simp

    /** [[Journey]] after email verification status journey is complete Simp
      */
    final case class EmailVerificationComplete(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val emailToBeVerified:            Email,
      override val emailVerificationResult:      EmailVerificationResult,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.EmailVerificationComplete
        with JourneyRegime.Simp

    /** [[Journey]] after Submission of their arrangement to the enact api Simp
      */
    final case class SubmittedArrangement(
      override val _id:                          JourneyId,
      override val origin:                       Origins.Simp,
      override val createdOn:                    Instant,
      override val sjRequest:                    SjRequest.Simp,
      override val sessionId:                    SessionId,
      override val correlationId:                CorrelationId,
      override val affordabilityEnabled:         Option[Boolean],
      override val taxId:                        Nino,
      override val eligibilityCheckResult:       EligibilityCheckResult,
      override val whyCannotPayInFullAnswers:    WhyCannotPayInFullAnswers,
      override val upfrontPaymentAnswers:        UpfrontPaymentAnswers,
      override val extremeDatesResponse:         ExtremeDatesResponse,
      override val instalmentAmounts:            InstalmentAmounts,
      override val canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers,
      override val paymentPlanAnswers:           PaymentPlanAnswers,
      override val canSetUpDirectDebitAnswer:    CanSetUpDirectDebit,
      override val directDebitDetails:           BankDetails,
      override val isEmailAddressRequired:       IsEmailAddressRequired,
      override val arrangementResponse:          ArrangementResponse,
      override val emailVerificationAnswers:     EmailVerificationAnswers,
      override val pegaCaseId:                   Option[PegaCaseId]
    ) extends Journey
        with JourneyStageView.SubmittedArrangement
        with JourneyRegime.Simp
  }

  private def journeyCirceCodec(implicit cryptoFormat: CryptoFormat): Codec.AsObject[Journey] =
    deriveCodec[Journey]

  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[Journey] = {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    val defaultFormat: OFormat[Journey] = DerivedJson.Circe.format(journeyCirceCodec)

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
      defaultFormat,
      customWrites
    )
  }
}
