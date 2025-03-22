package net.nutrishare.app.view.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentEditPostBinding
import net.nutrishare.app.model.Post
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.viewmodel.PostViewModel
import java.util.UUID


class EditPostFragment : Fragment() {
   private lateinit var binding:FragmentEditPostBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private lateinit var postViewModel: PostViewModel
    private var user: User?=null
    private var post: Post? = null

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
        binding = FragmentEditPostBinding.inflate(inflater, container, false)

        val args: EditPostFragmentArgs by navArgs()
        post = args.post

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayPostDetails()
        observeViewModel()
        setUpListeners()
    }

    private fun displayPostDetails(){

        binding.apply {

            post?.let {
                descriptionInput.setText(it.description)
                productLinkInput.setText(it.productLink)

                if (it.postImage!!.isNotEmpty()){
                    Picasso.get().load(it.postImage)
                        .placeholder(R.drawable.placeholder)
                        .resize(600,400)
                        .centerCrop()
                        .into(imagePreview)
                }
            }
        }

    }

    private fun observeViewModel(){

        postViewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.submitPostButton.isEnabled = true
                binding.selectImageButton.isEnabled = true
                findNavController().navigateUp()
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
                post!!.description = description
                post!!.productLink = productLink
                post!!.userName = "${user?.userName}"
                post!!.userImage = "${user?.profileImageUrl}"

                postViewModel.updatePost(post!!, selectedImageUri)
            }

        }
    }

}