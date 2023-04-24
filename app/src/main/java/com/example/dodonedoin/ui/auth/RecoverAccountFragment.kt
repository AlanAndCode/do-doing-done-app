package com.example.dodonedoin.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.dodonedoin.R
import com.example.dodonedoin.databinding.FragmentRecoverAccountBinding
import com.example.dodonedoin.helper.BaseFragment
import com.example.dodonedoin.helper.FirebaseHelper
import com.example.dodonedoin.helper.initToolbar
import com.example.dodonedoin.helper.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RecoverAccountFragment : BaseFragment() {

    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)

        auth = Firebase.auth

        initClicks()
    }

    private fun initClicks() {
        binding.btnSend.setOnClickListener { validateData() }
    }

    private fun validateData() {
        val email = binding.edtEmail.text.toString().trim()

        if (email.isNotEmpty()) {
            hideKeyboard()

            binding.progressBar.isVisible = true

            recoverAccountUser(email)
        } else {
            showBottomSheet(message = R.string.text_email_empty_recover_account_fragment)
        }
    }

    private fun recoverAccountUser(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    showBottomSheet(
                        message = R.string.text_email_send_sucess_recover_account_fragment
                    )
                } else {
                    showBottomSheet(
                        message = FirebaseHelper.validError(task.exception?.message ?: "")
                    )
                }

                binding.progressBar.isVisible = false
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}