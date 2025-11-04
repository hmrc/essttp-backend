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

package testsupport.testdata

import essttp.journey.model._
import essttp.rootmodel._
import essttp.rootmodel.bank._
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.extremedates.{EarliestPaymentPlanStartDate, ExtremeDatesResponse, LatestPaymentPlanStartDate}
import essttp.rootmodel.dates.startdates.{InstalmentStartDate, StartDatesResponse}
import essttp.rootmodel.pega.{PegaCaseId, StartCaseResponse}
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes._
import essttp.rootmodel.ttp.eligibility.{ChargeReference, EligibilityCheckResult, EligibilityPass, EligibilityRules, EligibilityStatus}
import models.pega.{PegaOauthToken, PegaStartCaseResponse}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.TdSupport._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.Authorization

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

trait TdBase {
  def journeyId: JourneyId = JourneyId("b6217497-ab5b-4e93-855a-afc9f9e933b6")

  def sessionId: SessionId = SessionId("session-2082fcd4-70f6-49cc-a4bf-845917981cd7")

  def correlationId: CorrelationId = CorrelationId(UUID.fromString("5838794a-5419-496c-a5dd-e807f91d6da6"))

  def authorization: Authorization = Authorization("Bearer xyz")

  def createdOn: Instant = LocalDateTime.parse("2057-11-02T16:28:55.185").toInstant(ZoneOffset.UTC)

  def amountToUpdate: AmountInPence = AmountInPence(123499)

  def amountInPence: AmountInPence = AmountInPence(1000)

  val reusableDateAsString: String = "2022-05-17"
  val reusableDate: LocalDate      = LocalDate.parse(reusableDateAsString)

  val eligibleEligibilityRules: EligibilityRules = EligibilityRules(
    hasRlsOnAddress = false,
    markedAsInsolvent = false,
    isLessThanMinDebtAllowance = false,
    isMoreThanMaxDebtAllowance = false,
    disallowedChargeLockTypes = false,
    existingTTP = false,
    chargesOverMaxDebtAge = None,
    ineligibleChargeTypes = false,
    missingFiledReturns = false,
    hasInvalidInterestSignals = None,
    dmSpecialOfficeProcessingRequired = None,
    noDueDatesReached = false,
    cannotFindLockReason = None,
    creditsNotAllowed = None,
    isMoreThanMaxPaymentReference = None,
    chargesBeforeMaxAccountingDate = None,
    hasInvalidInterestSignalsCESA = None,
    hasDisguisedRemuneration = None,
    hasCapacitor = None,
    dmSpecialOfficeProcessingRequiredCDCS = None,
    isAnMtdCustomer = None,
    dmSpecialOfficeProcessingRequiredCESA = None,
    noMtditsaEnrollment = None
  )

  def ineligibleEligibilityCheckResult(eligibleEligibilityCheckResult: EligibilityCheckResult): EligibilityCheckResult =
    eligibleEligibilityCheckResult.copy(
      eligibilityStatus = EligibilityStatus(EligibilityPass(value = false)),
      eligibilityRules = hasRlsAddressOn
    )

  val hasRlsAddressOn: EligibilityRules =
    eligibleEligibilityRules.copy(hasRlsOnAddress = true)

  val whyCannotPayInFullNotRequired: WhyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired

  val whyCannotPayInFullRequired: WhyCannotPayInFullAnswers =
    WhyCannotPayInFullAnswers.WhyCannotPayInFull(Set(CannotPayReason.LostOrReducedAbilityToEarnOrTrade))

  val canPayUpfrontYes: CanPayUpfront = CanPayUpfront(value = true)
  val canPayUpfrontNo: CanPayUpfront  = CanPayUpfront(value = false)

  def upfrontPaymentAmount: UpfrontPaymentAmount = UpfrontPaymentAmount(amountInPence)

  def anotherUpfrontPaymentAmount: UpfrontPaymentAmount = UpfrontPaymentAmount(amountInPence.copy(value = 1001))

  def instalmentAmounts: InstalmentAmounts = InstalmentAmounts(AmountInPence(1000), AmountInPence(2000))

  def upfrontPaymentAnswersDeclared: UpfrontPaymentAnswers =
    UpfrontPaymentAnswers.DeclaredUpfrontPayment(upfrontPaymentAmount)

  def upfrontPaymentAnswersNoUpfrontPayment: UpfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment

  def initialPaymentDate: InitialPaymentDate = InitialPaymentDate(LocalDate.parse("2022-01-01"))

  def earliestPlanStartDate: EarliestPaymentPlanStartDate = EarliestPaymentPlanStartDate(LocalDate.parse("2022-02-01"))

  def latestPlanStartDate: LatestPaymentPlanStartDate = LatestPaymentPlanStartDate(LocalDate.parse("2022-03-01"))

  def extremeDatesWithUpfrontPayment: ExtremeDatesResponse =
    ExtremeDatesResponse(Some(initialPaymentDate), earliestPlanStartDate, latestPlanStartDate)

  def extremeDatesWithoutUpfrontPayment: ExtremeDatesResponse =
    extremeDatesWithUpfrontPayment.copy(initialPaymentDate = None)

  val canPayWithinSixMonthsNotRequired = CanPayWithinSixMonthsAnswers.AnswerNotRequired

  val canPayWithinSixMonthsNo = CanPayWithinSixMonthsAnswers.CanPayWithinSixMonths(value = false)

  def monthlyPaymentAmount: MonthlyPaymentAmount = MonthlyPaymentAmount(AmountInPence(20000))

  def dayOfMonth: DayOfMonth = DayOfMonth(1)

  def startDatesResponseWithInitialPayment: StartDatesResponse = StartDatesResponse(
    Some(InitialPaymentDate(LocalDate.parse("2022-01-01"))),
    InstalmentStartDate(LocalDate.parse("2022-01-01"))
  )

  def startDatesResponseWithoutInitialPayment: StartDatesResponse =
    StartDatesResponse(None, InstalmentStartDate(LocalDate.parse("2022-01-01")))

  def dueDate: DueDate = DueDate(LocalDate.parse("2022-02-01"))

  def amountDue: AmountDue = AmountDue(amountInPence)

  def paymentPlan(numberOfInstalments: Int): PaymentPlan = PaymentPlan(
    numberOfInstalments = NumberOfInstalments(numberOfInstalments),
    planDuration = PlanDuration(numberOfInstalments),
    totalDebt = TotalDebt(amountInPence),
    totalDebtIncInt = TotalDebtIncludingInterest(amountInPence.+(amountInPence)),
    planInterest = PlanInterest(amountInPence),
    collections = Collection(
      initialCollection = Some(InitialCollection(dueDate = dueDate, amountDue = amountDue)),
      regularCollections = List(RegularCollection(dueDate = dueDate, amountDue = amountDue))
    ),
    instalments = List(
      Instalment(
        instalmentNumber = InstalmentNumber(numberOfInstalments),
        dueDate = DueDate(LocalDate.parse("2022-02-01")),
        instalmentInterestAccrued = InterestAccrued(amountInPence),
        instalmentBalance = InstalmentBalance(amountInPence),
        debtItemChargeId = ChargeReference("testchargeid"),
        amountDue = amountDue,
        debtItemOriginalDueDate = DebtItemOriginalDueDate(LocalDate.parse("2022-01-01")),
        expectedPayment = None
      )
    )
  )

  def paymentPlanAnswersNoAffordability = PaymentPlanAnswers.PaymentPlanNoAffordability(
    monthlyPaymentAmount,
    dayOfMonth,
    startDatesResponseWithInitialPayment,
    affordableQuotesResponse,
    paymentPlan(1)
  )

  def paymentPlanAnswersWithAffordability = PaymentPlanAnswers.PaymentPlanAfterAffordability(
    startCaseResponse,
    dayOfMonth,
    paymentPlan(1)
  )

  def affordableQuotesResponse: AffordableQuotesResponse = AffordableQuotesResponse(List(paymentPlan(1)))

  def affordableQuotesResponseWith2Plans: AffordableQuotesResponse = AffordableQuotesResponse(List(paymentPlan(2)))

  def businessBankAccount: TypeOfBankAccount = TypesOfBankAccount.Business

  def personalBankAccount: TypeOfBankAccount = TypesOfBankAccount.Personal

  val directDebitDetails: BankDetails =
    BankDetails(
      AccountName(SensitiveString("First Last")),
      SortCode(SensitiveString("123456")),
      AccountNumber(SensitiveString("12345678"))
    )

  val email: Email = Email(SensitiveString("bobross@joyofpainting.com"))

  val emailVerificationSuccess: EmailVerificationResult = EmailVerificationResult.Verified

  val emailVerificationLocked: EmailVerificationResult = EmailVerificationResult.Locked

  val emailVerificationAnswersEmailNotNeeded: EmailVerificationAnswers = EmailVerificationAnswers.NoEmailJourney

  val emailVerificationAnswersSuccess: EmailVerificationAnswers =
    EmailVerificationAnswers.EmailVerified(email, emailVerificationSuccess)

  val emailVerificationAnswersLocked: EmailVerificationAnswers =
    EmailVerificationAnswers.EmailVerified(email, emailVerificationLocked)

  def emailVerificationAnswers(status: Option[EmailVerificationResult]): EmailVerificationAnswers = status match {
    case Some(EmailVerificationResult.Verified) => emailVerificationAnswersSuccess
    case Some(EmailVerificationResult.Locked)   => emailVerificationAnswersLocked
    case None                                   => emailVerificationAnswersEmailNotNeeded
  }

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

  val pegaCaseId = PegaCaseId("case-id")

  val pegaOauthToken = PegaOauthToken("access", "type", 123456L)

  val pegaStartCaseResponse = PegaStartCaseResponse(pegaCaseId.value)

  def pegaGetCaseResponse(
    dayOfMonth:  DayOfMonth,
    paymentPlan: PaymentPlan,
    expenditure: Map[String, String],
    income:      Map[String, String]
  ) = {

    val initialCollectionJsonString = paymentPlan.collections.initialCollection
      .map(c => s"""
        |"initialCollection": {
        |  "amountDue": ${c.amountDue.value.value.toString},
        |  "dueDate": "${c.dueDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        |},
        |""".stripMargin)
      .getOrElse("")

    def regularCollectionsJsonString = paymentPlan.collections.regularCollections
      .map(c => s"""{
         |  "amountDue": ${c.amountDue.value.value.toString},
         |  "dueDate": "${c.dueDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
         |}
         |""".stripMargin)
      .mkString(", ")

    def instalmentsJsonString = paymentPlan.instalments
      .map(i => s"""
        |{
        |  "amountDue": ${i.amountDue.value.value.toString},
        |  "debtItemChargeId": "${i.debtItemChargeId.value}",
        |  "debtItemOriginalDueDate": "${i.debtItemOriginalDueDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
        |  "dueDate": "${i.dueDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
        |  "instalmentBalance": ${i.instalmentBalance.value.value.toString},
        |  "instalmentInterestAccrued": ${i.instalmentInterestAccrued.value.value.toString},
        |  "instalmentNumber": ${i.instalmentNumber.value.toString}
        |}
        |""".stripMargin)
      .mkString(", ")

    val expenditureJsonString = {
      val items = expenditure
        .map { case (k, v) =>
          s"""{
           |  "pyLabel": "$k",
           |  "amountValue": "$v"
           |}""".stripMargin
        }
        .mkString(",")

      s"[ $items ]"
    }

    val incomeJsonString = {
      val items = income
        .map { case (k, v) =>
          s"""{
           |  "pyLabel": "$k",
           |  "amountValue": "$v"
           |}""".stripMargin
        }
        .mkString(",")

      s"[ $items ]"
    }

    s"""
       |{
       |  "AA": {
       |    "paymentDay": "${dayOfMonth.value.toString}",
       |    "paymentPlan": [
       |      {
       |        "planDuration": ${paymentPlan.planDuration.value.toString},
       |        "planSelected": true,
       |        "numberOfInstalments": ${paymentPlan.numberOfInstalments.value.toString},
       |        "totalDebt": ${paymentPlan.totalDebt.value.value.toString},
       |        "totalDebtIncInt": ${paymentPlan.totalDebtIncInt.value.value.toString},
       |        "planInterest": ${paymentPlan.planInterest.value.value.toString},
       |        "collections": {
       |          $initialCollectionJsonString
       |          "regularCollections": [ $regularCollectionsJsonString ]
       |        },
       |        "instalments": [ $instalmentsJsonString ]
       |      },
       |      {
       |        "planDuration": 0,
       |        "planSelected": false,
       |        "numberOfInstalments": 0,
       |        "totalDebt": 0,
       |        "totalDebtIncInt": 0,
       |        "planInterest": 0,
       |        "collections": {
       |          "initialCollection": {
       |            "amountDue": 0,
       |            "dueDate": "1932-12-04"
       |          },
       |          "regularCollections": []
       |        },
       |        "instalments": []
       |      }
       |    ],
       |    "expenditure": $expenditureJsonString,
       |    "income": $incomeJsonString
       |  }
       |}
       |""".stripMargin
  }

  val startCaseResponse = StartCaseResponse(pegaCaseId, "pegaCorrelationId")

}
