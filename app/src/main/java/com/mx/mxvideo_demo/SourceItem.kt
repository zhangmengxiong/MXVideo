package com.mx.mxvideo_demo

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper

class SourceItem {
    val type: String = ""
    val name: String = ""
    val url: String = ""
    val img: String = ""

    fun live() = (type == "live")

    companion object {
        private val list = ArrayList<SourceItem>()
        fun init(context: Context) {
            list.clear()
            val mapper = ObjectMapper()
            val jsonNode = mapper.readTree(context.assets.open("source.json"))
            for (node in jsonNode) {
                list.add(mapper.treeToValue(node, SourceItem::class.java))
            }
        }

        fun all() = list.toList()

        fun randomLive(): SourceItem {
            return list.filter { it.type == "live" }.random()
        }

        fun random16x9(): SourceItem {
            return list.filter { it.type == "16:9" }.random()
        }

        fun random9x16(): SourceItem {
            return list.filter { it.type == "9:16" }.random()
        }

        fun random4x3(): SourceItem {
            return list.filter { it.type == "4:3" }.random()
        }
    }
}