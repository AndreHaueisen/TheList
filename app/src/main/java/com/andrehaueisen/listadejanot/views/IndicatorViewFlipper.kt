package com.andrehaueisen.listadejanot.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ViewFlipper
import com.andrehaueisen.listadejanot.R

/**
 * Created by andre on 10/10/2017.
 */
class IndicatorViewFlipper : ViewFlipper {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    private val paint = Paint()

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val width = width

        val margin = 3F
        val radius = 6F
        var cx: Float = (width / 2F) - ((radius+margin) * 2F * childCount / 2F)
        val cy: Float = height - 20F

        canvas.save()

        for (i in 0 until childCount) {

            if (i == displayedChild) {
                paint.color = ContextCompat.getColor(context, R.color.colorAccentDark)
                canvas.drawCircle(cx, cy, radius, paint)

            } else {
                paint.color = ContextCompat.getColor(context, R.color.colorAccent)
                canvas.drawCircle(cx, cy, radius, paint)
            }
            cx += 2 * (radius + margin)

        }

        canvas.restore()
    }

}