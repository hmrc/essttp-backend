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

package journey

import action.Actions
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{Journey, JourneyId, JourneyStage}
import essttp.rootmodel.ttp.eligibility.AssessmentCategory
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAssessmentCategoryController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateAssessmentCategory(journeyId: JourneyId): Action[AssessmentCategory] =
    actions.authenticatedAction.async(parse.json[AssessmentCategory]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case _: JourneyStage.BeforeEligibilityChecked =>
                          Errors.throwBadRequestExceptionF(
                            "AssessmentCategory update is not possible in that state."
                          )

                        case j: Journey.EligibilityChecked =>
                          updateJourneyWithNewValue(j, request.body)

                        case j: JourneyStage.AfterAssessmentCategoryDetermined =>
                          j match {
                            case j: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update AssessmentCategory when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:            Journey.EligibilityChecked,
    assessmentCategory: AssessmentCategory
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.AssessmentCategoryDetermined]
        .withFieldConst(_.assessmentCategory, assessmentCategory)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:            JourneyStage.AfterAssessmentCategoryDetermined & Journey,
    assessmentCategory: AssessmentCategory
  )(using Request[?]): Future[Journey] =
    if (journey.assessmentCategory == assessmentCategory) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.AssessmentCategoryDetermined =>
          j.copy(assessmentCategory = assessmentCategory)

        case j: Journey.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.AnsweredCanPayUpfront =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EnteredUpfrontPaymentAmount =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.RetrievedExtremeDates =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.RetrievedAffordabilityResult =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.StartedPegaCase =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EnteredDayOfMonth =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.RetrievedStartDates =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.RetrievedAffordableQuotes =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.ChosenPaymentPlan =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.CheckedPaymentPlan =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.ChosenTypeOfBankAccount =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.AssessmentCategoryDetermined]
            .withFieldConst(_.assessmentCategory, assessmentCategory)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update WhyCannotPayInFullAnswers when journey is in completed state")
      }

      journeyService.upsert(newJourney)
    }

}
