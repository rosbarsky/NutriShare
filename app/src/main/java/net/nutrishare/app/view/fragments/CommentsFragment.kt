package net.nutrishare.app.view.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import net.nutrishare.app.adapters.PostCommentsAdapter
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentCommentsBinding
import net.nutrishare.app.model.Comment
import net.nutrishare.app.model.Post
import net.nutrishare.app.utils.SessionManager
import net.nutrishare.app.viewmodel.PostViewModel
import java.util.UUID


class CommentsFragment() : BottomSheetDialogFragment() {

    private lateinit var binding:FragmentCommentsBinding
    private lateinit var postCommentsAdapter :PostCommentsAdapter
    private lateinit var postViewModel: PostViewModel
    private var post: Post? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val commentDao = AppDatabase.getDatabase(requireContext()).commentsDao()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference
        val repository = PostRepository(postDao, firestore,storage,commentDao)

        postViewModel = PostViewModel(repository)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommentsBinding.inflate(inflater, container, false)

        val args: CommentsFragmentArgs by navArgs()
        post = args.post
        postViewModel.fetchCommentsByPostId("${post?.postId}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpRecyclerView()
        setUpListeners()
    }

    private fun observeViewModel(){

        postViewModel.commentsLiveData.observe(viewLifecycleOwner) { comments ->
            postCommentsAdapter.submitList(comments)
        }

        postViewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpRecyclerView(){
        binding.commentsRecyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.commentsRecyclerview.hasFixedSize()
        postCommentsAdapter = PostCommentsAdapter()
        binding.commentsRecyclerview.adapter = postCommentsAdapter
        postCommentsAdapter.setOnItemClickListener(object : PostCommentsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

            }

            override fun onItemDeleteClick(position: Int, comment: Comment) {
                 postViewModel.deleteComment(comment.commentId,"${post?.postId}")
            }

        })
    }

    private fun setUpListeners(){
        binding.sendButtonView.setOnClickListener {
           val message = binding.commentBoxView.text.toString()
           if (message.isNotEmpty()){
               val comment = Comment(
                   commentId = UUID.randomUUID().toString(),
                   postId = "${post?.postId}",
                   message = message,
                   userId = "${SessionManager.getUser()?.userId}",
                   userName = "${SessionManager.getUser()?.userName}",
                   userImage = "${SessionManager.getUser()?.profileImageUrl}",
                   timestamp = System.currentTimeMillis()
               )
               postViewModel.addComment(comment)
           }
            else
           {
               Toast.makeText(requireActivity(),"Comment box is empty!",Toast.LENGTH_SHORT).show()
           }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let { sheet ->

                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.requestLayout()

                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED // Ensure it's expanded
            }
        }

        return dialog
    }
}