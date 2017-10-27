package com.andrehaueisen.listadejanot.utilities

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.andrehaueisen.listadejanot.R

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {
    //val SNACKBAR_HEIGHT_DP = 58
    //val displayMetrics = context.resources.displayMetrics
    //val SNACKBAR_HEIGHT_PX = Math.round(SNACKBAR_HEIGHT_DP * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    Snackbar.make(this, message, duration)
            .apply {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                //view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply { height = SNACKBAR_HEIGHT_PX }
                val textView = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)
            }
            .show()

}

fun View.setPoliticianGradeText(grade: Float) {
    if (grade != -1F) {
        (this as TextView).text = String.format("%.1f", grade)
    } else {
        (this as TextView).text = this.resources.getString(R.string.no_grade)
    }
}


