package com.mata.weather.live.myapplication

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.WindowManager


/**
 *
 * @var viewGroupWidth
 * We will arrange the views horizontally of the parent view
 *
 * Need to calculate the length of sortable views on a row.
 * We will add the views row by row from left to right
 * If the newly added view makes the length of the views in the same row
 * greater than the length of the parent view, we need to enter a new line and start at the beginning.
 *
 */
class CustomTagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    var viewGroupWidth = 0

    init {
        init(context)
    }

    private fun init(context: Context) {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val deviceDisplay = Point()
        display.getSize(deviceDisplay)

        // Default value is width size of screen
        viewGroupWidth = deviceDisplay.x
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        var curWidth: Int
        var curHeight: Int
        var curLeft: Int
        var curTop: Int
        var maxHeight: Int

        // Set new size of group after mearsure
        viewGroupWidth = measuredWidth

        // Get the available size of child view
        val childLeft = this.paddingLeft
        val childTop = this.paddingTop
        val childRight = this.measuredWidth - this.paddingRight
        val childBottom = this.measuredHeight - this.paddingBottom
        val childWidth = childRight - childLeft
        val childHeight = childBottom - childTop
        maxHeight = 0
        curLeft = childLeft
        curTop = childTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) return

            // Get the maximum size of the child
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST)
            )
            curWidth = child.measuredWidth
            curHeight = child.measuredHeight

            // Wrap is reach to the end
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft
                curTop += maxHeight
                maxHeight = 0
            }

            // Do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight)

            // Store the max height
            if (maxHeight < curHeight) {
                maxHeight = curHeight
            }
            curLeft += curWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        // Measurement will ultimately be computing these values.
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0
        var mLeftWidth = 0

        // Iterate through all children, measuring them and computing our dimensions from their size.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxWidth += Math.max(maxWidth, child.measuredWidth)
            mLeftWidth += child.measuredWidth

            // Some special case we have gap in the end of life so we need to recalculate size of list tag
            if (i < count - 1) {
                if (mLeftWidth + getChildAt(i + 1).measuredWidth > viewGroupWidth) {
                    maxHeight += child.measuredHeight
                    mLeftWidth = 0
                } else {
                    maxHeight = Math.max(maxHeight, child.measuredHeight)
                }
            } else {
                maxHeight = Math.max(maxHeight, child.measuredHeight)
            }
            childState = combineMeasuredStates(childState, child.measuredState)
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

        // Report our final dimensions.
        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                maxHeight,
                heightMeasureSpec,
                childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }
}
