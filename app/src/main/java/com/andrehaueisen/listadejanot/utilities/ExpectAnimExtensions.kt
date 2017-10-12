package com.andrehaueisen.listadejanot.utilities

import android.content.Context
import android.view.View
import android.widget.RatingBar
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