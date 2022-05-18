package essttp.rootmodel

import play.api.libs.json.{Format, Json}

final case class CanPayUpfront(value: Boolean) extends AnyVal

object CanPayUpfront {
  implicit val format: Format[CanPayUpfront] = Json.valueFormat[CanPayUpfront]
}
