package com.example.dodonedoin.helper


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.annotation.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import javax.crypto.Cipher

private lateinit var auth: FirebaseAuth

/** EXIBE UMA MENSAGEM SIMPLES TEMPORARIZADA NA TELA DO CELULAR */
fun Fragment.toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()



fun Fragment.showYoutubeVideo(videoId: String) {
    val openURL = Intent(Intent.ACTION_VIEW)
    openURL.data = Uri.parse("https://youtu.be/$videoId")
    startActivity(openURL)
}




/** VERIFICA SE TEM REDE E SE TEM ACESSO A INTERNET: https://youtu.be/DpyxLwibE0M  */
fun Fragment.hasInternet(): Boolean {
    val connMgr =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
        capabilities != null &&
                // verifica se você tem rede ex: WIFI etc.
                capabilities.hasCapability(NET_CAPABILITY_INTERNET) &&
                // e realmetne consegue fazer requisições, pois em alguns casos
                // ex. aeroporto vc esta conectado, porem ainda não foi liberado
                // e por isso não tem rede
                capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
    } else {
        @Suppress("DEPRECATION")
        connMgr.activeNetworkInfo?.isConnected == true
    }
}

/** EXIBE UM LEITOR DE BIOMETRIA: XXXXXXX */
fun Fragment.promptBiometricChecker(
    title: String,
    message: String? = null, // OPCIONAL - SE QUISER EXIBIR UMA MENSAGEM
    negativeLabel: String,
    confirmationRequired: Boolean = true,
    initializedCipher: Cipher? = null, // OPICIONAL - SE VC MESMO(SUA APP) QUISER MANTER O CONTROLE SOBRE OS ACESSOS
    onAuthenticationSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    onAuthenticationError: (Int, String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(requireContext())
    val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {

            Timber.d("Authenticado com sucesso, acesso permitido!")
            onAuthenticationSuccess(result)
        }

        override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence) {
            Timber.d("Acesso negado! Alguem ta tentando usar teu celular!")
            onAuthenticationError(errorCode, errorMessage.toString())
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .apply { if (message != null) setDescription(message) }
        .setConfirmationRequired(confirmationRequired)
        .setNegativeButtonText(negativeLabel)
        .build()

    initializedCipher?.let {
        val cryptoObject = BiometricPrompt.CryptoObject(initializedCipher)
        prompt.authenticate(promptInfo, cryptoObject)
        return
    }

    prompt.authenticate(promptInfo)
}

