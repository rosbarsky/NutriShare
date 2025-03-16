package net.nutrishare.app.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.nutrishare.app.R
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.data.AuthResult
import net.nutrishare.app.databinding.FragmentLoginBinding
import net.nutrishare.app.factory.AuthViewModelFactory
import net.nutrishare.app.view.activities.MainActivity
import net.nutrishare.app.viewmodel.AuthViewModel


class LoginFragment : Fragment() {

    private lateinit var binding:FragmentLoginBinding
    private lateinit var authViewModel:AuthViewModel

            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentLoginBinding.inflate(inflater, container, false)
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
            binding.btnLogin.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupListeners() {

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(validation(email,password)){
                binding.btnLogin.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                authViewModel.login(email, password)
            }

        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validation(email:String,password:String):Boolean{
       if (email.isEmpty()){
            Toast.makeText(requireContext(),"Email field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (password.isEmpty()){
            Toast.makeText(requireContext(),"Password field cannot be empty!",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}