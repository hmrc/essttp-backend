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

import cats.Eval
import essttp.utils.Givens.jsValueCanEqual
import io.circe.{Codec, Decoder, Encoder, Json as CirceJson}
import play.api.libs.json.*
import play.api.libs.json as PlayJson

object DerivedJson {

  object Circe {

    private type Field[T] = (String, T)

    private def evalZero[T]: Eval[Vector[T]] = Eval.now(Vector.empty[T])

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    private def circeToPlay(circeJson: CirceJson): PlayJson.JsValue = {
      def inner(json: Eval[CirceJson]): Eval[PlayJson.JsValue] =
        json.flatMap(
          _.fold(
            jsonNull = Eval.now(PlayJson.JsNull),
            jsonBoolean = b => Eval.now(PlayJson.JsBoolean(b)),
            jsonNumber =
              n => Eval.now(n.toBigDecimal.map(PlayJson.JsNumber.apply).getOrElse(PlayJson.JsNumber(n.toDouble))),
            jsonString = s => Eval.now(PlayJson.JsString(s)),
            jsonArray = as =>
              Eval
                .defer {
                  as.foldLeft(evalZero[PlayJson.JsValue])((acc, c) => inner(Eval.now(c)).flatMap(p => acc.map(_ :+ p)))
                }
                .map(PlayJson.JsArray),
            jsonObject = obj =>
              Eval
                .defer {
                  obj.toIterable.foldLeft(evalZero[Field[PlayJson.JsValue]]) { case (acc, (k, c)) =>
                    inner(Eval.now(c)).flatMap(p => acc.map(_ :+ (k -> p)))
                  }
                }
                .map(PlayJson.JsObject)
          )
        )

      inner(Eval.now(circeJson)).value
    }

    private def playToCirce(value: PlayJson.JsValue): CirceJson = {
      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def inner(value: Eval[PlayJson.JsValue]): Eval[CirceJson] =
        value.flatMap {
          case PlayJson.JsNull =>
            Eval.now(CirceJson.Null)

          case PlayJson.JsTrue =>
            Eval.now(CirceJson.True)

          case PlayJson.JsFalse =>
            Eval.now(CirceJson.False)

          case PlayJson.JsNumber(value) =>
            Eval.now(CirceJson.fromBigDecimal(value))

          case PlayJson.JsString(value) =>
            Eval.now(CirceJson.fromString(value))

          case PlayJson.JsArray(values) =>
            if (values.isEmpty)
              Eval.now(CirceJson.arr())
            else
              Eval
                .defer {
                  values.foldLeft(evalZero[CirceJson])((acc, p) => inner(Eval.now(p)).flatMap(c => acc.map(_ :+ c)))
                }
                .map(CirceJson.fromValues)

          case PlayJson.JsObject(value) =>
            Eval
              .defer {
                value.view.foldLeft(evalZero[Field[CirceJson]]) { case (acc, (k, p)) =>
                  inner(Eval.now(p)).flatMap(c => acc.map(_ :+ (k -> c)))
                }
              }
              .map(CirceJson.fromFields)
        }

      inner(Eval.now(value)).value
    }

    def format[A](codec: Codec.AsObject[A]): OFormat[A] =
      new OFormat[A] {
        override def writes(o: A): JsObject =
          circeToPlay(CirceJson.fromJsonObject(codec.encodeObject(o))).as[JsObject]

        override def reads(json: JsValue): JsResult[A] =
          codec.decodeJson(playToCirce(json)) match {
            case Right(success) => JsSuccess(success)
            case Left(err)      => JsError(err.message)
          }
      }

    given formatToCodec[A](using format: Format[A]): Codec[A] =
      Codec.from(
        Decoder.decodeJson.emap { circeJson =>
          circeToPlay(circeJson).validate[A] match {
            case JsSuccess(value, _) =>
              Right(value)
            case JsError(errors)     =>
              Left(errors.flatMap { case (_, errs) => errs }.map(_.message).mkString(", "))
          }
        },
        Encoder.instance { value =>
          playToCirce(format.writes(value))
        }
      )

  }
}
