package com.andrehaueisen.listadejanot.utilities

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.TextView

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE){
    val SNACKBAR_HEIGHT_DP = 58
    val displayMetrics = context.resources.displayMetrics
    val SNACKBAR_HEIGHT_PX = Math.round(SNACKBAR_HEIGHT_DP * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    Snackbar.make(this, message, duration)
            .apply {
                view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply { height = SNACKBAR_HEIGHT_PX }
                val textView = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)
            }
            .show()
}

