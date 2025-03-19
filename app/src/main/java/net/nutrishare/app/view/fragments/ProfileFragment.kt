package net.nutrishare.app.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.databinding.FragmentProfileBinding
import net.nutrishare.app.factory.AuthViewModelFactory
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.view.activities.AuthActivity
import net.nutrishare.app.viewmodel.AuthViewModel


class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private var user:User?=null
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayUserInfo()
        setupListeners()
    }

    private fun displayUserInfo(){
       if (user != null){

           if (user?.profileImageUrl!!.isNotEmpty()){
               Picasso.get().load(user?.profileImageUrl)
                   .placeholder(R.drawable.placeholder)
                   .resize(200,200)
                   .centerCrop()
                   .into(binding.userImage)
           }
           binding.username.text = user?.userName

       }
    }

    private fun setupListeners(){

        binding.editButton.setOnClickListener {  }

        binding.logoutButton.setOnClickListener {
           authViewModel.logout()
           SessionManager.clearUser()
           startActivity(Intent(requireActivity(),AuthActivity::class.java).apply {
               flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
           }).apply {
               requireActivity().finish()
           }
        }

    }

}