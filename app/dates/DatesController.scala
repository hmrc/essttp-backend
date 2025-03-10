/*
 * Copyright 2023 HM Revenue & Customs
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

package dates

import action.Actions
import essttp.rootmodel.dates.extremedates.ExtremeDatesRequest
import essttp.rootmodel.dates.startdates.StartDatesRequest
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DatesController @Inject() (
  actions:             Actions,
  startDatesService:   StartDatesService,
  extremeDatesService: ExtremeDatesService,
  cc:                  ControllerComponents
)(using ExecutionContext)
    extends BackendController(cc) {

  val startDates: Action[StartDatesRequest] =
    actions.authenticatedAction(parse.json[StartDatesRequest]).async { implicit request =>
      startDatesService.calculateStartDates(request.body).map { result =>
        Ok(Json.toJson(result))
      }
    }

  val extremeDates: Action[ExtremeDatesRequest] =
    actions.authenticatedAction(parse.json[ExtremeDatesRequest]).async { implicit request =>
      extremeDatesService.calculateExtremeDates(request.body).map { result =>
        Ok(Json.toJson(result))
      }
    }

}
