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

package essttp.testdata

import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.journey.model.{Journey, SjRequest, SjResponse}
import essttp.rootmodel.{CanPayUpfront, TaxId, UpfrontPaymentAmount}
import play.api.libs.json.JsObject

trait TdJourneyStructure {
  /**
   * Defining all td requirements for each journey
   */
  def sjRequest: SjRequest
  def sjResponse: SjResponse
  def postPath: String
  def sjRequestJson: JsObject
  def journeyAfterStarted: Journey
  def journeyAfterStartedJson: JsObject
  def updateTaxIdRequest(): TaxId
  def updateTaxIdRequestJson(): JsObject
  def journeyAfterDetermineTaxIds: Journey
  def journeyAfterDetermineTaxIdsJson: JsObject
  def updateEligibilityCheckRequest(): EligibilityCheckResult
  def updateEligibilityCheckRequestJson(): JsObject
  def journeyAfterEligibilityCheckEligible: Journey
  def journeyAfterEligibilityCheckEligibleJson: JsObject
  def journeyAfterEligibilityCheckNotEligible: Journey
  def journeyAfterEligibilityCheckNotEligibleJson: JsObject
  def updateCanPayUpfrontYesRequest(): CanPayUpfront
  def updateCanPayUpfrontNoRequest(): CanPayUpfront
  def updateCanPayUpfrontYesRequestJson(): JsObject
  def updateCanPayUpfrontNoRequestJson(): JsObject
  def journeyAfterCanPayUpfrontYes: Journey
  def journeyAfterCanPayUpfrontYesJson: JsObject
  def journeyAfterCanPayUpfrontNo: Journey
  def journeyAfterCanPayUpfrontNoJson: JsObject
  def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount
  def updateUpfrontPaymentAmountRequestJson(): JsObject
  def journeyAfterUpfrontPaymentAmount: Journey
  def journeyAfterUpfrontPaymentAmountJson: JsObject
}
