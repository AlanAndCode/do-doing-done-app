package com.example.dodonedoin.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dodonedoin.R
import com.example.dodonedoin.databinding.FragmentFormTaskBinding
import com.example.dodonedoin.helper.BaseFragment
import com.example.dodonedoin.helper.FirebaseHelper
import com.example.dodonedoin.helper.initToolbar
import com.example.dodonedoin.helper.showBottomSheet
import com.example.dodonedoin.ui.model.Task
import java.util.*

class FormTaskFragment : BaseFragment() {

    private val args: FormTaskFragmentArgs by navArgs()

    private lateinit var  timePickerDialog: TimePickerDialog
    private lateinit var calendario : Calendar
    private var horaAtual = 0
    private var minutosAtuais = 0
    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private var newTask: Boolean = true
    private var statusTask: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)

        initListeners()

        getArgs()
    }

    private fun getArgs() {
        args.task.let {
            if (it != null) {
                task = it
                configTask()
            }
        }
    }
    private fun configTask() {
        newTask = false
        binding.textToolbar.text = getString(R.string.text_editing_task_form_task_fragment)
        statusTask = task.status

        binding.edtDescription.setText(task.description)
        setStatus()
    }

    private fun setStatus() {
        binding.radioGroup.check(
            when (task.status) {
                0 -> {
                    R.id.rbTodo
                }
                1 -> {
                    R.id.rbDoing
                }
                else -> {
                    R.id.rbDone
                }
            }
        )
    }

   private fun initListeners() {

        binding.btnSave.setOnClickListener { validateData() }

        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            statusTask = when (id) {
                R.id.rbTodo -> 0
                R.id.rbDoing -> 1
                else -> 2
            }
        }

        binding.Lembrete.setOnClickListener {

            calendario = Calendar.getInstance()
            horaAtual = calendario.get(Calendar.HOUR_OF_DAY)
            minutosAtuais = calendario.get(Calendar.MINUTE)
            timePickerDialog = TimePickerDialog(activity, { timePicker: TimePicker, hourOfDay: Int, minutes: Int ->
                binding.textHora.text = String.format("%02d", hourOfDay)
                binding.Minutos.text = String.format("%02d", minutes)

            }, horaAtual, minutosAtuais, true)
            timePickerDialog.show();
        }

        binding.Alarme.setOnClickListener{

            val textHora = binding.textHora
            val Minutos = binding.Minutos

            if(!binding.textHora.toString().isEmpty() && !binding.Minutos.text.toString().isEmpty()){
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                intent.putExtra(AlarmClock.EXTRA_HOUR, textHora.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MINUTES, Minutos.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, binding!!.edtDescription.text.toString())

               val  packageManager = getActivity()?.getPackageManager()
                if (packageManager?.let { it1 -> intent.resolveActivity(it1) } != null) {
                    startActivity(intent)
                }

            }
        }

    }



    private fun validateData() {
        val description = binding.edtDescription.text.toString().trim()
        val hour = binding.textHora.text.toString().trim()
        val minutes = binding.Minutos.text.toString().trim()


        if (description.isNotEmpty()) {

            hideKeyboard()

            binding.progressBar.isVisible = true

            if (newTask) task = Task()
            task.description = description
            task.status = statusTask
            task.hour = hour
            task.minutes = minutes

            saveTask()
        } else {
            showBottomSheet(message = R.string.text_description_empty_form_task_fragment)
        }
    }

    private fun saveTask() {
        FirebaseHelper
            .getDatabase()
            .child("do-done-doing")
            .child(FirebaseHelper.getIdUser() ?: "")
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (newTask) { // Nova tarefa
                        findNavController().popBackStack()
                        Toast.makeText(
                            requireContext(),
                            R.string.text_save_task_sucess_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else { // Editando tarefa
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            R.string.text_update_task_sucess_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.text_erro_save_task_form_task_fragment, Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                binding.progressBar.isVisible = false
                Toast.makeText(requireContext(), R.string.text_erro_save_task_form_task_fragment, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}