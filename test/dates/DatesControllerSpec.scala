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

package dates

import dates.DatesTdAll.TdDates
import essttp.rootmodel.dates.extremedates.{EarliestPaymentPlanStartDate, ExtremeDatesRequest, ExtremeDatesResponse, LatestPaymentPlanStartDate}
import essttp.rootmodel.dates.startdates.{PreferredDayOfMonth, StartDatesRequest, StartDatesResponse}
import essttp.rootmodel.dates.{InitialPayment, InitialPaymentDate}
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.Status.{INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import testsupport.{FrozenTime, ItSpec}
import testsupport.stubs.AuthStub
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}

import java.time.LocalDate
import scala.concurrent.Future

class DatesControllerSpec extends ItSpec with TableDrivenPropertyChecks {

  def connector: TestDatesConnector = app.injector.instanceOf[TestDatesConnector]

  "POST /start-dates" - {

    behave like unauthenticatedBehaviour(
      connector.startDates(DatesTdAll.startDatesRequest(InitialPayment(true), PreferredDayOfMonth(28)))(_)
    )

    val testDataTable = Table(
      ("Current date", "Preferred day of month", "Initial payment", "Earliest InitialPaymentDate", "Earliest InstalmentStartDate"),
      (TdDates.`1stJan2022`, 1, false, None, TdDates.`1stFeb2022`),
      (TdDates.`1stJan2022`, 2, false, None, TdDates.`2ndFeb2022`),
      (TdDates.`1stJan2022`, 10, false, None, TdDates.`10thFeb2022`),
      (TdDates.`1stJan2022`, 11, false, None, TdDates.`11thJan2022`),
      (TdDates.`1stJan2022`, 15, false, None, TdDates.`15thJan2022`),
      (TdDates.`1stJan2022`, 28, false, None, TdDates.`28thJan2022`),

      (TdDates.`15thJan2022`, 1, false, None, TdDates.`1stFeb2022`),
      (TdDates.`15thJan2022`, 2, false, None, TdDates.`2ndFeb2022`),
      (TdDates.`15thJan2022`, 10, false, None, TdDates.`10thFeb2022`),
      (TdDates.`15thJan2022`, 15, false, None, TdDates.`15thFeb2022`),
      (TdDates.`15thJan2022`, 25, false, None, TdDates.`25thJan2022`),
      (TdDates.`15thJan2022`, 28, false, None, TdDates.`28thJan2022`),

      (TdDates.`28thJan2022`, 1, false, None, TdDates.`1stMar2022`),
      (TdDates.`28thJan2022`, 2, false, None, TdDates.`2ndMar2022`),
      (TdDates.`28thJan2022`, 15, false, None, TdDates.`15thFeb2022`),
      (TdDates.`28thJan2022`, 28, false, None, TdDates.`28thFeb2022`),

      (TdDates.`1stFeb2022`, 1, false, None, TdDates.`1stMar2022`),
      (TdDates.`1stFeb2022`, 2, false, None, TdDates.`2ndMar2022`),
      (TdDates.`1stFeb2022`, 15, false, None, TdDates.`15thFeb2022`),
      (TdDates.`1stFeb2022`, 28, false, None, TdDates.`28thFeb2022`),
      (TdDates.`15thFeb2022`, 1, false, None, TdDates.`1stMar2022`),
      (TdDates.`15thFeb2022`, 2, false, None, TdDates.`2ndMar2022`),
      (TdDates.`15thFeb2022`, 15, false, None, TdDates.`15thMar2022`),
      (TdDates.`15thFeb2022`, 25, false, None, TdDates.`25thFeb2022`),

      (TdDates.`1stJan2022`, 1, true, Some(TdDates.`11thJan2022`), TdDates.`1stFeb2022`),
      (TdDates.`1stJan2022`, 2, true, Some(TdDates.`11thJan2022`), TdDates.`2ndFeb2022`),
      (TdDates.`1stJan2022`, 15, true, Some(TdDates.`11thJan2022`), TdDates.`15thFeb2022`),
      (TdDates.`1stJan2022`, 28, true, Some(TdDates.`11thJan2022`), TdDates.`28thFeb2022`),

      (TdDates.`15thJan2022`, 1, true, Some(TdDates.`25thJan2022`), TdDates.`1stMar2022`),
      (TdDates.`15thJan2022`, 2, true, Some(TdDates.`25thJan2022`), TdDates.`2ndMar2022`),
      (TdDates.`15thJan2022`, 15, true, Some(TdDates.`25thJan2022`), TdDates.`15thFeb2022`),
      (TdDates.`15thJan2022`, 28, true, Some(TdDates.`25thJan2022`), TdDates.`28thFeb2022`),

      (TdDates.`28thJan2022`, 1, true, Some(TdDates.`7thFeb2022`), TdDates.`1stMar2022`),
      (TdDates.`28thJan2022`, 2, true, Some(TdDates.`7thFeb2022`), TdDates.`2ndMar2022`),
      (TdDates.`28thJan2022`, 15, true, Some(TdDates.`7thFeb2022`), TdDates.`15thMar2022`),

      (TdDates.`25thDec2022`, 1, false, None, TdDates.`1stFeb2023`),
      (TdDates.`25thDec2022`, 1, true, Some(TdDates.`4thJan2023`), TdDates.`1stFeb2023`),
      (TdDates.`25thDec2022`, 15, false, None, TdDates.`15thJan2023`),
      (TdDates.`25thDec2022`, 15, true, Some(TdDates.`4thJan2023`), TdDates.`15thFeb2023`),
      (TdDates.`25thDec2022`, 28, false, None, TdDates.`28thJan2023`),
      (TdDates.`25thDec2022`, 28, true, Some(TdDates.`4thJan2023`), TdDates.`28thJan2023`)
    )
    forAll(testDataTable) { (
      currentDate: String,
      preferredDayOfMonth: Int,
      initialPayment: Boolean,
      earliestInitialPaymentDate: Option[String],
      earliestInstalmentStartDate: String
    ) =>
      s"[CurrentDay: $currentDate][PreferredDayOfMonth: ${preferredDayOfMonth.toString}][InitialPayment:${initialPayment.toString}][ExpectedStartDate: $earliestInstalmentStartDate]" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("Bearer abc")))
        stubCommonActions()

        FrozenTime.setTime(currentDate)
        val initialPaymentDate: Option[InitialPaymentDate] = earliestInitialPaymentDate.map(someDate => InitialPaymentDate(LocalDate.parse(someDate)))
        val request: StartDatesRequest = DatesTdAll.startDatesRequest(InitialPayment(initialPayment), PreferredDayOfMonth(preferredDayOfMonth))
        val response: StartDatesResponse = connector.startDates(request).futureValue
        response shouldBe DatesTdAll.startDatesResponse(initialPaymentDate, earliestInstalmentStartDate)

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }
  }

  "POST /extreme-dates should" - {

    behave like unauthenticatedBehaviour(
      connector.extremeDates(ExtremeDatesRequest(InitialPayment(false)))(_)
    )

    "return earliestPlanStartDate(+10 days), latestPlanStartDate(+40 days), when initialPayment=false, " in {
      implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("Bearer abc")))
      stubCommonActions()

      FrozenTime.setTime(TdDates.`1stJan2022`)
      val request: ExtremeDatesRequest = ExtremeDatesRequest(InitialPayment(false))
      val expectedResult: ExtremeDatesResponse = ExtremeDatesResponse(
        initialPaymentDate    = None,
        earliestPlanStartDate = EarliestPaymentPlanStartDate(LocalDate.parse(TdDates.`11thJan2022`)),
        latestPlanStartDate   = LatestPaymentPlanStartDate(LocalDate.parse(TdDates.`10thFeb2022`))
      )
      val response: ExtremeDatesResponse = connector.extremeDates(request).futureValue
      response shouldBe expectedResult

      verifyCommonActions(numberOfAuthCalls = 1)
    }

    "return Some(initialPaymentDate(+10 days)), earliestPlanStartDate(+30 days), latestPlanStartDate(+60 days), when initialPayment=true" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("Bearer abc")))
      stubCommonActions()

      FrozenTime.setTime(TdDates.`1stJan2022`)
      val request: ExtremeDatesRequest = ExtremeDatesRequest(InitialPayment(true))
      val expectedResult: ExtremeDatesResponse = ExtremeDatesResponse(
        initialPaymentDate    = Some(InitialPaymentDate(LocalDate.parse(TdDates.`11thJan2022`))),
        earliestPlanStartDate = EarliestPaymentPlanStartDate(LocalDate.parse("2022-01-31")),
        latestPlanStartDate   = LatestPaymentPlanStartDate(LocalDate.parse(TdDates.`2ndMar2022`))
      )
      val response: ExtremeDatesResponse = connector.extremeDates(request).futureValue
      response shouldBe expectedResult

      verifyCommonActions(numberOfAuthCalls = 1)
    }

  }

  def unauthenticatedBehaviour[A](doCall: HeaderCarrier => Future[A]): Unit = {
      def checkResultHasStatus(status: Int, hc: HeaderCarrier): Unit = {
        val result = doCall(hc).failed.futureValue

        result match {
          case e: UpstreamErrorResponse => e.statusCode shouldBe status
          case other                    => fail(s"Expected UpstreamErrorResponse but got ${other.getClass.getSimpleName}")
        }
        ()
      }

    "should return an 401 (UNAUTHORIZED) response" - {

      "when there is no bearer token can be found" in {
        checkResultHasStatus(UNAUTHORIZED, HeaderCarrier())

        AuthStub.ensureAuthoriseCalled(0)
      }

      "an authorisation exception is thrown" in {
        AuthStub.authoriseError("BearerTokenExpired")

        checkResultHasStatus(UNAUTHORIZED, HeaderCarrier(authorization = Some(Authorization("Bearer abc"))))
        AuthStub.ensureAuthoriseCalled(1)
      }

    }

    "should return an 500 (INTERNAL SERVER ERROR) response when there is an error authenticating" in {
      AuthStub.authoriseError("ABC")

      checkResultHasStatus(INTERNAL_SERVER_ERROR, HeaderCarrier(authorization = Some(Authorization("Bearer abc"))))

      AuthStub.ensureAuthoriseCalled(1)
    }

  }

}
