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

//package journey
//
//import cats.syntax.eq._
//import com.google.inject.{Inject, Singleton}
//import essttp.journey.model.Journey.Epaye
//import essttp.journey.model.Journey.Stages.{AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount}
//import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
//import essttp.rootmodel.SelectedPlan
//import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
//import essttp.utils.Errors
//import io.scalaland.chimney.dsl.TransformerOps
//import play.api.mvc.{Action, ControllerComponents, Request}
//import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
//
//import scala.concurrent.{ExecutionContext, Future}
//
//@Singleton
//class UpdateSelectedPaymentPlanController @Inject() (
//                                        journeyService: JourneyService,
//                                        cc:             ControllerComponents
//                                      )(implicit exec: ExecutionContext) extends BackendController(cc) {
//
//  def updateSelectedPaymentPlan(journeyId: JourneyId): Action[SelectedPlan] = Action.async(parse.json[SelectedPlan]) { implicit request =>
//    for {
//      journey <- journeyService.get(journeyId)
//      _ <- journey match {
//        case j: Journey.Stages.EnteredUpfrontPaymentAmount => updateJourneyWithNewValue(Right(j), request.body)
//        case j: Journey.Stages.AnsweredCanPayUpfront       => updateJourneyWithNewValue(Left(j), request.body)
//        case j: Journey.AfterExtremeDatesResponse          => updateJourneyWithExistingValue(j, request.body)
//        case j: Journey.BeforeUpfrontPaymentAnswers        => Errors.throwBadRequestExceptionF(s"UpdateExtremeDatesResponse update is not possible in that state: [${j.stage}]")
//      }
//    } yield Ok
//  }
//
//  private def updateJourneyWithNewValue(
//                                                     journey: Either[AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount],
//                                                     extremeDatesResponse: ExtremeDatesResponse
//                                                   )(implicit request: Request[_]): Future[Unit] = {
//    val newJourney: Journey.Epaye.RetrievedExtremeDates = journey match {
//      case Left(j: Epaye.AnsweredCanPayUpfront)        => j.into[Journey.Epaye.RetrievedExtremeDates]
//        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
//        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
//        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
//        .transform
//      case Right(j: Epaye.EnteredUpfrontPaymentAmount) => j.into[Journey.Epaye.RetrievedExtremeDates]
//        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
//        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
//        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
//        .transform
//    }
//    journeyService.upsert(newJourney)
//  }
//
//  private def updateJourneyWithExistingValue(
//                                                          journey: Journey.AfterExtremeDatesResponse,
//                                                          extremeDatesResponse: ExtremeDatesResponse
//                                                        )(implicit request: Request[_]): Future[Unit] = {
//
//    if (journey.extremeDatesResponse === extremeDatesResponse) {
//      JourneyLogger.info("Nothing to update, ExtremeDatesResponse is the same as the existing one in journey.")
//      Future.successful(())
//    } else {
//      val newJourney: Journey.AfterExtremeDatesResponse = journey match {
//        case j: Journey.Epaye.RetrievedExtremeDates        => j.copy(extremeDatesResponse = extremeDatesResponse)
//        case j: Journey.Epaye.RetrievedAffordabilityResult =>
//          j.into[Journey.Epaye.RetrievedExtremeDates]
//            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
//            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
//            .transform
//        case j: Journey.Epaye.EnteredMonthlyPaymentAmount  =>
//          j.into[Journey.Epaye.RetrievedExtremeDates]
//            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
//            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
//            .transform
//        case j: Journey.Epaye.EnteredDayOfMonth            =>
//          j.into[Journey.Epaye.RetrievedExtremeDates]
//            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
//            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
//            .transform
//      }
//      journeyService.upsert(newJourney)
//    }
//  }
//}