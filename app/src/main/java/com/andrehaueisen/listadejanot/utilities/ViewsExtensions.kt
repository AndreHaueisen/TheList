package com.andrehaueisen.listadejanot.utilities

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.andrehaueisen.listadejanot.R

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {

    Snackbar.make(this, message, duration)
            .apply {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                val textView = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)
            }
            .show()

}

fun RecyclerView.setLayoutAnimation(animation: Int = R.anim.layout_animation_grow_from_center) {
    val controller = AnimationUtils.loadLayoutAnimation(this.context, animation)

    this.layoutAnimation = controller
    this.scheduleLayoutAnimation()
}

fun View.setPoliticianGradeText(grade: Float, stringId: Int) {
    if (grade != -1F) {
        (this as TextView).text = this.resources.getString(stringId, String.format("%.1f", grade))
    } else {
        (this as TextView).text = this.resources.getString(stringId, resources.getString(R.string.no_grade))
    }
}


