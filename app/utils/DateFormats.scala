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

package utils

import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success, Try}

object DateFormats {

  /**
   * This formatter is equivalent to the previous Joda version used from org.joda.time.format.ISODateTimeFormat.dateTime
   */
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

  implicit val localDateTimeRead: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json match {
        case JsString(s) =>
          Try(LocalDateTime.parse(s, dateTimeFormatter)) match {
            case Failure(e) => JsError(
              s"Could not parse $s as a LocalDateTime with format '${dateTimeFormatter.toString}' : ${e.getMessage}"
            )
            case Success(v) => JsSuccess(v)
          }
        case _ => JsError(s"Expected value to be a string, was actually $json")
      }
  }

  implicit val localDateTimeWrite: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(dateTime: LocalDateTime): JsValue = JsString(dateTimeFormatter.format(dateTime.atZone(ZoneOffset.UTC)))
  }

  implicit val localDateTimeFormat: Format[LocalDateTime] = Format(localDateTimeRead, localDateTimeWrite)
}

