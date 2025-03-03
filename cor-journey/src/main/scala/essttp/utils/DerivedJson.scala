/*
 * Copyright 2025 HM Revenue & Customs
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

package essttp.utils

import com.evolution.playjson.circe.{CirceToPlayConversions, PlayToCirceConversions}
import essttp.utils.Givens.jsValueCanEqual
import io.circe.Codec
import play.api.libs.json.*

object DerivedJson {

  object Circe {

    def format[A](codec: Codec.AsObject[A]): OFormat[A] =
      new OFormat {
        override def writes(o: A): JsObject =
          CirceToPlayConversions.writesFromEncoder(codec).writes(o).as[JsObject]

        override def reads(json: JsValue): JsResult[A] =
          CirceToPlayConversions.readsFromDecoder(codec).reads(json)

      }

    given formatToCodec[A](using format: Format[A]): Codec[A] =
      Codec.from(PlayToCirceConversions.decoderFromReads(format), PlayToCirceConversions.encoderFromWrites(format))

  }
}
