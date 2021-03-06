package com.c0d3in3.finalproject.ui.dashboard.stories.story_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.c0d3in3.finalproject.MyDiffCallback
import com.c0d3in3.finalproject.R
import com.c0d3in3.finalproject.bean.StoryModel
import com.c0d3in3.finalproject.databinding.StoryImageItemLayoutBinding
import com.c0d3in3.finalproject.network.State
import com.c0d3in3.finalproject.network.UsersRepository
import com.c0d3in3.finalproject.tools.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoryRecyclerViewAdapter(private val mViewType: Int, private val callback: StoryRecyclerViewAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var firstLoad = false
    private var storyList = mutableListOf<StoryModel>()
    private var usersRepository = UsersRepository()

    interface StoryRecyclerViewAdapterCallback{
        fun storyLoaded()
    }

    companion object {
        private const val VIEW_TYPE_STORY = 0
        private const val VIEW_TYPE_PROGRESS_BAR = 1
    }

    fun setList(list: ArrayList<StoryModel>){
        if(list.isNotEmpty()){
            val mutableList = list.toMutableList()
            val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mutableList,storyList))
            storyList = mutableList
            if(mViewType == VIEW_TYPE_PROGRESS_BAR) notifyDataSetChanged()
            else{
                if(storyList[0].storyAuthorModel == null) getUser(storyList[0])
                else notifyDataSetChanged()
            }
        }
        else{
            storyList.clear()
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val binding: StoryImageItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        fun onBind(){
            binding.storyModel = storyList[adapterPosition]
            binding.storyAuthorModel = storyList[0].storyAuthorModel

            if(!firstLoad){
                callback.storyLoaded()
                firstLoad = true
            }
        }
    }

    inner class ProgressBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(){
            val lp = itemView.layoutParams as GridLayoutManager.LayoutParams
            lp.width = Utils.getScreenWidth() / storyList.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_STORY -> ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.story_image_item_layout, parent, false))
            else -> ProgressBarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.story_progressbar_layout, parent, false))
        }
    }

    override fun getItemCount() = storyList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ViewHolder -> holder.onBind()
            is ProgressBarViewHolder -> holder.onBind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (mViewType) {
            0 -> {
                VIEW_TYPE_STORY
            }
            else -> VIEW_TYPE_PROGRESS_BAR
        }
    }

    private fun getUser(
        model: StoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            usersRepository.getUser(model.storyAuthorId).collect { state ->
                when (state) {
                    is State.Success -> {
                        model.storyAuthorModel = state.data!!
                        withContext(Dispatchers.Main) {notifyDataSetChanged()}
                    }
                }
            }
        }
    }
}