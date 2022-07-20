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
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.bank.{TypeOfBankAccount, TypesOfBankAccount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateTypeOfBankAccountController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateTypeOfBankAccount(journeyId: JourneyId): Action[TypeOfBankAccount] = Action.async(parse.json[TypeOfBankAccount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeCheckedPaymentPlan  => Errors.throwBadRequestExceptionF(s"UpdateTypeOfBankAccount is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.CheckedPaymentPlan => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterChosenTypeOfBankAccount => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update TypeOfBankAccount when journey is in completed state")
        }
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:           Journey.Stages.CheckedPaymentPlan,
      typeOfBankAccount: TypeOfBankAccount
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterChosenTypeOfBankAccount = journey match {
      case j: Journey.Epaye.CheckedPaymentPlan =>
        j.into[Journey.Epaye.ChosenTypeOfBankAccount]
          .withFieldConst(_.stage, determineStage(typeOfBankAccount))
          .withFieldConst(_.typeOfBankAccount, typeOfBankAccount)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:           Journey.AfterChosenTypeOfBankAccount,
      typeOfBankAccount: TypeOfBankAccount
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.typeOfBankAccount === typeOfBankAccount) {
      JourneyLogger.info("Chosen type of bank account hasn't changed, nothing to update")
      Future.successful(())
    } else {
      val updatedJourney = journey match {
        case j: Journey.Epaye.ChosenTypeOfBankAccount =>
          j.copy(
            typeOfBankAccount = typeOfBankAccount,
            stage             = determineStage(typeOfBankAccount)
          )
        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.ChosenTypeOfBankAccount]
            .withFieldConst(_.stage, determineStage(typeOfBankAccount))
            .transform
        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.ChosenTypeOfBankAccount]
            .withFieldConst(_.stage, determineStage(typeOfBankAccount))
            .transform
        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.ChosenTypeOfBankAccount]
            .withFieldConst(_.stage, determineStage(typeOfBankAccount))
            .transform
        case _: Journey.Epaye.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update TypeOfBankAccount when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

  }

  private def determineStage(typeOfBankAccount: TypeOfBankAccount): Stage.AfterChosenTypeOfBankAccount =
    typeOfBankAccount match {
      case TypesOfBankAccount.Business => Stage.AfterChosenTypeOfBankAccount.Business
      case TypesOfBankAccount.Personal => Stage.AfterChosenTypeOfBankAccount.Personal
    }

}

