package com.c0d3in3.finalproject.ui.dashboard.stories.story_view

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.c0d3in3.finalproject.CustomProgressBar
import com.c0d3in3.finalproject.R
import com.c0d3in3.finalproject.base.BaseFragment
import com.c0d3in3.finalproject.bean.StoryModel
import kotlinx.android.synthetic.main.story_base_layout.view.*
import kotlinx.android.synthetic.main.story_progressbar_layout.view.*

class StoryViewFragment(private val storyList: ArrayList<StoryModel>, private val isFirst : Boolean) : BaseFragment(), StoryRecyclerViewAdapter.StoryRecyclerViewAdapterCallback {

    private lateinit var adapter : StoryRecyclerViewAdapter
    private lateinit var progressBarAdapter : StoryRecyclerViewAdapter

    private val gestureDetector = GestureDetector(activity,SingleTapConfirm())

    override fun init() {

    }

    override fun setUpFragment() {
        val recyclerView = rootView!!.storyListRecyclerView
        val progressBarRecyclerView =  rootView!!.progressBarRecyclerView

        adapter =  StoryRecyclerViewAdapter(0,this)

        progressBarRecyclerView.layoutManager = GridLayoutManager(activity, storyList.size)

        progressBarAdapter = StoryRecyclerViewAdapter(1, this)
        progressBarAdapter.setList(storyList)
        progressBarRecyclerView.adapter = progressBarAdapter

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter.setList(storyList)
        recyclerView.adapter = adapter

        recyclerView.setOnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) onSingleTapUp(
                v as RecyclerView, event
            )
            true
        }
    }


    private fun loadNextImage(){
        val recyclerView = rootView!!.storyListRecyclerView
        val progressBarRecyclerView =  rootView!!.progressBarRecyclerView
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (recyclerView.adapter!!.itemCount - 1 > visibleItem) {
            progressBarRecyclerView[visibleItem].storyTimeProgressBar.cancelProgress()
            progressBarRecyclerView[visibleItem].storyTimeProgressBar.progress = 9999
            setProgressBar(
                progressBarRecyclerView[visibleItem + 1].storyTimeProgressBar
            )
            recyclerView.scrollToPosition(visibleItem + 1)
        } else {
            progressBarRecyclerView[visibleItem].storyTimeProgressBar.cancelProgress()
            if(activity != null) (activity as StoryViewActivity).nextViewPagerItem()
        }
    }


    private fun previousViewPagerItem() {
        val recyclerView = rootView!!.storyListRecyclerView
        val progressBarRecyclerView =  rootView!!.progressBarRecyclerView
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (0 < visibleItem) {
            progressBarRecyclerView[visibleItem].storyTimeProgressBar.cancelProgress()
            setProgressBar(
                progressBarRecyclerView[visibleItem - 1].storyTimeProgressBar
            )
            recyclerView.scrollToPosition(visibleItem - 1)
        } else {
            progressBarRecyclerView[visibleItem].storyTimeProgressBar.cancelProgress()

            if(activity != null) (activity as StoryViewActivity).previousViewPagerItem()
        }
    }

    override fun getLayout() = R.layout.story_base_layout

    open class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }

    private fun onSingleTapUp(
        view: RecyclerView,
        e: MotionEvent
    ): Boolean {
        val viewWidth: Int = view.width
        // RIGHT SIDE SCREEN
        if (e.x > viewWidth * 0.7) {
            loadNextImage()
        }
        // LEFT SIDE SCREEN
        if (e.x < viewWidth * 0.3) {
            previousViewPagerItem()
        }
        return true
    }

    private fun setProgressBar(pb: CustomProgressBar) {
        pb.startProgress()

        pb.setOnProgressBarChangeListener(object : CustomProgressBar.OnMyProgressBarChangeListener {
            override fun onProgressChanged(myProgressBar: CustomProgressBar?) {
                if (myProgressBar != null) {
                    if (myProgressBar.progress == 10000) {
                        myProgressBar.cancelProgress()
                        loadNextImage()
                    }
                }
            }

        })
    }

    fun startTimer(){
        val recyclerView = rootView!!.storyListRecyclerView
        val progressBarRecyclerView =  rootView!!.progressBarRecyclerView
        if(progressBarRecyclerView.childCount > 0){
            for(idx in 0 until progressBarRecyclerView.childCount){
                progressBarRecyclerView[idx].storyTimeProgressBar.cancelProgress()
            }
        }
        setProgressBar(progressBarRecyclerView[0].storyTimeProgressBar)
        recyclerView.scrollToPosition(0)
    }

    override fun storyLoaded() {
        if(isFirst) {
            val progressBarRecyclerView =  rootView!!.progressBarRecyclerView
            setProgressBar(progressBarRecyclerView[0].storyTimeProgressBar)
        }

    }
}