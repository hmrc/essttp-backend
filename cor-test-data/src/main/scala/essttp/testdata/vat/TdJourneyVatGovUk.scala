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

package essttp.testdata.vat

import essttp.journey.model.SjRequest.Vat
import essttp.journey.model._
import essttp.rootmodel.{CanPayUpfront, TaxId}
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.testdata.TdBase
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader.read
import play.api.libs.json.JsObject

import scala.language.reflectiveCalls

trait TdJourneyVatGovUk {
  dependencies: TdBase with TdVat =>
  object VatGovUk { // todo uncomment this when we are closer to finishing vat... //extends TdJourneyStructure {
    def sjRequest: Vat.Empty = SjRequest.Vat.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/vat-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/vat/govuk/journey/start"

    def sjRequestJson: JsObject = read("/testdata/vat/govuk/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Vat.Started = Journey.Vat.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Vat.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterStarted.Started,
      correlationId = dependencies.correlationId,
    )

    def journeyAfterStartedJson: JsObject = read("/testdata/vat/govuk/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = vrn

    def updateTaxIdRequestJson(): JsObject = read("/testdata/vat/govuk/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Vat.ComputedTaxId = Journey.Vat.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Vat.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      correlationId = dependencies.correlationId,
      taxId         = vrn
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/vat/govuk/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult()

    def updateEligibilityCheckRequestJson(): JsObject = read("/testdata/vat/govuk/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Vat.EligibilityChecked = Journey.Vat.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult()
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("/testdata/vat/govuk/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Vat.EligibilityChecked = Journey.Vat.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = ineligibleEligibilityCheckResult(eligibleEligibilityCheckResult())
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("/testdata/vat/govuk/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/vat/govuk/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/vat/govuk/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Vat.AnsweredCanPayUpfront = Journey.Vat.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/vat/govuk/JourneyAfterCanPayUpfrontYes.json").asJson

    def journeyAfterCanPayUpfrontNo: Journey.Vat.AnsweredCanPayUpfront = Journey.Vat.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      canPayUpfront          = canPayUpfrontNo
    )

    def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/vat/govuk/JourneyAfterCanPayUpfrontNo.json").asJson
  }
}
