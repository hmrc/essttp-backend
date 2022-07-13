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

import essttp.journey.model.ttp.{ChargeId, InterestAccrued}
import essttp.journey.model.ttp.affordability.InstalmentAmounts
import essttp.journey.model.ttp.affordablequotes.{AffordableQuotesResponse, AmountDue, Collection, DebtItemOriginalDueDate, DueDate, InitialCollection, Instalment, InstalmentBalance, InstalmentNumber, NumberOfInstalments, PaymentPlan, PlanDuration, PlanInterest, RegularCollection, TotalDebt, TotalDebtIncludingInterest}
import essttp.journey.model.{JourneyId, UpfrontPaymentAnswers}
import essttp.rootmodel._
import essttp.rootmodel.bank.{AccountNumber, BankDetails, DirectDebitDetails, SortCode, TypeOfBankAccount, TypesOfBankAccount}
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.extremedates.{EarliestPlanStartDate, ExtremeDatesResponse, LatestPlanStartDate}
import essttp.rootmodel.dates.startdates.{InstalmentStartDate, StartDatesResponse}
import essttp.utils.TdSupport.FakeRequestOps
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

import java.time.{LocalDate, LocalDateTime}

trait TdBase {
  def journeyId: JourneyId = JourneyId("b6217497-ab5b-4e93-855a-afc9f9e933b6")
  def traceId: TraceId = TraceId(journeyId)
  def sessionId: SessionId = SessionId("session-2082fcd4-70f6-49cc-a4bf-845917981cd7")

  def createdOn: LocalDateTime = LocalDateTime.parse("2057-11-02T16:28:55.185")
  def amountToUpdate: AmountInPence = AmountInPence(123499)
  def amountInPence: AmountInPence = AmountInPence(1000)
  def upfrontPaymentAmount: UpfrontPaymentAmount = UpfrontPaymentAmount(amountInPence)
  def anotherUpfrontPaymentAmount: UpfrontPaymentAmount = UpfrontPaymentAmount(amountInPence.copy(value = 1001))
  def instalmentAmounts: InstalmentAmounts = InstalmentAmounts(AmountInPence(1000), AmountInPence(2000))
  def upfrontPaymentAnswersDeclared: UpfrontPaymentAnswers = UpfrontPaymentAnswers.DeclaredUpfrontPayment(upfrontPaymentAmount)
  def upfrontPaymentAnswersNoUpfrontPayment: UpfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment
  def initialPaymentDate: InitialPaymentDate = InitialPaymentDate(LocalDate.parse("2022-01-01"))
  def earliestPlanStartDate: EarliestPlanStartDate = EarliestPlanStartDate(LocalDate.parse("2022-02-01"))
  def latestPlanStartDate: LatestPlanStartDate = LatestPlanStartDate(LocalDate.parse("2022-03-01"))
  def extremeDatesWithUpfrontPayment: ExtremeDatesResponse = ExtremeDatesResponse(Some(initialPaymentDate), earliestPlanStartDate, latestPlanStartDate)
  def extremeDatesWithoutUpfrontPayment: ExtremeDatesResponse = extremeDatesWithUpfrontPayment.copy(initialPaymentDate = None)
  def monthlyPaymentAmount: MonthlyPaymentAmount = MonthlyPaymentAmount(AmountInPence(20000))
  def dayOfMonth: DayOfMonth = DayOfMonth(1)
  def startDatesResponseWithInitialPayment: StartDatesResponse = StartDatesResponse(Some(InitialPaymentDate(LocalDate.parse("2022-01-01"))), InstalmentStartDate(LocalDate.parse("2022-01-01")))
  def startDatesResponseWithoutInitialPayment: StartDatesResponse = StartDatesResponse(None, InstalmentStartDate(LocalDate.parse("2022-01-01")))
  def dueDate: DueDate = DueDate(LocalDate.parse("2022-02-01"))
  def amountDue: AmountDue = AmountDue(amountInPence)
  def paymentPlan(numberOfInstalments: Int): PaymentPlan = PaymentPlan(
    numberOfInstalments = NumberOfInstalments(numberOfInstalments),
    planDuration        = PlanDuration(numberOfInstalments),
    totalDebt           = TotalDebt(amountInPence),
    totalDebtIncInt     = TotalDebtIncludingInterest(amountInPence.+(amountInPence)),
    planInterest        = PlanInterest(amountInPence),
    collections         = Collection(
      initialCollection  = Some(InitialCollection(dueDate   = dueDate, amountDue = amountDue)),
      regularCollections = List(RegularCollection(dueDate   = dueDate, amountDue = amountDue))
    ),
    instalments         = List(Instalment(
      instalmentNumber          = InstalmentNumber(numberOfInstalments),
      dueDate                   = DueDate(LocalDate.parse("2022-02-01")),
      instalmentInterestAccrued = InterestAccrued(amountInPence),
      instalmentBalance         = InstalmentBalance(amountInPence),
      debtItemChargeId          = ChargeId("testchargeid"),
      amountDue                 = amountDue,
      debtItemOriginalDueDate   = DebtItemOriginalDueDate(LocalDate.parse("2022-01-01"))
    ))
  )

  def affordableQuotesResponse: AffordableQuotesResponse = AffordableQuotesResponse(List(paymentPlan(1)))

  def businessBankAccount: TypeOfBankAccount = TypesOfBankAccount.Business
  def personalBankAccount: TypeOfBankAccount = TypesOfBankAccount.Personal

  def directDebitDetails(isAccountHolder: Boolean): DirectDebitDetails = DirectDebitDetails(
    BankDetails(
      "First Last",
      SortCode("123456"),
      AccountNumber("12345678")
    ),
    isAccountHolder
  )

  def backUrl: BackUrl = BackUrl("https://www.tax.service.gov.uk/back-url")
  def returnUrl: ReturnUrl = ReturnUrl("https://www.tax.service.gov.uk/return-url")

  def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSessionId()
    .withAuthToken()
    .withAkamaiReputationHeader()
    .withRequestId()
    .withTrueClientIp()
    .withTrueClientPort()
    .withDeviceId()

  def requestNotLoggedIn: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSessionId()
    .withAkamaiReputationHeader()
    .withRequestId()
    .withTrueClientIp()
    .withTrueClientPort()
    .withDeviceId()

  def authToken: String = "authorization-value-123"
  def akamaiReputationValue: String = "akamai-reputation-value-123"
  def requestId: String = "request-id-value-123"
  def trueClientIp: String = "client-ip-123"
  def trueClientPort: String = "client-port-123"
  def deviceId: String = "device-id-123"

}
