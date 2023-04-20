package com.mx.mxvideo_demo.adapts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mx.adapt.MXBaseSimpleAdapt
import com.mx.mxvideo_demo.HomePages
import com.mx.mxvideo_demo.R
import kotlinx.android.synthetic.main.adapt_home_item.view.textTxv

class HomeAdapt (list: ArrayList<HomePages>) : MXBaseSimpleAdapt<HomePages>(list) {
    override fun createItem(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(R.layout.adapt_home_item, parent, false)
    }

    override fun bindView(position: Int, itemView: View, record: HomePages) {
        itemView.textTxv.text = record.title
    }
}