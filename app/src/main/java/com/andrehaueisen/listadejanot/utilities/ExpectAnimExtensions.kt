package com.andrehaueisen.listadejanot.utilities

import android.content.Context
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.views.IndicatorViewFlipper
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations

fun ExpectAnim.scaleRatingBarUpAndDown(ratingBar: RatingBar, viewFlipper: IndicatorViewFlipper, context: Context) {

    this.expect(ratingBar)
            .toBe(Expectations.scale(1.2f, 1.2f))
            .toAnimation()
            .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
            .start()
            .addEndListener { _ ->
                ExpectAnim().expect(ratingBar)
                        .toBe(Expectations.atItsOriginalScale())
                        .toAnimation()
                        .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
                        .start()
                        .addStartListener { _ ->
                            viewFlipper.setInAnimation(context, R.anim.slide_in_right)
                            viewFlipper.setOutAnimation(context, R.anim.slide_out_left)
                            viewFlipper.showNext()
                        }
            }


}

fun ExpectAnim.animateVoteTextChange(view: View, adapterType: Int, newCount: Int){
    this.expect(view)
            .toBe(Expectations.scale(1.4F, 1.4F), Expectations.alpha(0F))
            .toAnimation()
            .setDuration(QUICK_ANIMATIONS_DURATION)
            .start()
            .addEndListener{
                if(adapterType == WILL_VOTE_POLITICIANS_ADAPTER_TYPE){
                    (view as TextView).text = if (newCount == 0)
                        view.resources.getString(R.string.recommendation_votes_zero)
                    else
                        view.resources.getQuantityString(
                                R.plurals.recommendation_votes,
                                newCount,
                                newCount)

                }else{
                    (view as TextView).text = if (newCount == 0)
                        view.resources.getString(R.string.condemnation_votes_zero)
                    else
                        view.resources.getQuantityString(
                                R.plurals.condemnation_votes,
                                newCount,
                                newCount)
                }

                ExpectAnim().expect(view)
                        .toBe(Expectations.atItsOriginalScale(), Expectations.alpha(1F))
                        .toAnimation()
                        .setDuration(QUICK_ANIMATIONS_DURATION)
                        .start()
            }
}

fun ExpectAnim.fadeOutSingleView(view: View) {
    this.expect(view)
            .toBe(Expectations.alpha(0.0f))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .start()
}

fun ExpectAnim.fadeInSingleView(view: View) {
    this.expect(view)
            .toBe(Expectations.alpha(1.0f))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .start()
}