package com.andrehaueisen.listadejanot.views

import android.app.Activity
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.ImageView
import com.andrehaueisen.listadejanot.R

/**
 * Created by andre on 11/29/2017.
 */
class DeleteOpinionDialog(activity: Activity): AlertDialog(activity){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_delete_opinion)
        setCancelable(true)

        val idAnimatedVectorDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.anim_ic_shredder)
        val idAnimationView = findViewById<ImageView>(R.id.animation_view)
        idAnimationView?.setImageDrawable(idAnimatedVectorDrawable)
        idAnimatedVectorDrawable?.start()

    }

    fun setPositiveButton() = findViewById<Button>(R.id.positive_button) as Button
    fun setNegativeButton() = findViewById<Button>(R.id.negative_button) as Button

}