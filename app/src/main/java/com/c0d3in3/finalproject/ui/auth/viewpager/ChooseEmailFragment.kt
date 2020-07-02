package com.c0d3in3.finalproject.ui.auth.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.c0d3in3.finalproject.R
import com.c0d3in3.finalproject.ui.auth.RegisterActivity
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.choose_email_layout.*
import kotlinx.android.synthetic.main.choose_email_layout.view.*
import kotlinx.android.synthetic.main.choose_name_layout.*
import kotlinx.android.synthetic.main.choose_name_layout.btnNext

/**
 * A simple [Fragment] subclass.
 */
class ChooseEmailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment



        val itemView = inflater.inflate(R.layout.choose_email_layout, container, false)
        itemView.btnNext.setOnClickListener {
            (activity as RegisterActivity).registerViewPager.currentItem = (activity as RegisterActivity).registerViewPager.currentItem + 1
        }

        itemView.btnBack.setOnClickListener {
            (activity as RegisterActivity).registerViewPager.currentItem = (activity as RegisterActivity).registerViewPager.currentItem - 1
        }

        return itemView
    }



}
