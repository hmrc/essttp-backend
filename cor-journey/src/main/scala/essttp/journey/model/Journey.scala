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

package essttp.journey.model

import essttp.crypto.CryptoFormat
import essttp.emailverification.EmailVerificationStatus
import essttp.rootmodel._
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.utils.Errors
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import julienrf.json.derived
import play.api.libs.json.{JsValue, Json, OFormat, OWrites}

import java.time.{Clock, Instant}

sealed trait Journey {
  def _id: JourneyId
  def origin: Origin
  def createdOn: Instant
  val lastUpdated: Instant = Instant.now(Clock.systemUTC())
  def sjRequest: SjRequest
  def sessionId: SessionId
  def taxRegime: TaxRegime
  def stage: Stage
  def correlationId: CorrelationId

  /* derived stuff: */

  def id: JourneyId = _id
  def journeyId: JourneyId = _id
  val traceId: TraceId = TraceId(journeyId)

  def name: String = {
    val className = getClass.getName
    val packageName = getClass.getPackage.getName
    className
      .replaceAllLiterally(s"$packageName.", "")
      .replaceAllLiterally("$", ".")
  }

  def backUrl: Option[BackUrl]
  def returnUrl: Option[ReturnUrl]

}

object Journey {

  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[Journey] = {

    val defaultFormat: OFormat[Journey] = derived.oformat[Journey]()

    //we need to write some extra fields on the top of the structure so it's
    //possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId" -> j.sessionId,
        "createdAt" -> MongoJavatimeFormats.instantFormat.writes(j.createdOn),
        "lastUpdated" -> MongoJavatimeFormats.instantFormat.writes(j.lastUpdated),
        "correlationId" -> j.correlationId
      ))
    OFormat(
      defaultFormat,
      customWrites
    )
  }

  implicit class JourneyOps(private val j: Journey) extends AnyVal {

    def json(implicit cryptoFormat: CryptoFormat): JsValue = Json.toJson(j)

  }

  sealed trait BeforeComputedTaxId extends Journey with Stages.JourneyStage

  sealed trait AfterComputedTaxId extends Journey {
    def taxId: TaxId
  }

  sealed trait BeforeEligibilityChecked extends Journey with Stages.JourneyStage

  sealed trait AfterEligibilityChecked extends Journey {
    def eligibilityCheckResult: EligibilityCheckResult
  }

  sealed trait BeforeAnsweredCanPayUpfront extends Journey with Stages.JourneyStage

  sealed trait AfterAnsweredCanPayUpfront extends Journey {
    def canPayUpfront: CanPayUpfront
  }

  sealed trait BeforeEnteredUpfrontPaymentAmount extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredUpfrontPaymentAmount extends Journey {
    def upfrontPaymentAmount: UpfrontPaymentAmount
  }

  sealed trait BeforeUpfrontPaymentAnswers extends Journey with Stages.JourneyStage

  sealed trait AfterUpfrontPaymentAnswers extends Journey with Stages.JourneyStage {
    def upfrontPaymentAnswers: UpfrontPaymentAnswers
  }

  sealed trait BeforeExtremeDatesResponse extends Journey with Stages.JourneyStage

  sealed trait AfterExtremeDatesResponse extends Journey with Stages.JourneyStage {
    def extremeDatesResponse: ExtremeDatesResponse
  }

  sealed trait BeforeRetrievedAffordabilityResult extends Journey with Stages.JourneyStage

  sealed trait AfterRetrievedAffordabilityResult extends Journey {
    def instalmentAmounts: InstalmentAmounts
  }

  sealed trait BeforeEnteredMonthlyPaymentAmount extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredMonthlyPaymentAmount extends Journey {
    def monthlyPaymentAmount: MonthlyPaymentAmount
  }

  sealed trait BeforeEnteredDayOfMonth extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredDayOfMonth extends Journey {
    def dayOfMonth: DayOfMonth
  }

  sealed trait BeforeStartDatesResponse extends Journey with Stages.JourneyStage

  sealed trait AfterStartDatesResponse extends Journey {
    def startDatesResponse: StartDatesResponse
  }

  sealed trait BeforeAffordableQuotesResponse extends Journey with Stages.JourneyStage

  sealed trait AfterAffordableQuotesResponse extends Journey {
    def affordableQuotesResponse: AffordableQuotesResponse
  }

  sealed trait BeforeSelectedPaymentPlan extends Journey with Stages.JourneyStage

  sealed trait AfterSelectedPaymentPlan extends Journey {
    def selectedPaymentPlan: PaymentPlan
  }

  sealed trait BeforeCheckedPaymentPlan extends Journey with Stages.JourneyStage

  sealed trait AfterCheckedPaymentPlan extends Journey

  sealed trait BeforeEnteredDetailsAboutBankAccount extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredDetailsAboutBankAccount extends Journey {
    def detailsAboutBankAccount: DetailsAboutBankAccount
  }

  sealed trait BeforeEnteredDirectDebitDetails extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredDirectDebitDetails extends Journey {
    def directDebitDetails: BankDetails
  }

  sealed trait BeforeConfirmedDirectDebitDetails extends Journey with Stages.JourneyStage

  sealed trait AfterConfirmedDirectDebitDetails extends Journey

  sealed trait BeforeAgreedTermsAndConditions extends Journey with Stages.JourneyStage

  sealed trait AfterAgreedTermsAndConditions extends Journey {
    def isEmailAddressRequired: IsEmailAddressRequired
  }

  sealed trait BeforeEmailAddressSelectedToBeVerified extends Journey with Stages.JourneyStage

  sealed trait AfterEmailAddressSelectedToBeVerified extends Journey with Stages.JourneyStage {
    def emailToBeVerified: Email
  }

  sealed trait BeforeEmailAddressVerificationResult extends Journey with Stages.JourneyStage

  sealed trait AfterEmailAddressVerificationResult extends Journey with Stages.JourneyStage {
    def emailVerificationStatus: EmailVerificationStatus
  }

  sealed trait BeforeEmailVerificationPhase extends Journey with Stages.JourneyStage

  sealed trait AfterEmailVerificationPhase extends Journey with Stages.JourneyStage {
    def emailVerificationAnswers: EmailVerificationAnswers
  }

  sealed trait BeforeArrangementSubmitted extends Journey with Stages.JourneyStage

  sealed trait AfterArrangementSubmitted extends Journey {
    def arrangementResponse: ArrangementResponse
  }

  /**
   * Journey extractors extracting journeys in particular stage.
   * They correspond to actual [[Stage]] values
   */
  object Stages {

    /**
     * Marking trait for selecting journey in stage
     */
    sealed trait JourneyStage extends Journey {
      def stage: Stage
    }

    private val sanityMessage = "Sanity check just in case if you messed journey traits up"

    sealed trait Started
      extends Journey
      with JourneyStage
      with BeforeComputedTaxId
      with BeforeEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterStarted.values.contains(stage), sanityMessage)
      def stage: Stage.AfterStarted
    }

    sealed trait ComputedTaxId
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with BeforeEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterComputedTaxId.values.contains(stage), sanityMessage)
      def stage: Stage.AfterComputedTaxId
    }

    sealed trait EligibilityChecked
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterEligibilityCheck.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEligibilityCheck
    }

    sealed trait AnsweredCanPayUpfront
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterCanPayUpfront.values.contains(stage), sanityMessage)
      def stage: Stage.AfterCanPayUpfront
    }

    sealed trait EnteredUpfrontPaymentAmount
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with AfterEnteredUpfrontPaymentAmount
      with BeforeUpfrontPaymentAnswers
      with BeforeExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterUpfrontPaymentAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterUpfrontPaymentAmount
    }

    sealed trait RetrievedExtremeDates
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with BeforeRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterExtremeDatesResponse.values.contains(stage), sanityMessage)
      def stage: Stage.AfterExtremeDatesResponse
    }

    sealed trait RetrievedAffordabilityResult
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with BeforeEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterAffordabilityResult.values.contains(stage), sanityMessage)
      def stage: Stage.AfterAffordabilityResult
    }

    sealed trait EnteredMonthlyPaymentAmount
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with BeforeEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterMonthlyPaymentAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterMonthlyPaymentAmount
    }

    sealed trait EnteredDayOfMonth
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with BeforeStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterEnteredDayOfMonth.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDayOfMonth
    }

    sealed trait RetrievedStartDates
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with BeforeAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterStartDatesResponse.values.contains(stage), sanityMessage)
      def stage: Stage.AfterStartDatesResponse
    }

    sealed trait RetrievedAffordableQuotes
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with BeforeSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterAffordableQuotesResponse.values.contains(stage), sanityMessage)
      def stage: Stage.AfterAffordableQuotesResponse
    }

    sealed trait ChosenPaymentPlan
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with BeforeCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterSelectedPlan.values.contains(stage), sanityMessage)
      def stage: Stage.AfterSelectedPlan
    }

    sealed trait CheckedPaymentPlan
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with BeforeEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterCheckedPlan.values.contains(stage), sanityMessage)
      def stage: Stage.AfterCheckedPlan
    }

    sealed trait EnteredDetailsAboutBankAccount
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterEnteredDetailsAboutBankAccount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDetailsAboutBankAccount
    }

    sealed trait EnteredDirectDebitDetails
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterEnteredDirectDebitDetails.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDirectDebitDetails
    }

    sealed trait ConfirmedDirectDebitDetails
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterConfirmedDirectDebitDetails.values.contains(stage), sanityMessage)
      def stage: Stage.AfterConfirmedDirectDebitDetails
    }

    sealed trait AgreedTermsAndConditions
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
      with BeforeEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterAgreedTermsAndConditions.values.contains(stage), sanityMessage)
      def stage: Stage.AfterAgreedTermsAndConditions
    }

    sealed trait SelectedEmailToBeVerified
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
      with AfterEmailAddressSelectedToBeVerified
      with BeforeEmailAddressVerificationResult
      with BeforeEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterSelectedAnEmailToBeVerified.values.contains(stage), sanityMessage)
      def stage: Stage.AfterSelectedAnEmailToBeVerified
    }

    sealed trait EmailVerificationComplete
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
      with AfterEmailAddressSelectedToBeVerified
      with AfterEmailAddressVerificationResult
      with AfterEmailVerificationPhase
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterEmailVerificationPhase.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEmailVerificationPhase
    }

    sealed trait SubmittedArrangement
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterUpfrontPaymentAnswers
      with AfterExtremeDatesResponse
      with AfterRetrievedAffordabilityResult
      with AfterEnteredMonthlyPaymentAmount
      with AfterEnteredDayOfMonth
      with AfterStartDatesResponse
      with AfterAffordableQuotesResponse
      with AfterSelectedPaymentPlan
      with AfterCheckedPaymentPlan
      with AfterEnteredDetailsAboutBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
      with AfterEmailVerificationPhase
      with AfterArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterSubmittedArrangement.values.contains(stage), sanityMessage)
      def stage: Stage.AfterSubmittedArrangement
    }

  }

  /**
   * Marking trait for extracting Epaye [[Journey]]s
   */
  sealed trait Epaye extends Journey {
    override def taxRegime: TaxRegime.Epaye.type = TaxRegime.Epaye
    override def sjRequest: SjRequest.Epaye
    override def origin: Origins.Epaye

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Epaye.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                         => (None, None)
    }
  }

  object Epaye {

    /**
     * [[Journey]] after started
     * Epaye
     */
    final case class Started(
        override val _id:           JourneyId,
        override val origin:        Origins.Epaye,
        override val createdOn:     Instant,
        override val sjRequest:     SjRequest.Epaye,
        override val sessionId:     SessionId,
        override val correlationId: CorrelationId,
        override val stage:         Stage.AfterStarted
    )
      extends Journey
      with Journey.Stages.Started
      with Journey.Epaye

    /**
     * [[Journey]] after computed TaxIds
     * Epaye
     */
    final case class ComputedTaxId(
        override val _id:           JourneyId,
        override val origin:        Origins.Epaye,
        override val createdOn:     Instant,
        override val sjRequest:     SjRequest.Epaye,
        override val sessionId:     SessionId,
        override val correlationId: CorrelationId,
        override val stage:         Stage.AfterComputedTaxId,
        override val taxId:         EmpRef
    )
      extends Journey
      with Journey.Stages.ComputedTaxId
      with Journey.Epaye

    /**
     * [[Journey]] after EligibilityCheck
     * Epaye
     */
    final case class EligibilityChecked(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterEligibilityCheck,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult
    )
      extends Journey
      with Journey.Stages.EligibilityChecked
      with Journey.Epaye

    /**
     * [[Journey]] after CanPayUpfront
     * Epaye
     */
    final case class AnsweredCanPayUpfront(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterCanPayUpfront,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront
    )
      extends Journey
      with Journey.Stages.AnsweredCanPayUpfront
      with Journey.Epaye

    /**
     * [[Journey]] after UpfrontPaymentAmount
     * Epaye
     */
    final case class EnteredUpfrontPaymentAmount(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterUpfrontPaymentAmount,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront,
        override val upfrontPaymentAmount:   UpfrontPaymentAmount
    )
      extends Journey
      with Journey.Stages.EnteredUpfrontPaymentAmount
      with Journey.Epaye

    /**
     * [[Journey]] after Affordability request to tpp
     * Epaye
     */
    final case class RetrievedExtremeDates(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterExtremeDatesResponse,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val upfrontPaymentAnswers:  UpfrontPaymentAnswers,
        override val extremeDatesResponse:   ExtremeDatesResponse
    )
      extends Journey
      with Journey.Stages.RetrievedExtremeDates
      with Journey.Epaye

    /**
     * [[Journey]] after Affordability request to tpp
     * Epaye
     */
    final case class RetrievedAffordabilityResult(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterAffordabilityResult,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val upfrontPaymentAnswers:  UpfrontPaymentAnswers,
        override val extremeDatesResponse:   ExtremeDatesResponse,
        override val instalmentAmounts:      InstalmentAmounts
    )
      extends Journey
      with Journey.Stages.RetrievedAffordabilityResult
      with Journey.Epaye

    /**
     * [[Journey]] after MonthlyPaymentAmount
     * Epaye
     */
    final case class EnteredMonthlyPaymentAmount(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterMonthlyPaymentAmount,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val upfrontPaymentAnswers:  UpfrontPaymentAnswers,
        override val extremeDatesResponse:   ExtremeDatesResponse,
        override val instalmentAmounts:      InstalmentAmounts,
        override val monthlyPaymentAmount:   MonthlyPaymentAmount
    )
      extends Journey
      with Journey.Stages.EnteredMonthlyPaymentAmount
      with Journey.Epaye

    /**
     * [[Journey]] after Day of month
     * Epaye
     */
    final case class EnteredDayOfMonth(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterEnteredDayOfMonth,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val upfrontPaymentAnswers:  UpfrontPaymentAnswers,
        override val extremeDatesResponse:   ExtremeDatesResponse,
        override val instalmentAmounts:      InstalmentAmounts,
        override val monthlyPaymentAmount:   MonthlyPaymentAmount,
        override val dayOfMonth:             DayOfMonth
    )
      extends Journey
      with Journey.Stages.EnteredDayOfMonth
      with Journey.Epaye

    /**
     * [[Journey]] after Start dates api call
     * Epaye
     */
    final case class RetrievedStartDates(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              Instant,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val correlationId:          CorrelationId,
        override val stage:                  Stage.AfterStartDatesResponse,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val upfrontPaymentAnswers:  UpfrontPaymentAnswers,
        override val extremeDatesResponse:   ExtremeDatesResponse,
        override val instalmentAmounts:      InstalmentAmounts,
        override val monthlyPaymentAmount:   MonthlyPaymentAmount,
        override val dayOfMonth:             DayOfMonth,
        override val startDatesResponse:     StartDatesResponse
    )
      extends Journey
      with Journey.Stages.RetrievedStartDates
      with Journey.Epaye

    /**
     * [[Journey]] after Affordable quotes call to ttp
     * Epaye
     */
    final case class RetrievedAffordableQuotes(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterAffordableQuotesResponse,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse
    )
      extends Journey
      with Journey.Stages.RetrievedAffordableQuotes
      with Journey.Epaye

    /**
     * [[Journey]] after Payment plan has been chosen
     * Epaye
     */
    final case class ChosenPaymentPlan(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterSelectedPlan,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan
    )
      extends Journey
      with Journey.Stages.ChosenPaymentPlan
      with Journey.Epaye

    /**
     * [[Journey]] after Payment plan has been checked
     * Epaye
     */
    final case class CheckedPaymentPlan(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterCheckedPlan,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan
    )
      extends Journey
      with Journey.Stages.CheckedPaymentPlan
      with Journey.Epaye

    /**
     * [[Journey]] after details about bank account
     * Epaye
     */
    final case class EnteredDetailsAboutBankAccount(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterEnteredDetailsAboutBankAccount,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount
    )
      extends Journey
      with Journey.Stages.EnteredDetailsAboutBankAccount
      with Journey.Epaye

    /**
     * [[Journey]] after bank details have been entered
     * Epaye
     */
    final case class EnteredDirectDebitDetails(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterEnteredDirectDebitDetails,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails
    )
      extends Journey
      with Journey.Stages.EnteredDirectDebitDetails
      with Journey.Epaye

    /**
     * [[Journey]] after bank details have been confirmed
     * Epaye
     */
    final case class ConfirmedDirectDebitDetails(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterConfirmedDirectDebitDetails,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails
    )
      extends Journey
      with Journey.Stages.ConfirmedDirectDebitDetails
      with Journey.Epaye

    /**
     * [[Journey]] after Agreeing terms and conditions
     * Epaye
     */
    final case class AgreedTermsAndConditions(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterAgreedTermsAndConditions,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails,
        override val isEmailAddressRequired:   IsEmailAddressRequired
    )
      extends Journey
      with Journey.Stages.AgreedTermsAndConditions
      with Journey.Epaye

    /**
     * [[Journey]] after Selecting email address to be verified
     * Epaye
     */
    final case class SelectedEmailToBeVerified(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterSelectedAnEmailToBeVerified,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails,
        override val isEmailAddressRequired:   IsEmailAddressRequired,
        override val emailToBeVerified:        Email
    )
      extends Journey
      with Journey.Stages.SelectedEmailToBeVerified
      with Journey.Epaye

    /**
     * [[Journey]] after email verification status journey is complete
     * Epaye
     */
    final case class EmailVerificationComplete(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterEmailVerificationPhase,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails,
        override val isEmailAddressRequired:   IsEmailAddressRequired,
        override val emailToBeVerified:        Email,
        override val emailVerificationStatus:  EmailVerificationStatus,
        override val emailVerificationAnswers: EmailVerificationAnswers
    )
      extends Journey
      with Journey.Stages.EmailVerificationComplete
      with Journey.Epaye

    /**
     * [[Journey]] after Submission of their arrangement to the enact api
     * Epaye
     */
    final case class SubmittedArrangement(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val correlationId:            CorrelationId,
        override val stage:                    Stage.AfterSubmittedArrangement,
        override val taxId:                    EmpRef,
        override val eligibilityCheckResult:   EligibilityCheckResult,
        override val upfrontPaymentAnswers:    UpfrontPaymentAnswers,
        override val extremeDatesResponse:     ExtremeDatesResponse,
        override val instalmentAmounts:        InstalmentAmounts,
        override val monthlyPaymentAmount:     MonthlyPaymentAmount,
        override val dayOfMonth:               DayOfMonth,
        override val startDatesResponse:       StartDatesResponse,
        override val affordableQuotesResponse: AffordableQuotesResponse,
        override val selectedPaymentPlan:      PaymentPlan,
        override val detailsAboutBankAccount:  DetailsAboutBankAccount,
        override val directDebitDetails:       BankDetails,
        override val isEmailAddressRequired:   IsEmailAddressRequired,
        override val arrangementResponse:      ArrangementResponse,
        override val emailVerificationAnswers: EmailVerificationAnswers
    )
      extends Journey
      with Journey.Stages.SubmittedArrangement
      with Journey.Epaye

  }

  /**
   * Marking trait for extracting Vat [[Journey]]s
   */
  sealed trait Vat extends Journey {
    override def taxRegime: TaxRegime.Vat.type = TaxRegime.Vat
    override def sjRequest: SjRequest.Vat
    override def origin: Origins.Vat

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Vat.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                       => (None, None)
    }
  }

  object Vat {
    /**
     * [[Journey]] after started
     * VAT
     */
    final case class Started(
        override val _id:           JourneyId,
        override val origin:        Origins.Vat,
        override val createdOn:     Instant,
        override val sjRequest:     SjRequest.Vat,
        override val sessionId:     SessionId,
        override val correlationId: CorrelationId,
        override val stage:         Stage.AfterStarted
    )
      extends Journey
      with Journey.Stages.Started
      with Journey.Vat

    /**
     * [[Journey]] after computed TaxIds
     * VAT
     */
    final case class ComputedTaxId(
        override val _id:           JourneyId,
        override val origin:        Origins.Vat,
        override val createdOn:     Instant,
        override val sjRequest:     SjRequest.Vat,
        override val sessionId:     SessionId,
        override val correlationId: CorrelationId,
        override val stage:         Stage.AfterComputedTaxId,
        override val taxId:         Vrn
    )
      extends Journey
      with Journey.Stages.ComputedTaxId
      with Journey.Vat
  }
}
