package com.andrehaueisen.listadejanot.utilities

import android.view.View
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations

fun ExpectAnim.plusOneCondemnAnimation(parentView: View, politician: Politician) {

    val plusOneTextView = parentView.findViewById(R.id.plus_one_text_view)
    val votesNumberTextView = parentView.findViewById(R.id.votes_number_text_view) as TextView
    val missingVotesTextView = parentView.findViewById(R.id.missing_votes_text_view) as TextView?

    this.expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.belowOf(votesNumberTextView))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .setStartDelay(200)
            .start()
            .addEndListener {
                ExpectAnim()
                        .expect(plusOneTextView)
                        .toBe(Expectations.alpha(0.0f),
                                Expectations.atItsOriginalScale(),
                                Expectations.atItsOriginalPosition())
                        .toAnimation()
                        .setDuration(DEFAULT_ANIMATIONS_DURATION)
                        .start()
                        .addEndListener {
                            votesNumberTextView.text = politician.votesNumber.toString()
                            missingVotesTextView?.setMissingVotesText(parentView.context.resources, politician.votesNumber)
                            ExpectAnim().stopRefreshingTitleAnimation(parentView)
                        }
            }
}

fun ExpectAnim.minusOneAbsolveAnimation(parentView: View, politician: Politician) {

    val plusOneTextView = parentView.findViewById(R.id.plus_one_text_view)
    val votesNumberTextView = parentView.findViewById(R.id.votes_number_text_view) as TextView
    val missingVotesTextView = parentView.findViewById(R.id.missing_votes_text_view) as TextView?

    this.addEndListener {
        votesNumberTextView.text = politician.votesNumber.toString()
        missingVotesTextView?.setMissingVotesText(parentView.context.resources, politician.votesNumber)
    }
            .expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.belowOf(votesNumberTextView))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .setStartDelay(500)
            .start()
            .addEndListener {
                ExpectAnim()
                        .expect(plusOneTextView)
                        .toBe(Expectations.alpha(0.0f),
                                Expectations.atItsOriginalScale(),
                                Expectations.atItsOriginalPosition())
                        .toAnimation()
                        .setDuration(DEFAULT_ANIMATIONS_DURATION)
                        .start()
                ExpectAnim().stopRefreshingTitleAnimation(parentView)
            }
}

fun ExpectAnim.startRefreshingTitleAnimation(parentView: View){
    val voteTitleTextView = parentView.findViewById(R.id.vote_title_text_view) as TextView

    this.expect(voteTitleTextView)
            .toBe(Expectations.scale(0F, 0F))
            .toAnimation()
            .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
            .start()
            .addEndListener {
                voteTitleTextView.text = parentView.context.getString(R.string.refreshing_votes_title)
                ExpectAnim()
                        .expect(voteTitleTextView)
                        .toBe(Expectations.atItsOriginalScale())
                        .toAnimation()
                        .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
                        .start()
            }
}

fun ExpectAnim.stopRefreshingTitleAnimation(parentView: View){
    val voteTitleTextView = parentView.findViewById(R.id.vote_title_text_view) as TextView

    this.expect(voteTitleTextView)
            .toBe(Expectations.scale(0F, 0F))
            .toAnimation()
            .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
            .start()
            .addEndListener {
                voteTitleTextView.text = parentView.context.getString(R.string.votes_title)
                ExpectAnim()
                        .expect(voteTitleTextView)
                        .toBe(Expectations.atItsOriginalScale())
                        .toAnimation()
                        .setDuration(VERY_QUICK_ANIMATIONS_DURATION)
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