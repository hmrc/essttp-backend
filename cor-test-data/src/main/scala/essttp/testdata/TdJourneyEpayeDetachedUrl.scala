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

import essttp.journey.model.SjRequest.Epaye
import essttp.journey.model._
import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.rootmodel.{CanPayUpfront, TaxId, UpfrontPaymentAmount}
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader._
import play.api.libs.json.JsObject

import scala.language.reflectiveCalls

trait TdJourneyEpayeDetachedUrl { dependencies: TdBase with TdEpaye =>

  object EpayeDetachedUrl extends TdJourneyStructure {

    def sjRequest: Epaye.Empty = SjRequest.Epaye.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/detached-url/journey/start"
    def sjRequestJson: JsObject = read("testdata/epaye/detachedurl/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id       = dependencies.journeyId,
      origin    = Origins.Epaye.DetachedUrl,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      stage     = Stage.AfterStarted.Started
    )

    def journeyAfterStartedJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = empRef

    def updateTaxIdRequestJson(): JsObject = read("testdata/epaye/detachedurl/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id       = dependencies.journeyId,
      origin    = Origins.Epaye.DetachedUrl,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      stage     = Stage.AfterComputedTaxId.ComputedTaxId,
      taxId     = empRef
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def updateEligibilityCheckRequestJson(): JsObject = read("testdata/epaye/detachedurl/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityCheck = Journey.Epaye.EligibilityCheck(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityCheck = Journey.Epaye.EligibilityCheck(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontNo
    )

    override def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterCanPayUpfrontYes.json").asJson

    override def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterCanPayUpfrontNo.json").asJson

    override def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    override def updateUpfrontPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateUpfrontPaymentAmountRequest.json").asJson

    override def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    override def journeyAfterUpfrontPaymentAmountJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateUpfrontPaymentAmount.json").asJson
  }
}
