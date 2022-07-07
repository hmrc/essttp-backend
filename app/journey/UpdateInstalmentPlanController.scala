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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.ttp.affordablequotes.PaymentPlan
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateInstalmentPlanController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateChosenInstalmentPlan(journeyId: JourneyId): Action[PaymentPlan] = Action.async(parse.json[PaymentPlan]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAffordableQuotesResponse   => Errors.throwBadRequestExceptionF(s"UpdateSelectedPaymentPlan is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.RetrievedAffordableQuotes => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterSelectedPaymentPlan         => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:     Journey.Stages.RetrievedAffordableQuotes,
      paymentPlan: PaymentPlan
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterSelectedPaymentPlan = journey match {
      case j: Journey.Epaye.RetrievedAffordableQuotes =>
        j.into[Journey.Epaye.ChosenPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
          .withFieldConst(_.selectedPaymentPlan, paymentPlan)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:     Journey.AfterSelectedPaymentPlan,
      paymentPlan: PaymentPlan
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.selectedPaymentPlan === paymentPlan) {
      JourneyLogger.info("Nothing to update, selected PaymentPlan is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val newJourney: Journey.AfterSelectedPaymentPlan = journey match {
        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.copy(selectedPaymentPlan = paymentPlan)
        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.ChosenPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
            .withFieldConst(_.selectedPaymentPlan, paymentPlan)
            .transform
        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.ChosenPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
            .withFieldConst(_.selectedPaymentPlan, paymentPlan)
            .transform
        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.ChosenPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
            .withFieldConst(_.selectedPaymentPlan, paymentPlan)
            .transform
        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.ChosenPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
            .withFieldConst(_.selectedPaymentPlan, paymentPlan)
            .transform
      }

      journeyService.upsert(newJourney)
    }
  }
}
