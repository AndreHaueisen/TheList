package com.andrehaueisen.listadejanot.utilities

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.support.v4.content.ContextCompat

fun ObjectAnimator.animatePropertyToColor(activity: Activity, initialColor: Int, finalColor: Int, propertyName: String): ObjectAnimator{

    this.propertyName = propertyName
    this.setIntValues(ContextCompat.getColor(activity, initialColor), ContextCompat.getColor(activity, finalColor))
    this.setEvaluator(ArgbEvaluator())
    this.duration = DEFAULT_ANIMATIONS_DURATION

    return this
}

fun ObjectAnimator.animateTint(activity: Activity, initialColor: Int, finalColor: Int, propertyName: String): ObjectAnimator{

    this.propertyName = propertyName
    this.setIntValues(ContextCompat.getColorStateList(activity, initialColor).defaultColor, ContextCompat.getColorStateList(activity, finalColor).defaultColor)
    this.setEvaluator(ArgbEvaluator())
    this.duration = DEFAULT_ANIMATIONS_DURATION

    return this
}
