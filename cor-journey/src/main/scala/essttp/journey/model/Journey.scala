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

import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.journey.model.ttp.affordability.InstalmentAmounts
import essttp.journey.model.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.journey.model.ttp.arrangement.ArrangementResponse
import essttp.rootmodel._
import essttp.rootmodel.bank.{DirectDebitDetails, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.utils.Errors
import julienrf.json.derived
import play.api.libs.json.{Json, OFormat, OWrites}

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

  implicit val format: OFormat[Journey] = {

    val defaultFormat: OFormat[Journey] = derived.oformat[Journey]()

    //we need to write some extra fields on the top of the structure so it's
    //possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId" -> j.sessionId,
        "createdAt" -> j.createdOn,
        "lastUpdated" -> j.lastUpdated
      ))
    OFormat(
      defaultFormat,
      customWrites
    )
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

  sealed trait BeforeChosenTypeOfBankAccount extends Journey with Stages.JourneyStage

  sealed trait AfterChosenTypeOfBankAccount extends Journey {
    def typeOfBankAccount: TypeOfBankAccount
  }

  sealed trait BeforeEnteredDirectDebitDetails extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredDirectDebitDetails extends Journey {
    def directDebitDetails: DirectDebitDetails
  }

  sealed trait BeforeConfirmedDirectDebitDetails extends Journey with Stages.JourneyStage

  sealed trait AfterConfirmedDirectDebitDetails extends Journey

  sealed trait BeforeAgreedTermsAndConditions extends Journey with Stages.JourneyStage

  sealed trait AfterAgreedTermsAndConditions extends Journey

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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with BeforeChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterCheckedPlan.values.contains(stage), sanityMessage)
      def stage: Stage.AfterCheckedPlan
    }

    sealed trait ChosenTypeOfBankAccount
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
      with AfterChosenTypeOfBankAccount
      with BeforeEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterChosenTypeOfBankAccount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterChosenTypeOfBankAccount
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
      with AfterChosenTypeOfBankAccount
      with AfterEnteredDirectDebitDetails
      with BeforeConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with AfterChosenTypeOfBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with BeforeAgreedTermsAndConditions
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
      with AfterChosenTypeOfBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
      with BeforeArrangementSubmitted {
      Errors.sanityCheck(Stage.AfterAgreedTermsAndConditions.values.contains(stage), sanityMessage)
      def stage: Stage.AfterAgreedTermsAndConditions
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
      with AfterChosenTypeOfBankAccount
      with AfterEnteredDirectDebitDetails
      with AfterConfirmedDirectDebitDetails
      with AfterAgreedTermsAndConditions
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
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: Instant,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterStarted
    )
      extends Journey
      with Journey.Stages.Started
      with Journey.Epaye

    /**
     * [[Journey]] after computed TaxIds
     * Epaye
     */
    final case class ComputedTaxId(
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: Instant,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterComputedTaxId,
        override val taxId:     EmpRef
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
     * [[Journey]] after Payment plan has been chosen
     * Epaye
     */
    final case class CheckedPaymentPlan(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
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
     * [[Journey]] after Payment plan has been chosen
     * Epaye
     */
    final case class ChosenTypeOfBankAccount(
        override val _id:                      JourneyId,
        override val origin:                   Origins.Epaye,
        override val createdOn:                Instant,
        override val sjRequest:                SjRequest.Epaye,
        override val sessionId:                SessionId,
        override val stage:                    Stage.AfterChosenTypeOfBankAccount,
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
        override val typeOfBankAccount:        TypeOfBankAccount
    )
      extends Journey
      with Journey.Stages.ChosenTypeOfBankAccount
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
        override val typeOfBankAccount:        TypeOfBankAccount,
        override val directDebitDetails:       DirectDebitDetails
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
        override val typeOfBankAccount:        TypeOfBankAccount,
        override val directDebitDetails:       DirectDebitDetails
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
        override val typeOfBankAccount:        TypeOfBankAccount,
        override val directDebitDetails:       DirectDebitDetails
    )
      extends Journey
      with Journey.Stages.AgreedTermsAndConditions
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
        override val typeOfBankAccount:        TypeOfBankAccount,
        override val directDebitDetails:       DirectDebitDetails,
        override val arrangementResponse:      ArrangementResponse
    )
      extends Journey
      with Journey.Stages.SubmittedArrangement
      with Journey.Epaye

  }

}
