package net.nutrishare.app.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import net.nutrishare.app.R
import net.nutrishare.app.adapters.PostAdapter
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentFeedBinding
import net.nutrishare.app.model.Post
import net.nutrishare.app.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    private lateinit var binding:FragmentFeedBinding
    private val postsList = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference
        val repository = PostRepository(postDao, firestore,storage)

        postViewModel = PostViewModel(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentFeedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpRecyclerView()
        setUpListeners()
    }

    private fun observeViewModel(){

        postViewModel.postsLiveData.observe(viewLifecycleOwner) { posts ->
            binding.progressBar.visibility = View.GONE
            if (posts.isNotEmpty()){
               postsList.clear()
               postsList.addAll(posts)
               adapter.notifyItemChanged(0,postsList.size)
               binding.postsRecyclerview.visibility = View.VISIBLE
               binding.emptyListView.visibility = View.GONE
           }
            else{
                binding.postsRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

       binding.progressBar.visibility = View.VISIBLE
       postViewModel.loadAllPosts()

        binding.postsRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.fabAdd.hide()
                } else if (dy < 0) {
                    binding.fabAdd.show()
                }
            }
        })
    }

    private fun setUpRecyclerView(){
        binding.postsRecyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.postsRecyclerview.hasFixedSize()
        adapter = PostAdapter(postsList)
        binding.postsRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, post: Post) {

            }

        })
    }

    private fun setUpListeners(){

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_feed_fragment_to_createPostFragment)
        }

    }
}