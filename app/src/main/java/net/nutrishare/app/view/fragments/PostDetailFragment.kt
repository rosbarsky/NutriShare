package net.nutrishare.app.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentPostDetailBinding
import net.nutrishare.app.model.Post
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.viewmodel.PostViewModel

class PostDetailFragment : Fragment() {

    private lateinit var binding:FragmentPostDetailBinding
    private var post: Post? = null
    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        val args: PostDetailFragmentArgs by navArgs()
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
               if (it.userImage.isNotEmpty()){
                   Picasso.get().load(it.userImage)
                       .placeholder(R.drawable.placeholder)
                       .resize(200,200)
                       .centerCrop()
                       .into(postUserImageView)
               }
               postUserNameView.text = it.userName
               if (it.productLink.isNotEmpty()){
                   postProductLinkView.visibility = View.VISIBLE
               }
               else{
                   postProductLinkView.visibility = View.GONE
               }
               if (it.postImage!!.isEmpty()) {
                   postImageView.visibility = View.GONE
               } else {
                   postImageView.visibility = View.VISIBLE
                   Picasso.get().load(it.postImage)
                       .placeholder(R.drawable.placeholder)
                       .resize(600,400)
                       .centerCrop()
                       .into(postImageView)
               }
               postProductLinkView.text = it.productLink
               postDescriptionView.text = it.description

               if (it.userId == SessionManager.getUser()?.userId){
                   postEditView.visibility = View.VISIBLE
                   postDeleteView.visibility = View.VISIBLE
               }
               else{
                   postEditView.visibility = View.GONE
                   postDeleteView.visibility = View.GONE
               }

               if (it.isFavorite){
                   postFavouriteView.setImageResource(R.drawable.ic_favourite)
               }
               else{
                   postFavouriteView.setImageResource(R.drawable.ic_unfavourite)
               }

           }
        }
    }

    private fun observeViewModel(){

        postViewModel.operationStatus.observe(viewLifecycleOwner){ result->
            result.onSuccess { message ->
                if (message.contains("Post deleted successfully")){
                    findNavController().navigateUp()
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpListeners(){
        binding.postFavouriteView.setOnClickListener {
            val userId = "${SessionManager.getUser()?.userId}"
            if (post!!.isFavorite){
                postViewModel.unfavoritePost(userId, post!!)
                binding.postFavouriteView.setImageResource(R.drawable.ic_unfavourite)
                post!!.isFavorite = false
            }
            else{
                postViewModel.favoritePost(userId, post!!)
                binding.postFavouriteView.setImageResource(R.drawable.ic_favourite)
                post!!.isFavorite = true
            }
        }

        binding.postCommentView.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToCommentsFragment(post!!)
            findNavController().navigate(action)
        }

        binding.postEditView.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToEditPostFragment(post!!)
            findNavController().navigate(action)
        }

        binding.postDeleteView.setOnClickListener {
            postViewModel.deletePost(post!!)
        }
    }
}