package com.mx.mxvideo_demo.adapts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mx.mxvideo_demo.HomePages
import com.mx.mxvideo_demo.R
import com.mx.recycleview.base.BaseSimpleAdapt
import kotlinx.android.synthetic.main.layout_home.view.*

class HomeAdapt (list: ArrayList<HomePages>) : BaseSimpleAdapt<HomePages>(list) {
    override fun createItem(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(R.layout.layout_home, parent, false)
    }

    override fun bindView(position: Int, itemView: View, record: HomePages) {
        itemView.textTxv.text = record.title
    }
}