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

package essttp.utils

import uk.gov.hmrc.http.HttpReads.Implicits.{readEitherOf, readRaw, throwOnFailure}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, UpstreamErrorResponse}

object HttpReadsUnitThrowingException {

  /**
   * It's a backward compatible implementation of readUnit for code written using versions of http-verbs before version 11.0.0.
   * This implementation retains the old behaviour of readUnit which throws an exception
   * if the http response status is 5xx or 4xx.
   *
   * Use it by avoiding the import of `readUnit` from import uk.gov.hmrc.http.HttpReads.Implicits.readUnit and instead
   * using this HttpReadsUnitThrowingException.readUnit in its place (try to do this without shadowing just in case).
   *
   * Example:
   * {{{
   *   import uk.gov.hmrc.http.HttpReads.Implicits.{readUnit =>_, _} //this imports all but readUnit
   *   import directdebit.corjourney.util.HttpReadsUnitThrowingException.readUnit
   * }}}
   *
   */
  implicit val readUnit: HttpReads[Unit] = {
    val eitherHttpResponseReads: HttpReads[Either[UpstreamErrorResponse, HttpResponse]] = readEitherOf[HttpResponse]
    val eitherUnitReads: HttpReads[Either[UpstreamErrorResponse, Unit]] = eitherHttpResponseReads.map(x => x.right.map(_ => ()))
    throwOnFailure(eitherUnitReads)
  }
}
