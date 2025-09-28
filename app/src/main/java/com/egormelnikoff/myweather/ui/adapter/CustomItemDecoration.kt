package com.egormelnikoff.myweather.ui.adapter

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecoration(
    context: Context,
    private val centerSpaceDp: Int,
    spaceLeftFirstDp: Int,
    spaceRightLastDp: Int
) : RecyclerView.ItemDecoration() {

    private val centerSpace: Int = convertDpToPx(centerSpaceDp, context)
    private val spaceLeftFirst: Int = convertDpToPx(spaceLeftFirstDp, context)
    private val spaceRightLast: Int = convertDpToPx(spaceRightLastDp, context)
    private val bottomSpace: Int = convertDpToPx(4, context)

    private fun convertDpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        if (centerSpaceDp != 0) {
            if (position == 0) outRect.left = spaceLeftFirst
            else outRect.left = 0

            if (position == parent.adapter?.itemCount?.minus(1)) outRect.right = spaceRightLast
            else outRect.right = centerSpace
        } else {
            outRect.bottom = bottomSpace
        }

    }
}