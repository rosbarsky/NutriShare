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
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.data.AuthResult
import net.nutrishare.app.databinding.FragmentEditUserDetailBinding
import net.nutrishare.app.factory.AuthViewModelFactory
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.view.activities.MainActivity
import net.nutrishare.app.viewmodel.AuthViewModel


class EditUserDetailFragment : Fragment() {

    private lateinit var binding:FragmentEditUserDetailBinding
    private var user:User?=null
    private lateinit var authViewModel: AuthViewModel
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.profileImageView.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

        if (user == null){
            user = SessionManager.getUser()
        }
        val authRepository = AuthRepository()
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this,factory)[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        bindUserDetails()
        setupListeners()

    }

    private fun observeViewModel() {

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> {
                  if (result.message.contains("Profile image updated")){
                      authViewModel.updateUserName("${user?.userId}",binding.usernameEditText.text.toString())
                  }
                    else{
                      Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                      findNavController().navigateUp()
                  }
                }
                is AuthResult.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.updateImageButton.isEnabled = true
                    binding.usernameEditText.isEnabled = true
                    binding.saveButton.isEnabled = true
                    Toast.makeText(requireContext(), result.exception.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun setupListeners(){

        binding.updateImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.saveButton.setOnClickListener {
           binding.progressBar.visibility = View.VISIBLE
           binding.updateImageButton.isEnabled = false
           binding.usernameEditText.isEnabled = false
           binding.saveButton.isEnabled = false
           if (selectedImageUri != null){
               authViewModel.updateProfileImage("${user?.userId}",selectedImageUri!!)
           }
            else{
               authViewModel.updateUserName("${user?.userId}",binding.usernameEditText.text.toString())
           }

        }

    }

    private fun bindUserDetails()
    {
        if (user != null){

            if (user?.profileImageUrl!!.isNotEmpty()){
                Picasso.get().load(user?.profileImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .resize(200,200)
                    .centerCrop()
                    .into(binding.profileImageView)
            }
            binding.usernameEditText.setText(user?.userName)

        }
    }
}