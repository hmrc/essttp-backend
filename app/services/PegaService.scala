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

package services

import connectors.PegaConnector
import essttp.enrolments.{EnrolmentDef, EnrolmentDefResult}
import essttp.journey.model.Journey.{Epaye, Sa, Sia, Vat}
import essttp.journey.model._
import essttp.rootmodel.epaye.{TaxOfficeNumber, TaxOfficeReference}
import essttp.rootmodel.pega.{GetCaseResponse, PegaAssigmentId, PegaCaseId, StartCaseResponse}
import essttp.rootmodel.ttp.PaymentPlanFrequencies
import essttp.rootmodel.ttp.affordablequotes._
import essttp.rootmodel.ttp.eligibility.{ChargeTypeAssessment, Charges, EligibilityCheckResult}
import essttp.rootmodel._
import essttp.utils.RequestSupport.hc
import essttp.utils.{Errors, RequestSupport}
import models.pega.PegaStartCaseRequest.MDTPropertyMapping
import models.pega.{PegaStartCaseRequest, PegaStartCaseResponse}
import play.api.mvc.Request
import repository.JourneyByTaxIdRepo
import repository.JourneyByTaxIdRepo.JourneyWithTaxId
import uk.gov.hmrc.auth.core.Enrolments

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PegaService @Inject() (
    pegaConnector:      PegaConnector,
    journeyByTaxIdRepo: JourneyByTaxIdRepo,
    journeyService:     JourneyService
)(implicit ec: ExecutionContext) {

  def startCase(journeyId: JourneyId)(implicit r: Request[_]): Future[StartCaseResponse] = {
    for {
      journey <- journeyService.get(journeyId)
      request = toPegaStartCaseRequest(journey)
      token <- pegaConnector.getToken()
      response <- pegaConnector.startCase(request, token)
    } yield toStartCaseResponse(response)
  }

  def getCase(journeyId: JourneyId)(implicit r: Request[_]): Future[GetCaseResponse] = {
    for {
      journey <- journeyService.get(journeyId)
      caseId = getCaseId(journey)
      token <- pegaConnector.getToken()
      response <- pegaConnector.getCase(caseId, token)
    } yield GetCaseResponse(response.paymentPlan)
  }

  def saveJourney(journeyId: JourneyId)(implicit r: Request[_]): Future[Unit] = {
    journeyService.get(journeyId).flatMap{
      case _: Journey.BeforeComputedTaxId =>
        Errors.throwBadRequestExceptionF("Cannot save journey when no tax ID has been computed yet")
      case j: Journey.AfterComputedTaxId =>
        val now = Instant.now(Clock.systemUTC())
        journeyByTaxIdRepo.upsert(JourneyWithTaxId(j.taxId, j, now))
    }
  }

  def recreateSession(taxRegime: TaxRegime, enrolments: Enrolments)(implicit r: Request[_]): Future[Journey] = {
      def toEmpRef(taxOfficeNumber: TaxOfficeNumber, taxOfficeReference: TaxOfficeReference): EmpRef =
        EmpRef(s"${taxOfficeNumber.value}${taxOfficeReference.value}")

    val enrolmentResult: EnrolmentDefResult[TaxId] = taxRegime match {
      case TaxRegime.Epaye =>
        EnrolmentDef.Epaye.findEnrolmentValues(enrolments).map((toEmpRef _).tupled)
      case TaxRegime.Vat => EnrolmentDef.Vat.findEnrolmentValues(enrolments)
      case TaxRegime.Sa  => EnrolmentDef.Sa.findEnrolmentValues(enrolments)
      case TaxRegime.Sia => sys.error("SIA should have never ended up here")
    }

    enrolmentResult match {
      case EnrolmentDefResult.Success(taxId) =>
        journeyByTaxIdRepo.findById(taxId).flatMap {
          case Some(journeyWithTaxId) =>
            val updatedJourney = updateJourneyWithSessionId(journeyWithTaxId.journey, RequestSupport.getSessionId())
            journeyService.upsert(updatedJourney)

          case None =>
            Errors.throwNotFoundException(s"Journey not found for tax regime ${taxRegime.entryName}")
        }

      case other =>
        Errors.throwForbiddenException(s"No enrolment found for tax regime ${taxRegime.entryName}: ${other.toString}")
    }
  }

  private def updateJourneyWithSessionId(journey: Journey, sessionId: SessionId): Journey = {
    journey match {
      case j: Epaye.Started                              => j.copy(sessionId = sessionId)
      case j: Epaye.ComputedTaxId                        => j.copy(sessionId = sessionId)
      case j: Epaye.EligibilityChecked                   => j.copy(sessionId = sessionId)
      case j: Epaye.ObtainedWhyCannotPayInFullAnswers    => j.copy(sessionId = sessionId)
      case j: Epaye.AnsweredCanPayUpfront                => j.copy(sessionId = sessionId)
      case j: Epaye.EnteredUpfrontPaymentAmount          => j.copy(sessionId = sessionId)
      case j: Epaye.RetrievedExtremeDates                => j.copy(sessionId = sessionId)
      case j: Epaye.RetrievedAffordabilityResult         => j.copy(sessionId = sessionId)
      case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers => j.copy(sessionId = sessionId)
      case j: Epaye.StartedPegaCase                      => j.copy(sessionId = sessionId)
      case j: Epaye.EnteredMonthlyPaymentAmount          => j.copy(sessionId = sessionId)
      case j: Epaye.EnteredDayOfMonth                    => j.copy(sessionId = sessionId)
      case j: Epaye.RetrievedStartDates                  => j.copy(sessionId = sessionId)
      case j: Epaye.RetrievedAffordableQuotes            => j.copy(sessionId = sessionId)
      case j: Epaye.ChosenPaymentPlan                    => j.copy(sessionId = sessionId)
      case j: Epaye.CheckedPaymentPlan                   => j.copy(sessionId = sessionId)
      case j: Epaye.EnteredDetailsAboutBankAccount       => j.copy(sessionId = sessionId)
      case j: Epaye.EnteredDirectDebitDetails            => j.copy(sessionId = sessionId)
      case j: Epaye.ConfirmedDirectDebitDetails          => j.copy(sessionId = sessionId)
      case j: Epaye.AgreedTermsAndConditions             => j.copy(sessionId = sessionId)
      case j: Epaye.SelectedEmailToBeVerified            => j.copy(sessionId = sessionId)
      case j: Epaye.EmailVerificationComplete            => j.copy(sessionId = sessionId)
      case j: Epaye.SubmittedArrangement                 => j.copy(sessionId = sessionId)

      case j: Vat.Started                                => j.copy(sessionId = sessionId)
      case j: Vat.ComputedTaxId                          => j.copy(sessionId = sessionId)
      case j: Vat.EligibilityChecked                     => j.copy(sessionId = sessionId)
      case j: Vat.ObtainedWhyCannotPayInFullAnswers      => j.copy(sessionId = sessionId)
      case j: Vat.AnsweredCanPayUpfront                  => j.copy(sessionId = sessionId)
      case j: Vat.EnteredUpfrontPaymentAmount            => j.copy(sessionId = sessionId)
      case j: Vat.RetrievedExtremeDates                  => j.copy(sessionId = sessionId)
      case j: Vat.RetrievedAffordabilityResult           => j.copy(sessionId = sessionId)
      case j: Vat.ObtainedCanPayWithinSixMonthsAnswers   => j.copy(sessionId = sessionId)
      case j: Vat.StartedPegaCase                        => j.copy(sessionId = sessionId)
      case j: Vat.EnteredMonthlyPaymentAmount            => j.copy(sessionId = sessionId)
      case j: Vat.EnteredDayOfMonth                      => j.copy(sessionId = sessionId)
      case j: Vat.RetrievedStartDates                    => j.copy(sessionId = sessionId)
      case j: Vat.RetrievedAffordableQuotes              => j.copy(sessionId = sessionId)
      case j: Vat.ChosenPaymentPlan                      => j.copy(sessionId = sessionId)
      case j: Vat.CheckedPaymentPlan                     => j.copy(sessionId = sessionId)
      case j: Vat.EnteredDetailsAboutBankAccount         => j.copy(sessionId = sessionId)
      case j: Vat.EnteredDirectDebitDetails              => j.copy(sessionId = sessionId)
      case j: Vat.ConfirmedDirectDebitDetails            => j.copy(sessionId = sessionId)
      case j: Vat.AgreedTermsAndConditions               => j.copy(sessionId = sessionId)
      case j: Vat.SelectedEmailToBeVerified              => j.copy(sessionId = sessionId)
      case j: Vat.EmailVerificationComplete              => j.copy(sessionId = sessionId)
      case j: Vat.SubmittedArrangement                   => j.copy(sessionId = sessionId)

      case j: Sa.Started                                 => j.copy(sessionId = sessionId)
      case j: Sa.ComputedTaxId                           => j.copy(sessionId = sessionId)
      case j: Sa.EligibilityChecked                      => j.copy(sessionId = sessionId)
      case j: Sa.ObtainedWhyCannotPayInFullAnswers       => j.copy(sessionId = sessionId)
      case j: Sa.AnsweredCanPayUpfront                   => j.copy(sessionId = sessionId)
      case j: Sa.EnteredUpfrontPaymentAmount             => j.copy(sessionId = sessionId)
      case j: Sa.RetrievedExtremeDates                   => j.copy(sessionId = sessionId)
      case j: Sa.RetrievedAffordabilityResult            => j.copy(sessionId = sessionId)
      case j: Sa.ObtainedCanPayWithinSixMonthsAnswers    => j.copy(sessionId = sessionId)
      case j: Sa.StartedPegaCase                         => j.copy(sessionId = sessionId)
      case j: Sa.EnteredMonthlyPaymentAmount             => j.copy(sessionId = sessionId)
      case j: Sa.EnteredDayOfMonth                       => j.copy(sessionId = sessionId)
      case j: Sa.RetrievedStartDates                     => j.copy(sessionId = sessionId)
      case j: Sa.RetrievedAffordableQuotes               => j.copy(sessionId = sessionId)
      case j: Sa.ChosenPaymentPlan                       => j.copy(sessionId = sessionId)
      case j: Sa.CheckedPaymentPlan                      => j.copy(sessionId = sessionId)
      case j: Sa.EnteredDetailsAboutBankAccount          => j.copy(sessionId = sessionId)
      case j: Sa.EnteredDirectDebitDetails               => j.copy(sessionId = sessionId)
      case j: Sa.ConfirmedDirectDebitDetails             => j.copy(sessionId = sessionId)
      case j: Sa.AgreedTermsAndConditions                => j.copy(sessionId = sessionId)
      case j: Sa.SelectedEmailToBeVerified               => j.copy(sessionId = sessionId)
      case j: Sa.EmailVerificationComplete               => j.copy(sessionId = sessionId)
      case j: Sa.SubmittedArrangement                    => j.copy(sessionId = sessionId)

      case _: Sia                                        => sys.error("No other regime should be found here")
    }
  }

  private def getCaseId(journey: Journey): PegaCaseId = journey match {
    case j: Journey.AfterStartedPegaCase =>
      j.startCaseResponse.caseId

    case j: Journey.AfterCheckedPaymentPlan =>
      j.paymentPlanAnswers match {
        case p: PaymentPlanAnswers.PaymentPlanAfterAffordability => p.startCaseResponse.caseId
        case _: PaymentPlanAnswers.PaymentPlanNoAffordability    => sys.error("Trying to find case ID on non-affordability journey")
      }

    case other =>
      sys.error(s"Could not find PEGA case id in journey in state ${other.name}")
  }

  private def toPegaStartCaseRequest(journey: Journey): PegaStartCaseRequest = {
    val taxId = journey match {
      case j: Journey.AfterComputedTaxId => j.taxId
      case _                             => sys.error("Could not find tax id")
    }
    val taxIdType = taxId match {
      case _: EmpRef => "EMPREF"
      case _: Vrn    => "VRN"
      case _: SaUtr  => "SAUTR"
      case _: Nino   => "NINO"
    }
    val regime = journey.taxRegime match {
      case TaxRegime.Epaye => "PAYE"
      case TaxRegime.Vat   => "VAT"
      case TaxRegime.Sa    => "SA"
      case TaxRegime.Sia   => "SIA"
    }
    val eligibilityCheckResult = journey match {
      case j: Journey.AfterEligibilityChecked => j.eligibilityCheckResult
      case _                                  => sys.error("Could not find eligibility check result")
    }
    val whyCannotPayReasons = journey match {
      case j: Journey.AfterWhyCannotPayInFullAnswers =>
        j.whyCannotPayInFullAnswers match {
          case WhyCannotPayInFullAnswers.AnswerNotRequired =>
            sys.error("Expected WhyCannotPayInFull reasons but answer was not required")
          case WhyCannotPayInFullAnswers.WhyCannotPayInFull(reasons) => reasons
        }
      case _ => sys.error("Could not find why cannot pay in full answers")
    }
    val upfrontPaymentAmount = journey match {
      case j: Journey.AfterUpfrontPaymentAnswers =>
        j.upfrontPaymentAnswers match {
          case UpfrontPaymentAnswers.NoUpfrontPayment               => None
          case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) => Some(amount)
        }
      case _ => sys.error("Could not find upfront payment answers")
    }
    val extremeDatesResponse = journey match {
      case j: Journey.AfterExtremeDatesResponse => j.extremeDatesResponse
      case _                                    => sys.error("Could not find extreme dates result")
    }

    val canPayWithinSixMonths = journey match {
      case j: Journey.AfterCanPayWithinSixMonthsAnswers =>
        j.canPayWithinSixMonthsAnswers match {
          case CanPayWithinSixMonthsAnswers.AnswerNotRequired =>
            sys.error("Expected WhyCannotPayInFull reasons but answer was not required")
          case CanPayWithinSixMonthsAnswers.CanPayWithinSixMonths(value) => value
        }

      case _ => sys.error("Could not find why cannot pay in full answers")
    }

    val totalDebt = AmountInPence(eligibilityCheckResult.chargeTypeAssessment.map(_.debtTotalAmount.value.value).sum)

    val mapping = MDTPropertyMapping(
      eligibilityCheckResult.customerPostcodes,
      extremeDatesResponse.initialPaymentDate,
      ChannelIdentifiers.eSSTTP,
      eligibilityCheckResult.chargeTypeAssessment.flatMap(toDebtItemCharges),
      AccruedDebtInterest(calculateCumulativeInterest(eligibilityCheckResult)),
      upfrontPaymentAmount,
      PaymentPlanFrequencies.Monthly,
      whyCannotPayReasons,
      upfrontPaymentAmount.isDefined,
      canPayWithinSixMonths
    )

    val content = PegaStartCaseRequest.Content(
      taxId.value,
      taxIdType,
      regime,
      totalDebt,
      mapping
    )

    PegaStartCaseRequest("HMRC-Debt-Work-AffordAssess", "", "", content)
  }

  private def toDebtItemCharges(chargeTypeAssessment: ChargeTypeAssessment): List[DebtItemCharge] =
    chargeTypeAssessment.charges.map { charge: Charges =>
      DebtItemCharge(
        OutstandingDebtAmount(charge.outstandingAmount.value),
        charge.mainTrans,
        charge.subTrans,
        charge.isInterestBearingCharge,
        charge.useChargeReference,
        chargeTypeAssessment.chargeReference,
        charge.interestStartDate,
        DebtItemOriginalDueDate(charge.dueDate.value)
      )
    }

  private def calculateCumulativeInterest(eligibilityCheckResult: EligibilityCheckResult): AmountInPence =
    AmountInPence(
      eligibilityCheckResult.chargeTypeAssessment
        .flatMap(_.charges)
        .map(_.accruedInterest.value.value)
        .sum
    )

  private def toStartCaseResponse(response: PegaStartCaseResponse): StartCaseResponse = {
    val assignmentId =
      response.data.caseInfo.assignments
        .headOption
        .map(_.ID)
        .getOrElse(throw new Exception("Could not find assignment ID in PEGA start case response"))

    StartCaseResponse(PegaCaseId(response.ID), PegaAssigmentId(assignmentId))
  }

}
