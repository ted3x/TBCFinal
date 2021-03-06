package com.c0d3in3.finalproject.ui.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.c0d3in3.finalproject.network.PostsRepository
import com.c0d3in3.finalproject.network.StoriesRepository

class HomeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(PostsRepository::class.java, StoriesRepository::class.java)
            .newInstance(PostsRepository(), StoriesRepository())
    }
}