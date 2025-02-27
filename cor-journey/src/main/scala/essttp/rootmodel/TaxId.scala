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

package essttp.rootmodel

import cats.Eq
import essttp.rootmodel.epaye.{TaxOfficeNumber, TaxOfficeReference}
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait TaxId {
  val value: String
}

object TaxId {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[TaxId] = derived.oformat[TaxId]()

  implicit val eq: Eq[TaxId] = Eq.fromUniversalEquals

}

final case class EmpRef(value: String) extends TaxId

/**
 * Employer Reference
 * Tax Id for Epaye.
 */
object EmpRef {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[EmpRef] = derived.oformat[EmpRef]()

  def makeEmpRef(taxOfficeNumber: TaxOfficeNumber, taxOfficeReference: TaxOfficeReference): EmpRef = EmpRef(
    s"${taxOfficeNumber.value}${taxOfficeReference.value}"
  )
}

/**
 * Vat Reference Number (Vrn)
 * Tax Id for Vat.
 * Regex https://github.com/hmrc/service-enrolment-config/blob/master/conf/SEC1_with_enrolment_rules_json/prod/HMRC-MTD-VAT.json
 */
final case class Vrn(value: String) extends TaxId

object Vrn {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Vrn] = derived.oformat[Vrn]()
}

/**
 * Self Assessment Unique Tax Reference (SaUtr)
 * Tax Id for Sa.
 */
final case class SaUtr(value: String) extends TaxId

object SaUtr {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[SaUtr] = derived.oformat[SaUtr]()
}

/**
 * Simple Self Assessment Unique Tax Reference (Nino) Tax Id for Simp.
 */
final case class Nino(value: String) extends TaxId

object Nino {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Nino] = derived.oformat[Nino]()
}
