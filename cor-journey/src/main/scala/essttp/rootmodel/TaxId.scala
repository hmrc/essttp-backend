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

package essttp.rootmodel

import play.api.libs.functional.syntax._
import play.api.libs.json.Format

sealed trait TaxId {
  def value: String
}

/**
 * Accounts Office Reference (Aor)
 * Tax Id for Epaye.
 */
final case class Aor(value: String) extends TaxId

object Aor {
  implicit val format: Format[Aor] = implicitly[Format[String]].inmap(Aor(_), _.value)
}

/**
 * Vat Reference Number (Vrn)
 * Tax Id for Vat.
 */
final case class Vrn(value: String) extends TaxId

object Vrn {
  implicit val format: Format[Vrn] = implicitly[Format[String]].inmap(Vrn(_), _.value)
}

final case class EmpRef(value: String) extends TaxId

object EmpRef {
  implicit val format: Format[EmpRef] = implicitly[Format[String]].inmap(EmpRef(_), _.value)
}
