package httpssetup

import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object HttpsSetup {

  def getHttpsContext : HttpsConnectionContext = {
  //Step 1: Key Store
  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keyStoreFile: InputStream  = getClass.getClassLoader.getResourceAsStream("keystore.pkcs12")
  val password = "akka-https".toCharArray
  ks.load(keyStoreFile,password)

  //Step 2 initialize manager
  val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks,password)

  //Step 3 Initialize trust manager
  val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  trustManagerFactory.init(ks)

  //Step 4 Initialize SSL context
  val sslContext : SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)

  //Step 5: return the https connection context
  //val httpsConnectionContext: HttpsConnectionContext = ConnectionContext.https(sslContext)
  ConnectionContext.https(sslContext)

  }

}
