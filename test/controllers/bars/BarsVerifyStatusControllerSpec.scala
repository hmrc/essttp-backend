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
import testsupport.ItSpec

class BarsVerifyStatusControllerSpec extends ItSpec {

  def connector: BarsVerifyStatusConnector = app.injector.instanceOf[BarsVerifyStatusConnector]

  "POST /bars/verify/status" - {
    "when no BARs verify calls have been made" - {
      "should return the empty BarsVerifyStatusResponse" in new BarsVerifyStatusItTest {
        val result = connector.status(EmpRef("empRef")).futureValue
        result shouldBe BarsVerifyStatusResponse(attempts              = 0, lockoutExpiryDateTime = None)
      }
    }
  }

  "POST /bars/verify/update" - {
    "when no BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 1" in new BarsVerifyStatusItTest {
        val result = connector.update(EmpRef("empRef")).futureValue
        result shouldBe BarsVerifyStatusResponse(attempts              = 1, lockoutExpiryDateTime = None)
      }
    }
  }

  "POST /bars/verify/status" - {
    "after one BARs verify call update has been made" - {
      "should return BarsVerifyStatusResponse with a count of 1" in new BarsVerifyStatusItTest {
        connector.update(EmpRef("empRef")).futureValue
        val result = connector.status(EmpRef("empRef")).futureValue
        result shouldBe BarsVerifyStatusResponse(attempts              = 1, lockoutExpiryDateTime = None)
      }
    }
  }

  "POST /bars/verify/status" - {
    "after two BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 2" in new BarsVerifyStatusItTest {
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        val result = connector.status(EmpRef("empRef")).futureValue
        result shouldBe BarsVerifyStatusResponse(attempts              = 2, lockoutExpiryDateTime = None)
      }
    }
  }

  "POST /bars/verify/status" - {
    "after three BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 3 and an expiry time" in new BarsVerifyStatusItTest {
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        val result: BarsVerifyStatusResponse = connector.status(EmpRef("empRef")).futureValue

        result.attempts shouldBe 3
        result.lockoutExpiryDateTime.isDefined shouldBe true
      }
    }
  }

  "POST /bars/verify/status" - {
    "after four BARs verify call updates have been made" - {
      "should return BarsVerifyStatusResponse with a count of 4 and an expiry time" in new BarsVerifyStatusItTest {
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        connector.update(EmpRef("empRef")).futureValue
        val result: BarsVerifyStatusResponse = connector.status(EmpRef("empRef")).futureValue

        result.attempts shouldBe 4
        result.lockoutExpiryDateTime.isDefined shouldBe true
      }
    }
  }
}
