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
import essttp.journey.model.Journey.Stages.{AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount}
import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.MonthlyPaymentAmount
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
        case j: Journey.BeforeAnsweredCanPayUpfront        => Errors.throwBadRequestExceptionF(s"UpdateMonthlyPaymentAmount update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.EnteredUpfrontPaymentAmount => updateJourneyWithNewValue(Left(j), request.body)
        case j: Journey.Stages.AnsweredCanPayUpfront       => updateJourneyWithNewValue(Right(j), request.body)
        case j: Journey.AfterEnteredMonthlyPaymentAmount   => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:              Either[EnteredUpfrontPaymentAmount, AnsweredCanPayUpfront],
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Epaye.EnteredMonthlyPaymentAmount = journey match {
      case Left(j: Epaye.EnteredUpfrontPaymentAmount) =>
        j.into[Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case Right(j: Epaye.AnsweredCanPayUpfront) =>
        j.into[Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:              Journey.AfterEnteredMonthlyPaymentAmount,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {
    val updatedJourney: Journey = journey match {
      case j: Epaye.EnteredMonthlyPaymentAmount => j.copy(monthlyPaymentAmount = monthlyPaymentAmount)
      //here add stages after EnteredMonthlyPaymentAmount, using chimney
    }
    journeyService.upsert(updatedJourney)
  }

}
