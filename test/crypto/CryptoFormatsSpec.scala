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
      jsValue shouldNot be("Bob Ross")
      jsValue shouldNot be(JsString("Bob Ross"))

      // encrypted strings seem to always be length 58
      jsValue.toString().length shouldBe 58
      jsValue.toString().length shouldNot be(stringToBeEncrypted.length)

      encryptedAccountName.value.decryptedValue shouldBe "Bob Ross"
    }
    "CryptoFormat.NoOpCryptoFormat should return a JsString" in {
      implicit val cryptoFormat: CryptoFormat = CryptoFormat.NoOpCryptoFormat
      val encryptedAccountName = AccountName(SensitiveString("Bob Ross"))
      println(encryptedAccountName)
      Json.toJson(encryptedAccountName) shouldBe JsString("Bob Ross")
      encryptedAccountName.value.decryptedValue shouldBe "Bob Ross"
    }
  }

}
