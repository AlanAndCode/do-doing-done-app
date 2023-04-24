package com.example.dodonedoin.ui.auth

import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.dodonedoin.R
import com.example.dodonedoin.databinding.FragmentLoginBinding
import com.example.dodonedoin.helper.FirebaseHelper
import com.example.dodonedoin.helper.showBottomSheet
import com.example.dodonedoin.helper.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import javax.crypto.Cipher

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {

        binding.btnLogin.setOnClickListener { validateData() }
        binding.biometryButton.setOnClickListener { loginWithBio() }
        binding.textCC.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.textFP.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }
    }


    private  fun promptBiometricChecker(
        title: String,
        message: String? = null, // OPCIONAL - SE QUISER EXIBIR UMA MENSAGEM
        negativeLabel: String,
        confirmationRequired: Boolean = true,
        initializedCipher: Cipher? = null, // OPICIONAL - SE VC MESMO(SUA APP) QUISER MANTER O CONTROLE SOBRE OS ACESSOS
        onAuthenticationSuccess: (androidx.biometric.BiometricPrompt.AuthenticationResult) -> Unit,
        onAuthenticationError: (Int, String) -> Unit
    )

    {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val prompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    Timber.d("Authenticado com sucesso, acesso permitido!")
                    onAuthenticationSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence) {
                    Timber.d("Acesso negado! Alguem ta tentando usar teu celular!")
                    onAuthenticationError(errorCode, errorMessage.toString())
                }
            })

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { if (message != null) setDescription(message) }
            .setConfirmationRequired(confirmationRequired)
            .setNegativeButtonText(negativeLabel)
            .build()

        initializedCipher?.let {
            val cryptoObject = androidx.biometric.BiometricPrompt.CryptoObject(initializedCipher)
            prompt.authenticate(promptInfo, cryptoObject)
            return
        }

        prompt.authenticate(promptInfo)
    }


    private fun loginWithBio(){
        binding.biometryButton.setOnClickListener {
            promptBiometricChecker(
                "Desbloqueia Por Favor",
                "Use Seu FingerPrint",
                "Use account password",
                confirmationRequired = true,
                null,
                { task ->
                    when (task.authenticationType) {
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                            toast("sucesso fingerprint or face!")
                            FirebaseAuth.getInstance()
                                        findNavController().navigate(R.id.action_global_homeFragment)
                                    }
                        androidx.biometric.BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN -> {
                            toast("sucesso por meio legado ou desconhecido")
                        }
                        androidx.biometric.BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                            toast("sucesso pin, pattern or password")
                        }
                    }
                }
            ) { error, errorMsg ->
                toast("$error: $errorMsg")
            }
        }

    }


    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPass.text.toString().trim()

        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {



                binding.progressBar.isVisible = true

                loginUser(email, password)

            } else {
                showBottomSheet(
                    message = R.string.text_password_empty_login_fragment
                )
            }
        } else {
            showBottomSheet(
                message = R.string.text_email_empty_login_fragment
            )
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                } else {

                    showBottomSheet(
                        message = FirebaseHelper.validError(task.exception?.message ?: "")
                    )
                    binding.progressBar.isVisible = false
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}