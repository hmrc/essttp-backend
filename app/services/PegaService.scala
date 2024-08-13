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
import essttp.journey.model.{CanPayWithinSixMonthsAnswers, Journey, JourneyId, UpfrontPaymentAnswers, WhyCannotPayInFullAnswers}
import essttp.rootmodel.{AmountInPence, EmpRef, SaUtr, TaxRegime, Vrn}
import essttp.rootmodel.pega.{PegaAssigmentId, PegaCaseId, StartCaseResponse}
import essttp.rootmodel.ttp.PaymentPlanFrequencies
import essttp.rootmodel.ttp.affordablequotes.{AccruedDebtInterest, ChannelIdentifiers, DebtItemCharge, DebtItemOriginalDueDate, OutstandingDebtAmount}
import essttp.rootmodel.ttp.eligibility.{ChargeTypeAssessment, Charges, EligibilityCheckResult}
import essttp.utils.RequestSupport.hc
import models.pega.PegaStartCaseRequest.MDTPropertyMapping
import models.pega.{PegaStartCaseRequest, PegaStartCaseResponse}
import play.api.mvc.Request

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PegaService @Inject() (pegaConnector: PegaConnector, journeyService: JourneyService)(implicit ec: ExecutionContext) {

  def startCase(journeyId: JourneyId)(implicit r: Request[_]): Future[StartCaseResponse] = {
    for {
      journey <- journeyService.get(journeyId)
      request = toPegaStartCaseRequest(journey)
      token <- pegaConnector.getToken()
      response <- pegaConnector.startCase(request, token)
    } yield toStartCaseResponse(response)
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
    }
    val regime = journey.taxRegime match {
      case TaxRegime.Epaye => "PAYE"
      case TaxRegime.Vat   => "VAT"
      case TaxRegime.Sa    => "SA"
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
