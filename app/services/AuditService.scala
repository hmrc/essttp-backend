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

package services

import cats.syntax.eq._
import essttp.crypto.CryptoFormat
import essttp.journey.model.{Journey, Origin}
import essttp.rootmodel.ttp.eligibility.{EligibilityCheckResult, EmailSource}
import essttp.rootmodel.{Email, GGCredId}
import essttp.utils.Errors
import models.audit.{AuditDetail, EmailVerificationRequestedAuditDetail, EmailVerificationResultAudit, EmailVerificationResultAuditDetail, TaxDetail}
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.util.{Locale, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  implicit val cryptoFormat: CryptoFormat = CryptoFormat.NoOpCryptoFormat

  private val auditSource: String = "set-up-payment-plan"

  private def toAuditString(origin: Origin) = origin.toString.split('.').lastOption.getOrElse(origin.toString)

  private def audit[A <: AuditDetail: Writes](a: A)(implicit hc: HeaderCarrier): Unit = {
    val _ = auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = auditSource,
        auditType   = a.auditType,
        eventId     = UUID.randomUUID().toString,
        tags        = hc.toAuditTags(),
        detail      = Json.toJson(a)
      )
    )
  }

  def auditEmailVerificationRequested(journey: Journey, ggCredId: GGCredId, email: Email, result: String)(implicit headerCarrier: HeaderCarrier): Unit =
    audit(toEmailVerificationRequested(journey, ggCredId, email, result))

  def auditEmailVerificationResult(journey: Journey, ggCredId: GGCredId, email: Email, result: EmailVerificationResultAudit, failureReason: Option[String])(implicit headerCarrier: HeaderCarrier): Unit =
    audit(toEmailVerificationResult(journey, ggCredId, email: Email, result, failureReason))

  private def toEmailVerificationRequested(
      journey:  Journey,
      ggCredId: GGCredId,
      email:    Email,
      result:   String
  ): EmailVerificationRequestedAuditDetail = {
    EmailVerificationRequestedAuditDetail(
      origin         = toAuditString(journey.origin),
      taxType        = journey.taxRegime.toString,
      taxDetail      = toTaxDetail(toEligibilityCheckResult(journey)),
      correlationId  = journey.correlationId,
      emailAddress   = email,
      emailSource    = deriveEmailSource(journey, email),
      result         = result,
      authProviderId = ggCredId.value
    )
  }

  private def toEmailVerificationResult(
                                         journey: Journey,
                                         ggCredId: GGCredId,
                                         email: Email,
                                         result: EmailVerificationResultAudit,
                                         failureReason: Option[String]
                                       ): EmailVerificationResultAuditDetail = {
    EmailVerificationResultAuditDetail(
      origin         = toAuditString(journey.origin),
      taxType        = journey.taxRegime.toString,
      taxDetail      = toTaxDetail(toEligibilityCheckResult(journey)),
      correlationId  = journey.correlationId,
      emailAddress   = email,
      emailSource    = deriveEmailSource(journey, email),
      result         = result,
      failureReason  = failureReason,
      authProviderId = ggCredId.value
    )
  }

  private def toTaxDetail(eligibilityCheckResult: EligibilityCheckResult): TaxDetail =
    TaxDetail(
      utr               = None,
      taxOfficeNo       = None,
      taxOfficeRef      = None,
      employerRef       = getTaxId("EMPREF")(eligibilityCheckResult),
      accountsOfficeRef = getTaxId("BROCS")(eligibilityCheckResult),
      vrn               = getTaxId("VRN")(eligibilityCheckResult)
    )

  private def getTaxId(name: String)(eligibilityCheckResult: EligibilityCheckResult): Option[String] =
    eligibilityCheckResult.identification.find(_.idType.value === name).map(_.idValue.value)

  private def deriveEmailSource(journey: Journey, email: Email): EmailSource = {
    val emailFromEligibility = toEligibilityCheckResult(journey).email
    if (emailFromEligibility.map(_.value.decryptedValue.toLowerCase(Locale.UK)).contains(email.value.decryptedValue.toLowerCase(Locale.UK))) {
      EmailSource.ETMP
    } else {
      EmailSource.TEMP
    }
  }

  private def toEligibilityCheckResult(journey: Journey) = journey match {
    case j: Journey.AfterEligibilityChecked => j.eligibilityCheckResult
    case _                                  => Errors.throwServerErrorException("Trying to get eligibility check result for audit event, but it hasn't been retrieved yet.")
  }

}
