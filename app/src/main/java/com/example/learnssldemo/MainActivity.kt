package com.example.learnssldemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.learnssldemo.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val KEY_STORE_PASSWORD = "123456" // 客户端证书密码


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            thread {
                test()
//                val url = URL("https://192.168.38.70:8080/test/xx")
//                val url = URL("https://baidu.com")
//                val urlConnection = url.openConnection() as HttpsURLConnection
//                urlConnection.connect()
//                val readBytes = urlConnection.inputStream.readBytes()
//                Log.e("test", "${String(readBytes)}")
            }

        }
    }

    fun test() {


        //服务端证书固定代码
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = BufferedInputStream(this.resources.assets.open("your-cert.crt"))
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out.println("ca=" + ca.subjectDN)

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("baeldung", ca)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }



        //客户端证书导入
        val keyStore2 = KeyStore.getInstance("BKS");
        val ksIn = this.resources.getAssets().open("cbaeldung.bks");
        ksIn.use {ksIn->
            keyStore2.load(ksIn, KEY_STORE_PASSWORD.toCharArray())
        }
        val keyManagerFactory = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keyStore2, KEY_STORE_PASSWORD.toCharArray());


        // Create an SSLContext that uses our TrustManager
        val context: SSLContext = SSLContext.getInstance("TLS").apply {
            init(
                keyManagerFactory.keyManagers,//开启客户端证书
                tmf.trustManagers, //服务端证书固定
                null)
        }

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        val url = URL("https://192.168.38.70:8080/test/xx")
        val urlConnection = url.openConnection() as HttpsURLConnection
        urlConnection.sslSocketFactory = context.socketFactory
        urlConnection.connect()
        val inputStream: InputStream = urlConnection.inputStream

        Log.e("test", "${String(inputStream.readBytes())}")
    }
}