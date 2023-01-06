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

package email

import cats.implicits.catsSyntaxEq
import essttp.emailverification.{EmailVerificationResult, EmailVerificationStatus}
import essttp.rootmodel.{Email, GGCredId}
import services.CorrelationIdGenerator

import java.time.{Instant, LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationStatusService @Inject() (
    emailVerificationStatusRepo: EmailVerificationStatusRepo,
    correlationIdGenerator:      CorrelationIdGenerator
)(implicit ec: ExecutionContext) {

  /*
   * return list of EmailVerificationStatus associated with given credId
   */
  def findEmailVerificationStatuses(ggCredId: GGCredId): Future[Option[List[EmailVerificationStatus]]] = find(ggCredId)

  def findEarliestCreatedAt(ggCredId: GGCredId): Future[Option[LocalDateTime]] = {
    find(ggCredId).map { maybeStatus: Option[List[EmailVerificationStatus]] =>
      maybeStatus.flatMap { listOfStatuses: List[EmailVerificationStatus] =>
        implicit val localDateTimeOrdering: Ordering[LocalDateTime] = _ compareTo _
        listOfStatuses
          .map(status => LocalDateTime.ofInstant(status.createdAt, ZoneOffset.UTC))
          .sorted
          .minOption
      }
    }
  }

  /*
   * increment the verification attempts for EmailVerificationStatus entry that matches given credId and email,
   * or create one if it doesn't exist
   */
  def updateNumberOfPasscodeJourneys(credId: GGCredId, email: Email): Future[Unit] =
    findEmailVerificationStatuses(credId).flatMap { maybeListOfVerificationStatuses: Option[List[EmailVerificationStatus]] =>
      maybeListOfVerificationStatuses.fold {
        upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, email, None))
      } { emailVerificationStatuses =>
        emailVerificationStatuses.find(_.email === email)
          .fold{
            upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, email, None))
          } {
            emailVerificationStatus =>
              upsert(emailVerificationStatus.copy(
                numberOfPasscodeJourneysStarted = emailVerificationStatus.numberOfPasscodeJourneysStarted.increment,
                lastUpdated                     = Instant.now
              ))
          }
      }
    }

  /*
   * update the emailVerificationResult for EmailVerificationStatus entry that matches given credId and email,
   * or create one if it doesn't exist
   */
  def updateEmailVerificationStatusResult(credId: GGCredId, email: Email, emailVerificationResult: EmailVerificationResult): Future[Unit] =
    findEmailVerificationStatuses(credId).flatMap { maybeListOfVerificationStatuses: Option[List[EmailVerificationStatus]] =>
      maybeListOfVerificationStatuses.fold {
        upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, email, Some(emailVerificationResult)))
      } { emailVerificationStatuses =>
        emailVerificationStatuses.find(_.email === email)
          .fold {
            upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, email, Some(emailVerificationResult)))
          } {
            emailVerificationStatus =>
              upsert(emailVerificationStatus.copy(
                verificationResult = Some(emailVerificationResult),
                lastUpdated        = Instant.now
              ))
          }
      }
    }

  private def find(ggCredId: GGCredId): Future[Option[List[EmailVerificationStatus]]] =
    emailVerificationStatusRepo.findAllEntries(ggCredId).map {
      case ::(head, next) => Some(head :: next)
      case Nil            => None
    }

  private def upsert(emailVerificationStatus: EmailVerificationStatus): Future[Unit] =
    emailVerificationStatusRepo.update(emailVerificationStatus)

}
