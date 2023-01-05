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

package crypto

import essttp.crypto.{Crypto, CryptoFormat}
import essttp.rootmodel.bank.AccountName
import play.api.Configuration
import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class CryptoFormatsSpec extends UnitSpec {

  "sensitiveStringFormat should format correctly" - {
    "CryptoFormat.OperationalCryptoFormat" in {
      implicit val cryptoFormat: CryptoFormat = CryptoFormat.OperationalCryptoFormat(new Crypto(Configuration("crypto.encryption-key" -> "P5xsJ9Nt+quxGZzB4DeLfw==")))
      val stringToBeEncrypted = "Bob Ross"
      val encryptedAccountName = AccountName(SensitiveString(stringToBeEncrypted))

      val jsValue = Json.toJson(encryptedAccountName)
      jsValue.toString shouldNot include("Bob Ross")

      // encrypted strings seem to always be length 58
      jsValue.toString().length shouldBe 58
      jsValue.toString().length shouldNot be(stringToBeEncrypted.length)

      encryptedAccountName.value.decryptedValue shouldBe "Bob Ross"
    }
    "CryptoFormat.NoOpCryptoFormat should return a JsString" in {
      implicit val cryptoFormat: CryptoFormat = CryptoFormat.NoOpCryptoFormat
      val encryptedAccountName = AccountName(SensitiveString("Bob Ross"))
      Json.toJson(encryptedAccountName) shouldBe JsString("Bob Ross")
      encryptedAccountName.value.decryptedValue shouldBe "Bob Ross"
    }
  }

}
