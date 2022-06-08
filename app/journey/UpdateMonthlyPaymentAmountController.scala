package journey

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.Journey.{Epaye, Stages}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.{MonthlyPaymentAmount, UpfrontPaymentAmount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateMonthlyPaymentAmountController @Inject()(
                                                      journeyService: JourneyService,
                                                      cc: ControllerComponents
                                                    )(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateMonthlyPaymentAmount(journeyId: JourneyId): Action[MonthlyPaymentAmount] = Action.async(parse.json[MonthlyPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAnsweredCanPayUpfront      => Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage}]")
//        case j: Journey.Stages.AnsweredCanPayUpfront     => updateJourneyWithNewValue(j, request.body)
//        case j: Journey.AfterEnteredMonthlyPaymentAmount => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(journey: ???, monthlyPaymentAmount: MonthlyPaymentAmount) = ???
  private def updateJourneyWithExistingValue(journey: ???, monthlyPaymentAmount: MonthlyPaymentAmount) = ???