package com.c0d3in3.finalproject.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.c0d3in3.finalproject.App
import com.c0d3in3.finalproject.Constants.POST_TYPE_IMAGE
import com.c0d3in3.finalproject.Constants.POST_TYPE_TEXT
import com.c0d3in3.finalproject.MyDiffCallback
import com.c0d3in3.finalproject.R
import com.c0d3in3.finalproject.databinding.PostImageItemLayoutBinding
import com.c0d3in3.finalproject.network.State
import com.c0d3in3.finalproject.network.UsersRepository
import com.c0d3in3.finalproject.bean.PostModel
import com.c0d3in3.finalproject.databinding.PostTextItemLayoutBinding
import kotlinx.android.synthetic.main.post_image_item_layout.view.*
import kotlinx.android.synthetic.main.post_image_item_layout.view.commentButton
import kotlinx.android.synthetic.main.post_image_item_layout.view.fullNameTextView
import kotlinx.android.synthetic.main.post_image_item_layout.view.likeButton
import kotlinx.android.synthetic.main.post_image_item_layout.view.postHeaderLayout
import kotlinx.android.synthetic.main.post_image_item_layout.view.profileImageView
import kotlinx.android.synthetic.main.post_image_item_layout.view.spinnerButton
import kotlinx.android.synthetic.main.post_text_item_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsAdapter(private val callback: CustomPostCallback) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var posts = mutableListOf<PostModel>()
    private val usersRepository = UsersRepository()

    interface CustomPostCallback {
        fun onLikeButtonClick(position: Int)
        fun onCommentButtonClick(position: Int)
        fun openDetailedPost(position: Int)
        fun openProfile(position: Int)
        fun openOptionsDialog(position: Int)
    }

    fun updateSingleItem(model: PostModel,position: Int){
        posts[position] = model
        notifyItemChanged(position)
    }


    fun setPostsList(list: List<PostModel>) {

        if (list.isNotEmpty()) {
            val mutableList = list.toMutableList()
            val diffResult = DiffUtil.calculateDiff(
                MyDiffCallback(mutableList, posts)
            )
            var found = false
            posts = mutableList
            for (idx in 0 until posts.size) {
                if (posts[idx].postAuthorModel != null) continue
                else found = true
                if (idx == posts.size - 1) getUser(posts[idx], true, diffResult)
                else getUser(posts[idx], false, null)
            }
            if(!found) diffResult.dispatchUpdatesTo(this)
        }
        else{
            posts.clear()
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            POST_TYPE_TEXT ->{
                TextPostViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.post_text_item_layout,
                        parent,
                        false
                    )
                )
            }
            else ->{
                ViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.post_image_item_layout,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.onBind()
            is TextPostViewHolder -> holder.onBind()
        }
    }

    inner class ViewHolder(private val binding: PostImageItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        lateinit var model: PostModel

        fun onBind() {
            model = posts[adapterPosition]

            binding.postModel = model

            if(model.postAuthor == App.getCurrentUser().userId)
                itemView.spinnerButton.visibility= View.VISIBLE

            else itemView.spinnerButton.visibility= View.GONE

            itemView.postHeaderLayout.setOnClickListener(this)
            itemView.postImageView.setOnClickListener(this)
            itemView.commentButton.setOnClickListener(this)
            itemView.likeButton.setOnClickListener(this)
            itemView.fullNameTextView.setOnClickListener(this)
            itemView.profileImageView.setOnClickListener(this)
            itemView.spinnerButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                itemView.postImageView.id, itemView.postHeaderLayout.id -> callback.openDetailedPost(
                    adapterPosition
                )
                itemView.commentButton.id -> callback.onCommentButtonClick(adapterPosition)
                itemView.likeButton.id -> callback.onLikeButtonClick(adapterPosition)
                itemView.profileImageView.id, itemView.fullNameTextView.id -> callback.openProfile(adapterPosition)
                itemView.spinnerButton.id -> callback.openOptionsDialog(adapterPosition)
            }
        }
    }

    inner class TextPostViewHolder(private val binding: PostTextItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        lateinit var model: PostModel

        fun onBind() {
            model = posts[adapterPosition]

            binding.postModel = model

            if(model.postAuthor == App.getCurrentUser().userId)
                itemView.spinnerButton.visibility= View.VISIBLE

            else itemView.spinnerButton.visibility= View.GONE

            itemView.postHeaderLayout.setOnClickListener(this)
            itemView.commentButton.setOnClickListener(this)
            itemView.likeButton.setOnClickListener(this)
            itemView.fullNameTextView.setOnClickListener(this)
            itemView.profileImageView.setOnClickListener(this)
            itemView.spinnerButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                itemView.postHeaderLayout.id -> callback.openDetailedPost(
                    adapterPosition
                )
                itemView.commentButton.id -> callback.onCommentButtonClick(adapterPosition)
                itemView.likeButton.id -> callback.onLikeButtonClick(adapterPosition)
                itemView.profileImageView.id, itemView.fullNameTextView.id -> callback.openProfile(adapterPosition)
                itemView.spinnerButton.id -> callback.openOptionsDialog(adapterPosition)
            }
        }
    }

    private fun getUser(
        model: PostModel,
        isLast: Boolean,
        diffResult: DiffUtil.DiffResult?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            usersRepository.getUser(model.postAuthor!!).collect { state ->
                when (state) {
                    is State.Success -> {
                        model.postAuthorModel = state.data!!
                        if (isLast) withContext(Dispatchers.Main) {
                            diffResult?.dispatchUpdatesTo(
                                this@PostsAdapter
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(posts[position].postType.toInt()){
            POST_TYPE_TEXT ->{
                POST_TYPE_TEXT
            }
            else -> POST_TYPE_IMAGE
        }
    }

}