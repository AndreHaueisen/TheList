package com.andrehaueisen.listadejanot.utilities

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.support.v4.content.ContextCompat

fun ObjectAnimator.animateBackgroundToColor(activity: Activity, initialColor: Int, finalColor: Int): ObjectAnimator{

    this.propertyName = "backgroundColor"
    this.setIntValues(ContextCompat.getColor(activity, initialColor), ContextCompat.getColor(activity, finalColor))
    this.setEvaluator(ArgbEvaluator())
    this.duration = DEFAULT_ANIMATIONS_DURATION

    return this
}
