package com.avon.spott

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

fun RecyclerView.betterSmoothScrollToPosition(targetItem: Int) {
    layoutManager?.apply {
        val maxScroll = 20
        when (this) {
            is GridLayoutManager -> {
                val topItem = findFirstVisibleItemPosition()
                val distance = topItem - targetItem
                val anchorItem = when {
                    distance > maxScroll -> targetItem + maxScroll
                    distance < -maxScroll -> targetItem - maxScroll
                    else -> topItem
                }
                if (anchorItem != topItem) scrollToPosition(anchorItem)
                post {
                    smoothScrollToPosition(targetItem)
                }
            }

            is StaggeredGridLayoutManager -> {
                val firstVisibleItems : IntArray? = null
                val topItem = findFirstVisibleItemPositions(firstVisibleItems)
                val distance = topItem[0] - targetItem
                val anchorItem = when {
                    distance > maxScroll -> targetItem + maxScroll
                    distance < -maxScroll -> targetItem - maxScroll
                    else -> topItem[0]
                }
                if (anchorItem != topItem[0]) scrollToPosition(anchorItem)
                post {
                    smoothScrollToPosition(targetItem)
                }
            }

            else -> smoothScrollToPosition(targetItem)
        }
    }
}