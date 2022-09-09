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

package essttp.rootmodel.bank

import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.{Format, Json, __}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, PlainText}

final case class AccountName(value: SensitiveString) extends AnyVal

object AccountName {

  implicit val plainTextFormat: Format[PlainText] = {
    (__ \ "value").format[String].inmap(value => PlainText(value), (plainText: PlainText) => plainText.value)
  }

  implicit def format(implicit crypto: Encrypter with Decrypter): Format[AccountName] = {
    implicit val sensitiveStringFormat: Format[SensitiveString] = JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)
    Json.valueFormat
  }
}
