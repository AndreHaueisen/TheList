package com.andrehaueisen.listadejanot.utilities

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RatingBar
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.g_user_vote_list.UserVotesAdapter
import com.andrehaueisen.listadejanot.views.IndicatorViewFlipper
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.github.florent37.expectanim.listener.AnimationEndListener

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

fun ExpectAnim.animateAdapterChange(view: View, onStartGravityDislocation: Int, onEndGravityDislocation: Int, adapter: UserVotesAdapter){
    this.expect(view)
            .toBe(Expectations.alpha(0F), Expectations.outOfScreen(onStartGravityDislocation))
            .toAnimation()
            .setDuration(QUICK_ANIMATIONS_DURATION)
            .start()
            .addEndListener{
                ExpectAnim().expect(view)
                        .toBe(Expectations.outOfScreen(onEndGravityDislocation))
                        .toAnimation()
                        .setNow()

                (view as RecyclerView).swapAdapter(adapter, false)

                ExpectAnim().expect(view)
                        .toBe(Expectations.alpha(1F), Expectations.atItsOriginalPosition())
                        .toAnimation()
                        .setDuration(QUICK_ANIMATIONS_DURATION)
                        .start()
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
                    (view as TextView).text = view.resources.getQuantityString(
                            R.plurals.recommendation_votes,
                            newCount,
                            newCount)
                }else{
                    (view as TextView).text = view.resources.getQuantityString(
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

fun ExpectAnim.startInfiniteViewTranslation(view: View,
                                            startDirectionGravity: Int,
                                            endDirectionGravity: Int,
                                            startAnimationDuration: Long = SLOW_ANIMATION_DURATION,
                                            endAnimationDuration: Long = ULTRA_SLOW_ANIMATION_DURATION,
                                            endListener: AnimationEndListener){
    this.expect(view)
            .toBe(Expectations.outOfScreen(startDirectionGravity))
            .toAnimation()
            .setDuration(SLOW_ANIMATION_DURATION)
            .setInterpolator(LinearInterpolator())
            .start()
            .addEndListener{
                this
                        .expect(view)
                        .toBe(Expectations.outOfScreen(endDirectionGravity))
                        .toAnimation()
                        .setNow()

                Log.i("ANIMATION", "ANIMATION IS ACTIVE")

                ExpectAnim()
                        .expect(view)
                        .toBe(Expectations.outOfScreen(endDirectionGravity))
                        .toAnimation()
                        .setDuration(ULTRA_SLOW_ANIMATION_DURATION)
                        .setInterpolator(LinearInterpolator())
                        .start()
                        .addEndListener(endListener)
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