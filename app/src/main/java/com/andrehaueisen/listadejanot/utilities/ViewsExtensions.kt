package com.andrehaueisen.listadejanot.utilities

import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.design.widget.Snackbar
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

fun TextView.setMissingVotesText(resources: Resources, voteNumber: Long){

    val missingVoteNumber = (VOTES_TO_MAIN_LIST_THRESHOLD - voteNumber).toInt()
    if(missingVoteNumber > 0){
        text = resources.getQuantityString(R.plurals.missing_votes_to_threshold, missingVoteNumber, missingVoteNumber)
    }else{
        text = resources.getString(R.string.politician_already_banned)
    }
}

fun View.showIndefiniteSnackbar(message: String){
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE).show()
}
