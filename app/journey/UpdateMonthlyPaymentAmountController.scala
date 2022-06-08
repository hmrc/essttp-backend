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

package journey

import com.google.inject.{Inject, Singleton}
import essttp.journey.model.Journey.Epaye
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.{AmountInPence, MonthlyPaymentAmount, UpfrontPaymentAmount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateMonthlyPaymentAmountController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateMonthlyPaymentAmount(journeyId: JourneyId): Action[MonthlyPaymentAmount] = Action.async(parse.json[MonthlyPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAnsweredCanPayUpfront      => Errors.throwBadRequestExceptionF(s"UpdateMonthlyPaymentAmount update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.AfterUpfrontPaymentAnswers       => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredMonthlyPaymentAmount => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private val noUpfrontPayment: UpfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(0))

  private def updateJourneyWithNewValue(
      journey:              Journey.AfterAnsweredCanPayUpfront,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {
    //andy/pawel, this is where chimney is not happy - work around is to set to 0 for AnsweredCanPayUpfront
    // - maybe add a conditional for checking they aren't in state yes-payupfront but no upfrontAmount?
    journey match {
      case j: Epaye.EnteredUpfrontPaymentAmount =>
        val newJourney: Epaye.EnteredMonthlyPaymentAmount =
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        journeyService.upsert(newJourney)
      case j: Epaye.AnsweredCanPayUpfront =>
        val newJourney: Epaye.EnteredMonthlyPaymentAmount =
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.upfrontPaymentAmount, noUpfrontPayment)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        journeyService.upsert(newJourney)
    }
  }

  private def updateJourneyWithExistingValue(
      journey:              Journey.AfterEnteredMonthlyPaymentAmount,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {
    val updatedJourney: Journey = journey match {
      case j: Epaye.EnteredMonthlyPaymentAmount => j.copy(monthlyPaymentAmount = monthlyPaymentAmount)
      case j: Epaye.EnteredDayOfMonth =>
        j.into[Journey.Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Epaye.EnteredInstalmentAmount =>
        j.into[Journey.Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Epaye.HasSelectedPlan =>
        j.into[Journey.Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
    }
    journeyService.upsert(updatedJourney)
  }

}
