package io.xacml.pep.json.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DefaultContextConfiguration {

  public static SSLContext getContext() {
    SSLContext sslcontext = null;
    try {

      sslcontext = SSLContext.getInstance("TLS");

      sslcontext.init(null, new TrustManager[] {new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      }}, new java.security.SecureRandom());


      HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      e.printStackTrace();
    }
    return sslcontext;
  }

  public static HostnameVerifier getHostnameVerifier() {
    return new HostnameVerifier() {
      @Override
      public boolean verify(String hostname, SSLSession session) {
        return true;
      }
    };
  }
}
