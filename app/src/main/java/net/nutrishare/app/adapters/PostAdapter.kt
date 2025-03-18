package net.nutrishare.app.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.nutrishare.app.R
import net.nutrishare.app.databinding.PostItemDesignBinding
import net.nutrishare.app.model.Post

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, post: Post)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, mListener!!)
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    class PostViewHolder(
        private val binding: PostItemDesignBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                if (post.userImage.isNotEmpty()){
                    Picasso.get().load(post.userImage)
                        .placeholder(R.drawable.placeholder)
                        .resize(200,200)
                        .centerCrop()
                        .into(postUserImageView)
                }
                postUserNameView.text = post.userName
                if (post.productLink.isNotEmpty()){
                    postProductLinkView.visibility = View.VISIBLE
                }
                else{
                    postProductLinkView.visibility = View.GONE
                }
                if (post.postImage!!.isEmpty()) {
                    postImageView.visibility = View.GONE
                } else {
                    postImageView.visibility = View.VISIBLE
                    Picasso.get().load(post.postImage)
                        .placeholder(R.drawable.placeholder)
                        .resize(600,400)
                        .centerCrop()
                        .into(postImageView)
                }
                postProductLinkView.text = post.productLink
                postDescriptionView.text = post.description

                binding.root.setOnClickListener {
                    mListener.onItemClick(layoutPosition, post)
                }
            }
        }
    }

}
