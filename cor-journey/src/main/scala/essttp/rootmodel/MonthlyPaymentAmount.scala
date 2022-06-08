package essttp.rootmodel

import play.api.libs.json.{Format, Json}

final case class MonthlyPaymentAmount(value: AmountInPence) extends AnyVal

object MonthlyPaymentAmount {
  implicit val format: Format[MonthlyPaymentAmount] = Json.valueFormat[MonthlyPaymentAmount]
}
