package journey

import com.google.inject.{Inject, Singleton}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.CanPayUpfront
import essttp.utils.Errors
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayUpfrontController @Inject()(
                                               journeyService: JourneyService,
                                               cc: ControllerComponents
                                             )(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateCanPayUpfront(journeyId: JourneyId): Action[CanPayUpfront] = Action.async(parse.json[CanPayUpfront]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- updateJourney(journey, request.body)
    } yield Ok
  }

  private def updateJourney(journey: Journey, canPayUpfront: CanPayUpfront)(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.AfterStarted =>
        val newJourney: Journey.Epaye.AfterComputedTaxIds = j
          .into[Journey.Epaye.AfterComputedTaxIds]
          .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
          .withFieldConst(_.taxId, empRef)
          .transform
        journeyService.upsert(newJourney)
      case j: Journey.HasTaxId if j.taxId == empRef =>
        JourneyLogger.info("Nothing to update, journey has already updated tax id.")
        Future.successful(())
      case j: Journey.HasTaxId if j.taxId != empRef =>
        Errors.notImplemented("Incorrect taxId type. For Epaye it must be Aor")
    }
  }

}
