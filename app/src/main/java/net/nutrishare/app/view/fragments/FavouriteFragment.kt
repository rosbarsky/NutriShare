package net.nutrishare.app.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import net.nutrishare.app.adapters.PostAdapter
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentFavouriteBinding
import net.nutrishare.app.model.Post
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.utils.WrapContentLinearLayoutManager
import net.nutrishare.app.viewmodel.PostViewModel

class FavouriteFragment : Fragment() {

    private lateinit var binding:FragmentFavouriteBinding
    private val favouritePosts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
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
        binding =  FragmentFavouriteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpRecyclerView()
    }

    private fun observeViewModel(){

        postViewModel.favoritePosts.observe(viewLifecycleOwner) { posts ->
            binding.progressBar.visibility = View.GONE
            if (posts.isNotEmpty()){
                favouritePosts.clear()
                favouritePosts.addAll(posts)
                adapter.notifyItemChanged(0,favouritePosts.size)
                binding.favouritesRecyclerview.visibility = View.VISIBLE
                binding.emptyListView.visibility = View.GONE
            }
            else{
                binding.favouritesRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        postViewModel.fetchFavoritePostsByUser("${SessionManager.getUser()?.userId}")

        postViewModel.operationStatus.observe(viewLifecycleOwner){ result->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }

            if (favouritePosts.isEmpty()){
                binding.favouritesRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

    }

    private fun setUpRecyclerView(){
        binding.favouritesRecyclerview.layoutManager = WrapContentLinearLayoutManager(requireActivity(),
            RecyclerView.VERTICAL,false)
        binding.favouritesRecyclerview.hasFixedSize()
        adapter = PostAdapter(favouritePosts)
        binding.favouritesRecyclerview.adapter = adapter
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
                favouritePosts.removeAt(position)
                adapter.notifyItemChanged(position)
            }

            override fun onItemCommentClick(position: Int, post: Post) {
                val action = FavouriteFragmentDirections.actionFavouriteFragmentToCommentsFragment(post)
                findNavController().navigate(action)
            }

            override fun onItemEditClick(position: Int, post: Post) {
                val action = FavouriteFragmentDirections.actionFavouriteFragmentToEditPostFragment(post)
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
                    favouritePosts.removeAt(position)
                    adapter.notifyItemChanged(position)
                }

                val alert = builder.create()
                alert.show()
            }

        })
    }
}