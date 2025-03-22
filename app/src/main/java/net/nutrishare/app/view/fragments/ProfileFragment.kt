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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.adapters.PostAdapter
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentProfileBinding
import net.nutrishare.app.factory.AuthViewModelFactory
import net.nutrishare.app.model.Post
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.utils.WrapContentLinearLayoutManager
import net.nutrishare.app.view.activities.AuthActivity
import net.nutrishare.app.viewmodel.AuthViewModel
import net.nutrishare.app.viewmodel.PostViewModel


class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private var user:User?=null
    private lateinit var authViewModel: AuthViewModel
    private val myPosts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository()
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this,factory)[AuthViewModel::class.java]

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
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
        setUpRecyclerView()
    }

    private fun observeViewModel(){

        postViewModel.myPostsLiveData.observe(viewLifecycleOwner) { posts ->
            binding.progressBar.visibility = View.GONE
            if (posts.isNotEmpty()){
                myPosts.clear()
                myPosts.addAll(posts)
                adapter.notifyItemChanged(0,myPosts.size)
                binding.myPostsRecyclerview.visibility = View.VISIBLE
                binding.emptyListView.visibility = View.GONE
            }
            else{
                binding.myPostsRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        postViewModel.loadAllPostsByUserId("${SessionManager.getUser()?.userId}")

        postViewModel.operationStatus.observe(viewLifecycleOwner){ result->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }

            if (myPosts.isEmpty()){
                binding.myPostsRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

    }

    private fun setUpRecyclerView(){
        binding.myPostsRecyclerview.layoutManager = WrapContentLinearLayoutManager(requireActivity(),
            RecyclerView.VERTICAL,false)
        binding.myPostsRecyclerview.hasFixedSize()
        adapter = PostAdapter(myPosts)
        binding.myPostsRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, post: Post) {

            }

            override fun onItemFavouriteClick(position: Int, post: Post) {
                val userId = "${SessionManager.getUser()?.userId}" // Replace with the current user's ID
                if (post.isFavorite){
                    postViewModel.unfavoritePost(userId, post)
                }
                else{
                    postViewModel.favoritePost(userId, post)
                }
                post.isFavorite = !post.isFavorite
                myPosts.removeAt(position)
                adapter.notifyItemChanged(position)
            }

            override fun onItemCommentClick(position: Int, post: Post) {
                val action = ProfileFragmentDirections.actionProfileFragmentToCommentsFragment(post)
                findNavController().navigate(action)
            }

            override fun onItemEditClick(position: Int, post: Post) {
                val action = ProfileFragmentDirections.actionProfileFragmentToEditPostFragment(post)
                findNavController().navigate(action)
            }

            override fun onItemDeleteClick(position: Int, post: Post) {
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setTitle("Delete")
                builder.setMessage("Are you sure you want to delete?")
                builder.setNegativeButton("No"){dialog,which->
                    dialog.dismiss()
                }
                builder.setPositiveButton("Yes"){dialog,which->
                    dialog.dismiss()
                    postViewModel.deletePost(post)
                    myPosts.removeAt(position)
                    adapter.notifyItemChanged(position)
                }

                val alert = builder.create()
                alert.show()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        displayUserInfo()
    }

    private fun displayUserInfo(){
        user = SessionManager.getUser()

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

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_editUserDetailFragment)
        }

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