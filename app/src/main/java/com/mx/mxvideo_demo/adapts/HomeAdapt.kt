package com.mx.mxvideo_demo.adapts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.mx.adapt.MXBaseSimpleAdapt
import com.mx.mxvideo_demo.HomePages
import com.mx.mxvideo_demo.databinding.AdaptHomeItemBinding

class HomeAdapt(list: ArrayList<HomePages>) : MXBaseSimpleAdapt<HomePages>(list) {
    override fun createItem(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        return AdaptHomeItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(position: Int, binding: ViewBinding, record: HomePages) {
        binding as AdaptHomeItemBinding
        binding.textTxv.text = record.title
    }
}