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

@Singleton()
class DatesController @Inject() (
    actions:             Actions,
    startDatesService:   StartDatesService,
    extremeDatesService: ExtremeDatesService,
    cc:                  ControllerComponents
) extends BackendController(cc) {

  def startDates(): Action[StartDatesRequest] = actions.authenticatedAction(parse.json[StartDatesRequest]) { implicit request =>
    Ok(Json.toJson(startDatesService.calculateStartDates(request.body)))
  }

  def extremeDates(): Action[ExtremeDatesRequest] = actions.authenticatedAction(parse.json[ExtremeDatesRequest]) { implicit request =>
    Ok(Json.toJson(extremeDatesService.calculateExtremeDates(request.body)))
  }

}
