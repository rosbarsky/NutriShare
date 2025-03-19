package net.nutrishare.app.view.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import net.nutrishare.app.R
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentCreatePostBinding
import net.nutrishare.app.model.Post
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.viewmodel.PostViewModel
import java.util.UUID

class CreatePostFragment : Fragment() {

    private lateinit var binding:FragmentCreatePostBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private lateinit var postViewModel: PostViewModel
    private var user:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.imagePreview.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
        if (user == null){
            user = SessionManager.getUser()
        }
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference
        val repository = PostRepository(postDao, firestore,storage,null)

        postViewModel = PostViewModel(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentCreatePostBinding.inflate(inflater, container, false)
      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpListeners()
    }

    private fun observeViewModel(){

        postViewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.submitPostButton.isEnabled = true
                binding.selectImageButton.isEnabled = true
                resetForm()
            }.onFailure { exception ->
                binding.progressBar.visibility = View.GONE
                binding.submitPostButton.isEnabled = true
                binding.selectImageButton.isEnabled = true
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setUpListeners(){

        binding.selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.submitPostButton.setOnClickListener {
          val description = binding.descriptionInput.text.toString()
          val productLink = binding.productLinkInput.text.toString()

          if (description.isEmpty()){
              Toast.makeText(requireContext(),"Description field cannot be empty!",Toast.LENGTH_SHORT).show()
          }
            else{
              binding.progressBar.visibility = View.VISIBLE
              binding.submitPostButton.isEnabled = false
              binding.selectImageButton.isEnabled = false
              val post = Post(
                  postId = UUID.randomUUID().toString(),
                  description = description,
                  productLink = productLink,
                  postImage = "", // Will be updated later if an image is selected
                  userId = "${user?.userId}",
                  userName = "${user?.userName}",
                  userImage = "${user?.profileImageUrl}"
              )
              postViewModel.createPost(post, selectedImageUri)
          }

        }
    }

    private fun resetForm(){
        binding.descriptionInput.setText("")
        binding.productLinkInput.setText("")
        binding.imagePreview.setImageResource(R.drawable.placeholder)
        selectedImageUri = null
    }
}