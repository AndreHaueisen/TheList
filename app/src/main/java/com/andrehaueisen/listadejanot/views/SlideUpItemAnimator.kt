package com.andrehaueisen.listadejanot.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.andrehaueisen.listadejanot.utilities.DEFAULT_ANIMATIONS_DURATION


/**
 * Created by andre on 10/15/2017.
 */
class SlideUpItemAnimator : SimpleItemAnimator() {
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val anim = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f)
        anim.duration = 500
        holder.itemView.startAnimation(anim)
        return true
    }

    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val anim = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f)
        anim.duration = DEFAULT_ANIMATIONS_DURATION
        holder.itemView.startAnimation(anim)
        return false
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, fromLeft: Int, fromTop: Int, toLeft: Int, toTop: Int): Boolean {
        return false
    }

    override fun runPendingAnimations() {

    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {

    }

    override fun endAnimations() {

    }

    override fun isRunning(): Boolean {
        return false
    }
}