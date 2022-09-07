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

package controllers.bars

import essttp.bars.BarsVerifyStatusConnector
import essttp.bars.model.BarsVerifyStatusResponse
import essttp.rootmodel.EmpRef
import org.scalatest.Assertion
import testsupport.ItSpec

import java.time.temporal.ChronoUnit

class BarsVerifyStatusControllerSpec extends ItSpec {

  def connector: BarsVerifyStatusConnector = app.injector.instanceOf[BarsVerifyStatusConnector]

  abstract class Setup(numberUpdates: Int) extends BarsVerifyStatusItTest {
    private val empRef = "123XYZ456"
    private val expectedExpiry = frozenZonedDateTime.toInstant.plus(24, ChronoUnit.HOURS)

    for (n <- 1 to numberUpdates) {
      connector.update(EmpRef(empRef)).futureValue
    }

    def assertBarsVerifyStatusResponse(): Assertion = {
      val result = connector.status(EmpRef(empRef)).futureValue
      if (numberUpdates < 3) {
        result shouldBe BarsVerifyStatusResponse(attempts              = numberUpdates, lockoutExpiryDateTime = None)
      } else {
        result.attempts shouldBe numberUpdates
        result.lockoutExpiryDateTime shouldBe Some(expectedExpiry)
      }
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
      private val taxIdUnderTest = "taxId"
      // initial status
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe 0
      // after other update
      connector.update(EmpRef("taxId-OTHER")).futureValue
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe 0
      // after correct update
      connector.update(EmpRef(taxIdUnderTest)).futureValue
      connector.status(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe 1
    }
  }

  "POST /bars/verify/update" - {
    "is dependent on taxId" in new BarsVerifyStatusItTest {
      private val taxIdUnderTest = "taxId"
      // first update for taxId under test
      connector.update(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe 1
      // other updates
      connector.update(EmpRef("taxId-OTHER")).futureValue
      connector.update(EmpRef("taxId-OTHER")).futureValue

      // second update for taxId under test
      connector.update(EmpRef(taxIdUnderTest)).futureValue.attempts shouldBe 2
    }
  }
}
