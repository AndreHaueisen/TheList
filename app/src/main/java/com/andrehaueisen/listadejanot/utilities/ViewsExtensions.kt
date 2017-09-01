package com.andrehaueisen.listadejanot.utilities

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.listadejanot.R



fun ImageView.animateVectorDrawable(initialAnimation: AnimatedVectorDrawable,
                                    finalAnimation: AnimatedVectorDrawable,
                                    useInitialToFinalFlow: Boolean){

    if(drawable == initialAnimation && useInitialToFinalFlow){
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == initialAnimation && !useInitialToFinalFlow){
        setImageDrawable(finalAnimation)
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == finalAnimation && useInitialToFinalFlow){
        setImageDrawable(initialAnimation)
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == finalAnimation && !useInitialToFinalFlow){
        (drawable as AnimatedVectorDrawable).start()

    }
}

fun TextView.setMissingVotesText(context: Context, voteNumber: Long){

    val resources = context.resources
    val minimumVotesToMainList = context.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
    val missingVoteNumber = (minimumVotesToMainList - voteNumber).toInt()
    text = if(missingVoteNumber > 0){
        resources.getQuantityString(R.plurals.missing_votes_to_threshold, missingVoteNumber, missingVoteNumber)
    }else{
        resources.getString(R.string.politician_already_banned)
    }
}

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

