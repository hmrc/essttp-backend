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

package essttp.crypto

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainBytes, PlainContent, PlainText, SymmetricCryptoFactory}

@Singleton
class Crypto @Inject() (configuration: Configuration) extends Encrypter, Decrypter {

  val aesGcmCryptoKey: String = configuration.get[String]("crypto.encryption-key")

  given aesCrypto: (Encrypter & Decrypter) = SymmetricCryptoFactory.aesGcmCrypto(aesGcmCryptoKey)

  override def encrypt(plain: PlainContent): Crypted = aesCrypto.encrypt(plain)

  override def decrypt(reversiblyEncrypted: Crypted): PlainText = aesCrypto.decrypt(reversiblyEncrypted)

  override def decryptAsBytes(reversiblyEncrypted: Crypted): PlainBytes = aesCrypto.decryptAsBytes(reversiblyEncrypted)
}
