package net.nutrishare.app.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.nutrishare.app.R
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.data.AuthResult
import net.nutrishare.app.databinding.FragmentRegisterBinding
import net.nutrishare.app.factory.AuthViewModelFactory
import net.nutrishare.app.view.activities.MainActivity
import net.nutrishare.app.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private lateinit var binding:FragmentRegisterBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProfileImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authRepository = AuthRepository()
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this,factory)[AuthViewModel::class.java]

        observeViewModel()
        setupListeners()

    }

    private fun observeViewModel() {
        // Observe LiveData from the ViewModel
        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            // Handle auth results
            when (result) {
                is AuthResult.Success -> {
                    startActivity(Intent(requireActivity(), MainActivity::class.java)).apply {
                        requireActivity().finish()
                    }
                }
                is AuthResult.Failure -> {
                    Toast.makeText(requireContext(), result.exception.message, Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupListeners() {

        binding.ivUploadImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validation(username,email,password,confirmPassword)){
                binding.btnSignUp.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                authViewModel.register(email,password,username,null)
            }

        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validation(username:String,email:String,password:String,confirmPassword:String):Boolean{
        if (username.isEmpty()){
            Toast.makeText(requireContext(),"Username field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (email.isEmpty()){
            Toast.makeText(requireContext(),"Email field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (password.isEmpty()){
            Toast.makeText(requireContext(),"Password field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (confirmPassword.isEmpty()){
            Toast.makeText(requireContext(),"Confirm Password field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (password != confirmPassword){
            Toast.makeText(requireContext(),"Password and Confirm Password doesn't the same!",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}