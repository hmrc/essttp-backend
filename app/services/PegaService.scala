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
import essttp.journey.model.*
import essttp.rootmodel.*
import essttp.rootmodel.epaye.{TaxOfficeNumber, TaxOfficeReference}
import essttp.rootmodel.pega.{GetCaseResponse, PegaCaseId, StartCaseResponse}
import essttp.rootmodel.ttp.PaymentPlanFrequencies
import essttp.rootmodel.ttp.affordablequotes.*
import essttp.rootmodel.ttp.eligibility.{ChargeReference, ChargeTypeAssessment, Charges, EligibilityCheckResult}
import essttp.utils.RequestSupport.hc
import essttp.utils.{Errors, RequestSupport}
import models.pega.PegaGetCaseResponse.{PegaCollection, PegaInstalment}
import models.pega.PegaStartCaseRequest.{MDTPropertyMapping, UnableToPayReason}
import models.pega.{PegaGetCaseResponse, PegaStartCaseRequest, PegaStartCaseResponse, PegaTokenManager}
import org.apache.commons.lang3.StringUtils
import play.api.mvc.Request
import repository.JourneyByTaxIdRepo
import repository.JourneyByTaxIdRepo.JourneyWithTaxId
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.{Clock, Instant}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PegaService @Inject() (
  pegaConnector:          PegaConnector,
  journeyByTaxIdRepo:     JourneyByTaxIdRepo,
  journeyService:         JourneyService,
  tokenManager:           PegaTokenManager,
  correlationIdGenerator: PegaCorrelationIdGenerator
)(using ExecutionContext) {

  def startCase(journeyId: JourneyId, recalculationNeeded: Boolean)(using Request[?]): Future[StartCaseResponse] =
    for {
      journey          <- journeyService.get(journeyId)
      request           = toPegaStartCaseRequest(journey, recalculationNeeded)
      pegaCorrelationId = correlationIdGenerator.nextCorrelationId()
      response         <- doPegaCall(pegaConnector.startCase(request, _, pegaCorrelationId))
    } yield toStartCaseResponse(response, pegaCorrelationId)

  def getCase(journeyId: JourneyId)(using Request[?]): Future[GetCaseResponse] =
    for {
      journey          <- journeyService.get(journeyId)
      caseId            = getCaseId(journey)
      pegaCorrelationId = correlationIdGenerator.nextCorrelationId()
      response         <- doPegaCall(pegaConnector.getCase(caseId, _, pegaCorrelationId))
    } yield toGetCaseResponse(response, pegaCorrelationId)

  private def doPegaCall[A](callAPI: String => Future[A])(using HeaderCarrier): Future[A] =
    tokenManager.getToken().flatMap { originalToken =>
      callAPI(originalToken).recoverWith {
        case u: UpstreamErrorResponse if u.statusCode == 401 =>
          // Handle 401 Unauthorized, refresh token and retry once
          tokenManager.fetchNewToken().flatMap { newToken =>
            callAPI(newToken)
          }
      }
    }

  def saveJourney(journeyId: JourneyId)(using Request[?]): Future[Unit] =
    journeyService.get(journeyId).flatMap {
      case _: JourneyStage.BeforeComputedTaxId =>
        Errors.throwBadRequestExceptionF("Cannot save journey when no tax ID has been computed yet")
      case j: JourneyStage.AfterComputedTaxId  =>
        val now = Instant.now(Clock.systemUTC())
        journeyByTaxIdRepo.upsert(JourneyWithTaxId(j.taxId, j, now))
    }

  def recreateSession(taxRegime: TaxRegime, enrolments: Enrolments)(using Request[?]): Future[Journey] = {
    def toEmpRef(taxOfficeNumber: TaxOfficeNumber, taxOfficeReference: TaxOfficeReference): EmpRef =
      EmpRef(s"${taxOfficeNumber.value}${taxOfficeReference.value}")

    val enrolmentResult: EnrolmentDefResult[TaxId] = taxRegime match {
      case TaxRegime.Epaye =>
        EnrolmentDef.Epaye.findEnrolmentValues(enrolments).map(toEmpRef.tupled)
      case TaxRegime.Vat   => EnrolmentDef.Vat.findEnrolmentValues(enrolments)
      case TaxRegime.Sa    => EnrolmentDef.Sa.findEnrolmentValues(enrolments)
      case TaxRegime.Simp  => sys.error("SIMP should have never ended up here")
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

  private def updateJourneyWithSessionId(journey: Journey, sessionId: SessionId): Journey =
    journey match {
      case j: Journey.Started                              => j.copy(sessionId = sessionId)
      case j: Journey.ComputedTaxId                        => j.copy(sessionId = sessionId)
      case j: Journey.EligibilityChecked                   => j.copy(sessionId = sessionId)
      case j: Journey.ObtainedWhyCannotPayInFullAnswers    => j.copy(sessionId = sessionId)
      case j: Journey.AnsweredCanPayUpfront                => j.copy(sessionId = sessionId)
      case j: Journey.EnteredUpfrontPaymentAmount          => j.copy(sessionId = sessionId)
      case j: Journey.RetrievedExtremeDates                => j.copy(sessionId = sessionId)
      case j: Journey.RetrievedAffordabilityResult         => j.copy(sessionId = sessionId)
      case j: Journey.ObtainedCanPayWithinSixMonthsAnswers => j.copy(sessionId = sessionId)
      case j: Journey.StartedPegaCase                      => j.copy(sessionId = sessionId)
      case j: Journey.EnteredMonthlyPaymentAmount          => j.copy(sessionId = sessionId)
      case j: Journey.EnteredDayOfMonth                    => j.copy(sessionId = sessionId)
      case j: Journey.RetrievedStartDates                  => j.copy(sessionId = sessionId)
      case j: Journey.RetrievedAffordableQuotes            => j.copy(sessionId = sessionId)
      case j: Journey.ChosenPaymentPlan                    => j.copy(sessionId = sessionId)
      case j: Journey.CheckedPaymentPlan                   => j.copy(sessionId = sessionId)
      case j: Journey.EnteredCanYouSetUpDirectDebit        => j.copy(sessionId = sessionId)
      case j: Journey.EnteredDirectDebitDetails            => j.copy(sessionId = sessionId)
      case j: Journey.ConfirmedDirectDebitDetails          => j.copy(sessionId = sessionId)
      case j: Journey.AgreedTermsAndConditions             => j.copy(sessionId = sessionId)
      case j: Journey.SelectedEmailToBeVerified            => j.copy(sessionId = sessionId)
      case j: Journey.EmailVerificationComplete            => j.copy(sessionId = sessionId)
      case j: Journey.SubmittedArrangement                 => j.copy(sessionId = sessionId)
    }

  private def getCaseId(journey: Journey): PegaCaseId = journey match {
    case j: JourneyStage.AfterStartedPegaCase =>
      j.startCaseResponse.caseId

    case j: JourneyStage.AfterCheckedPaymentPlan =>
      j.paymentPlanAnswers match {
        case p: PaymentPlanAnswers.PaymentPlanAfterAffordability => p.startCaseResponse.caseId
        case _: PaymentPlanAnswers.PaymentPlanNoAffordability    =>
          sys.error("Trying to find case ID on non-affordability journey")
      }

    case other =>
      sys.error(s"Could not find PEGA case id in journey in state ${other.name}")
  }

  private def toPegaStartCaseRequest(journey: Journey, recalculationNeeded: Boolean): PegaStartCaseRequest = {
    val taxId                  = journey match {
      case j: JourneyStage.AfterComputedTaxId => j.taxId
      case _                                  => sys.error("Could not find tax id")
    }
    val taxIdType              = taxId match {
      case _: EmpRef => "EMPREF"
      case _: Vrn    => "VRN"
      case _: SaUtr  => "SAUTR"
      case _: Nino   => "NINO"
    }
    val regime                 = journey.taxRegime match {
      case TaxRegime.Epaye => "PAYE"
      case TaxRegime.Vat   => "VAT"
      case TaxRegime.Sa    => "SA"
      case TaxRegime.Simp  => "SIMP"
    }
    val eligibilityCheckResult = journey match {
      case j: JourneyStage.AfterEligibilityChecked => j.eligibilityCheckResult
      case _                                       => sys.error("Could not find eligibility check result")
    }
    val unableToPayReasons     = journey match {
      case j: JourneyStage.AfterWhyCannotPayInFullAnswers =>
        j.whyCannotPayInFullAnswers match {
          case WhyCannotPayInFullAnswers.AnswerNotRequired           =>
            sys.error("Expected WhyCannotPayInFull reasons but answer was not required")
          case WhyCannotPayInFullAnswers.WhyCannotPayInFull(reasons) => reasons.map(toUnableToPayReason)
        }
      case _                                              => sys.error("Could not find why cannot pay in full answers")
    }
    val upfrontPaymentAmount   = journey match {
      case j: JourneyStage.AfterUpfrontPaymentAnswers =>
        j.upfrontPaymentAnswers match {
          case UpfrontPaymentAnswers.NoUpfrontPayment               => None
          case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) => Some(amount)
        }
      case _                                          => sys.error("Could not find upfront payment answers")
    }
    val extremeDatesResponse   = journey match {
      case j: JourneyStage.AfterExtremeDatesResponse => j.extremeDatesResponse
      case _                                         => sys.error("Could not find extreme dates result")
    }

    val totalDebt = AmountInPence(eligibilityCheckResult.chargeTypeAssessment.map(_.debtTotalAmount.value.value).sum)

    val mapping = MDTPropertyMapping(
      eligibilityCheckResult.customerPostcodes,
      extremeDatesResponse.initialPaymentDate,
      ChannelIdentifiers.eSSTTP,
      eligibilityCheckResult.chargeTypeAssessment.flatMap(toDebtItemCharges),
      AccruedDebtInterest(calculateCumulativeInterest(eligibilityCheckResult)),
      upfrontPaymentAmount,
      PaymentPlanFrequencies.Monthly
    )

    val aa = PegaStartCaseRequest.AA(
      totalDebt,
      upfrontPaymentAmount.isDefined,
      unableToPayReasons,
      mapping
    )

    val pegaCaseId = journey.pegaCaseId

    val content = PegaStartCaseRequest.Content(
      taxId.value,
      taxIdType,
      regime,
      pegaCaseId,
      pegaCaseId.map(_ => recalculationNeeded),
      aa
    )

    PegaStartCaseRequest("HMRC-Debt-Work-AffordAssess", content)
  }

  private def toUnableToPayReason(cannotPayReason: CannotPayReason): UnableToPayReason = {
    val code = cannotPayReason match {
      case CannotPayReason.UnexpectedReductionOfIncome       => "UTPR-1"
      case CannotPayReason.UnexpectedIncreaseInSpending      => "UTPR-2"
      case CannotPayReason.LostOrReducedAbilityToEarnOrTrade => "UTPR-3"
      case CannotPayReason.NationalOrLocalDisaster           => "UTPR-4"
      case CannotPayReason.ChangeToPersonalCircumstances     => "UTPR-5"
      case CannotPayReason.NoMoneySetAside                   => "UTPR-6"
      case CannotPayReason.WaitingForRefund                  => "UTPR-7"
      case CannotPayReason.Other                             => "UTPR-8"
    }
    UnableToPayReason(code)
  }

  private def toDebtItemCharges(chargeTypeAssessment: ChargeTypeAssessment): List[DebtItemCharge] =
    chargeTypeAssessment.charges.map { (charge: Charges) =>
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

  private def toStartCaseResponse(response: PegaStartCaseResponse, pegaCorrelationId: String): StartCaseResponse =
    StartCaseResponse(PegaCaseId(response.ID), pegaCorrelationId)

  private def toGetCaseResponse(response: PegaGetCaseResponse, pegaCorrelationId: String): GetCaseResponse = {
    val paymentPlan =
      response.AA.paymentPlan
        .find(_.planSelected)
        .getOrElse(throw new Exception("Could not find selected payment plan in PEGA get case response"))

    val initialCollection = paymentPlan.collections.initialCollection.map(c =>
      InitialCollection(DueDate(c.dueDate), AmountDue(AmountInPence(c.amountDue)))
    )

    def toRegularCollection(pegaCollection: PegaCollection): RegularCollection =
      RegularCollection(DueDate(pegaCollection.dueDate), AmountDue(AmountInPence(pegaCollection.amountDue)))

    def toInstalment(pegaInstalment: PegaInstalment): Instalment =
      Instalment(
        InstalmentNumber(pegaInstalment.instalmentNumber),
        DueDate(pegaInstalment.dueDate),
        InterestAccrued(AmountInPence(pegaInstalment.instalmentInterestAccrued)),
        InstalmentBalance(AmountInPence(pegaInstalment.instalmentBalance)),
        ChargeReference(pegaInstalment.debtItemChargeId),
        AmountDue(AmountInPence(pegaInstalment.amountDue)),
        DebtItemOriginalDueDate(pegaInstalment.debtItemOriginalDueDate),
        pegaInstalment.expectedPayment.map(exp => ExpectedPayment(AmountInPence(exp)))
      )

    val expenditure =
      response.`AA`.expenditure
        .map(e => toCamelCase(e.pyLabel) -> toBigDecimal(e.amountValue))
        .toMap

    val income =
      response.`AA`.income
        .map(e => toCamelCase(e.pyLabel) -> toBigDecimal(e.amountValue))
        .toMap

    GetCaseResponse(
      DayOfMonth(response.AA.paymentDay),
      PaymentPlan(
        NumberOfInstalments(paymentPlan.numberOfInstalments),
        PlanDuration(paymentPlan.planDuration),
        TotalDebt(AmountInPence(paymentPlan.totalDebt)),
        TotalDebtIncludingInterest(AmountInPence(paymentPlan.totalDebtIncInt)),
        PlanInterest(AmountInPence(paymentPlan.planInterest)),
        Collection(initialCollection, paymentPlan.collections.regularCollections.map(toRegularCollection)),
        paymentPlan.instalments.map(toInstalment)
      ),
      expenditure,
      income,
      pegaCorrelationId
    )
  }

  private def toCamelCase(s: String): String =
    s.trim.split(" ").toList.filter(_.nonEmpty) match {
      case Nil           => ""
      case first :: rest =>
        StringUtils.uncapitalize(first) + rest.map(_.capitalize).mkString("")

    }

  private def toBigDecimal(s: String): BigDecimal = {
    val trimmed = s.trim.filter(c => c.isDigit || c == '.')
    if (trimmed.isEmpty) BigDecimal(0)
    else BigDecimal(trimmed)
  }

}

@Singleton
class PegaCorrelationIdGenerator {

  def nextCorrelationId(): String = UUID.randomUUID().toString

}
