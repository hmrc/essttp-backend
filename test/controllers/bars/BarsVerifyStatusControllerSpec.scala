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

package controllers.bars

import essttp.bars.BarsVerifyStatusConnector
import essttp.bars.model.{BarsVerifyStatusResponse, NumberOfBarsVerifyAttempts}
import essttp.rootmodel.EmpRef
import testsupport.{FrozenTime, ItSpec}
import testsupport.Givens.{canEqualInstant, canEqualJsValue}

import java.time.temporal.ChronoUnit

class BarsVerifyStatusControllerSpec extends ItSpec {

  def connector: BarsVerifyStatusConnector = app.injector.instanceOf[BarsVerifyStatusConnector]

  abstract class Setup(numberUpdates: Int) extends BarsVerifyStatusItTest {
    stubCommonActions()

    private val empRef         = "123XYZ456"
    private val expectedExpiry = FrozenTime.getClock.instant().plus(24, ChronoUnit.HOURS)

    for (_ <- 1 to numberUpdates)
      connector.update(EmpRef(empRef)).futureValue

    def assertBarsVerifyStatusResponse(): Unit = {
      val result = connector.status(EmpRef(empRef)).futureValue
      if (numberUpdates < 3) {
        result shouldBe BarsVerifyStatusResponse(
          attempts = NumberOfBarsVerifyAttempts(numberUpdates),
          lockoutExpiryDateTime = None
        )
      } else {
        result.attempts shouldBe NumberOfBarsVerifyAttempts(numberUpdates)
        result.lockoutExpiryDateTime shouldBe Some(expectedExpiry)
      }

      verifyCommonActions(numberOfAuthCalls = numberUpdates + 1)
    }
  }

  "POST /bars/verify/status" - {
    "when no BARs verify calls have been made" - {
      "should return the empty BarsVerifyStatusResponse" in new Setup(0) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/update" - {
    "when no BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 1" in new Setup(0) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/status" - {
    "after one BARs verify call update has been made" - {
      "should return BarsVerifyStatusResponse with a count of 1" in new Setup(1) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/status" - {
    "after two BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 2" in new Setup(2) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/status" - {
    "after three BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 3 and an expiry time" in new Setup(3) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/status" - {
    "after four BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 4 and an expiry time" in new Setup(4) {
        assertBarsVerifyStatusResponse()
      }
    }
  }

  "POST /bars/verify/status" - {
    "is dependent on taxId" in new BarsVerifyStatusItTest {
      stubCommonActions()

      private val taxIdUnderTest = "taxId"
      // initial status
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe NumberOfBarsVerifyAttempts(0)
      // after other update
      connector.update(EmpRef("taxId-OTHER")).futureValue
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe NumberOfBarsVerifyAttempts(0)
      // after correct update
      connector.update(EmpRef(taxIdUnderTest)).futureValue
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe NumberOfBarsVerifyAttempts(1)

      verifyCommonActions(numberOfAuthCalls = 5)
    }
  }

  "POST /bars/verify/update" - {
    "is dependent on taxId" in new BarsVerifyStatusItTest {
      stubCommonActions()

      private val taxIdUnderTest = "taxId"
      // first update for taxId under test
      connector.update(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe NumberOfBarsVerifyAttempts(1)
      // other updates
      connector.update(EmpRef("taxId-OTHER")).futureValue
      connector.update(EmpRef("taxId-OTHER")).futureValue

      // second update for taxId under test
      connector.update(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe NumberOfBarsVerifyAttempts(2)
    }
  }
}
