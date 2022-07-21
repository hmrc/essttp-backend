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

package repository

import com.github.ghik.silencer.silent
import org.bson.codecs.Codec
import play.api.libs.json.OFormat
import shapeless.{:+:, CNil, Coproduct, Generic}
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.Codecs.SumCodecsBuilder

import scala.reflect.ClassTag

object CodecUtils {

  trait CoproductCodecsBuilder[A, C <: Coproduct] {
    def builder: SumCodecsBuilder[A]
  }

  object CoproductCodecsBuilder {

    implicit def cnilBuilder[A: ClassTag](implicit f: OFormat[A]): CoproductCodecsBuilder[A, CNil] =
      new CoproductCodecsBuilder[A, CNil] {
        def builder: SumCodecsBuilder[A] = Codecs.playFormatCodecsBuilder(f)
      }

    implicit def coproductBuilder[A, H <: A, T <: Coproduct](
        implicit
        tailBuilder: CoproductCodecsBuilder[A, T],
        classTag:    ClassTag[H]
    ): CoproductCodecsBuilder[A, H :+: T] =
      new CoproductCodecsBuilder[A, H :+: T] {
        def builder: SumCodecsBuilder[A] = tailBuilder.builder.forType[H]
      }
  }

  trait CoproductsCodecs[A] {
    def codecs: Seq[Codec[_]]
  }

  object CoproductsCodecs {

    @silent // for unused parameters warnings
    implicit def fromCoproductCodecsBuilder[A, C <: Coproduct](implicit
        gen: Generic.Aux[A, C],
                                                               builder: CoproductCodecsBuilder[A, C]
    ): CoproductsCodecs[A] =
      new CoproductsCodecs[A] {
        def codecs: Seq[Codec[_]] = builder.builder.build
      }
  }

  def coproductCodecs[T](implicit ev: CoproductsCodecs[T]): Seq[Codec[_]] = ev.codecs

}
