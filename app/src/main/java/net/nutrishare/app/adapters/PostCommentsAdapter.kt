package net.nutrishare.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.databinding.CommentItemDesignBinding
import net.nutrishare.app.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostCommentsAdapter : RecyclerView.Adapter<PostCommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemDeleteClick(position: Int, comment: Comment)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = CommentItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(view,mListener!!)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun submitList(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    class CommentViewHolder(private val binding: CommentItemDesignBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            if (comment.userImage.isNotEmpty()){
                Picasso.get().load(comment.userImage)
                    .placeholder(R.drawable.placeholder)
                    .resize(200,200)
                    .centerCrop()
                    .into(binding.postUserImageview)
            }
            binding.commentedUsername.text = comment.userName
            binding.commentMessageView.text = comment.message
            binding.dateTimeView.text =
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                    Date(comment.timestamp)
                )
            binding.deleteButton.setOnClickListener {
                mListener.onItemDeleteClick(layoutPosition,comment)
            }
        }
    }
}