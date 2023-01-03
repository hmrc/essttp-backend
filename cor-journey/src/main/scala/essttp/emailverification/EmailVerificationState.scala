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

package essttp.emailverification

import enumeratum.{Enum, EnumEntry}
import julienrf.json.derived
import play.api.libs.json.OFormat

import scala.collection.immutable

sealed trait EmailVerificationState extends EnumEntry

object EmailVerificationState extends Enum[EmailVerificationState] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[EmailVerificationState] = derived.oformat[EmailVerificationState]()

  case object OkToBeVerified extends EmailVerificationState

  case object AlreadyVerified extends EmailVerificationState

  case object TooManyPasscodeAttempts extends EmailVerificationState

  case object TooManyPasscodeJourneysStarted extends EmailVerificationState

  case object TooManyDifferentEmailAddresses extends EmailVerificationState

  override val values: immutable.IndexedSeq[EmailVerificationState] = findValues

}
