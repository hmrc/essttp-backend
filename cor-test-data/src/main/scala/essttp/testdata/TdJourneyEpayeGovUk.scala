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

import essttp.journey.model._
import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.rootmodel.{CanPayUpfront, TaxId}
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader._
import play.api.libs.json.JsObject

import scala.language.reflectiveCalls

trait TdJourneyEpayeGovUk { dependencies: TdBase with TdEpaye =>

  object EpayeGovUk {

    def sjRequest = SjRequest.Epaye.Empty()

    def sjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/gov-uk/journey/start"
    def sjRequestJson: JsObject = read("testdata/epaye/govuk/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.AfterStarted = Journey.Epaye.AfterStarted(
      _id       = dependencies.journeyId,
      origin    = Origins.Epaye.GovUk,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      stage     = Stage.AfterStarted.Started
    )

    def journeyAfterStartedJson: JsObject = read("testdata/epaye/govuk/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = empRef

    def updateTaxIdRequestJson(): JsObject = read("testdata/epaye/govuk/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Epaye.AfterComputedTaxIds = Journey.Epaye.AfterComputedTaxIds(
      _id       = dependencies.journeyId,
      origin    = Origins.Epaye.GovUk,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      stage     = Stage.AfterComputedTaxId.ComputedTaxId,
      taxId     = empRef
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/epaye/govuk/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def updateEligibilityCheckRequestJson(): JsObject = read("testdata/epaye/govuk/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.AfterEligibilityCheck = Journey.Epaye.AfterEligibilityCheck(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("testdata/epaye/govuk/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.AfterEligibilityCheck = Journey.Epaye.AfterEligibilityCheck(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("testdata/epaye/govuk/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AfterCanPayUpfront = Journey.Epaye.AfterCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AfterCanPayUpfront = Journey.Epaye.AfterCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontNo
    )
  }
}
