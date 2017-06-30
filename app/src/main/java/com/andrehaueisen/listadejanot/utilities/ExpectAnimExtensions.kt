package com.andrehaueisen.listadejanot.utilities

import android.view.View
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations

fun ExpectAnim.plusOneCondemnAnimation(parentView : View, politician: Politician) {

    val plusOneTextView = parentView.findViewById(R.id.plus_one_text_view)
    val addVoteCountToggleButton = parentView.findViewById(R.id.add_to_vote_count_toggle_button)
    val votesNumberTextView = parentView.findViewById(R.id.votes_number_text_view) as TextView
    val missingVotesTextView = parentView.findViewById(R.id.missing_votes_text_view) as TextView?

    this.expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.centerBetweenViews(addVoteCountToggleButton, votesNumberTextView, true, true))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .start()
            .addEndListener {
                ExpectAnim()
                        .expect(plusOneTextView)
                        .toBe(Expectations.alpha(0.0f),
                                Expectations.atItsOriginalScale(),
                                Expectations.sameCenterAs(votesNumberTextView, true, true))
                        .toAnimation()
                        .setDuration(DEFAULT_ANIMATIONS_DURATION)
                        .start()
                        .addEndListener {
                            votesNumberTextView.text = politician.votesNumber.toString()
                            missingVotesTextView?.setMissingVotesText(parentView.context.resources, politician.votesNumber)
                        }
            }
}

fun ExpectAnim.minusOneAbsolveAnimation(parentView : View, politician: Politician) {

    val plusOneTextView = parentView.findViewById(R.id.plus_one_text_view)
    val addVoteCountToggleButton = parentView.findViewById(R.id.add_to_vote_count_toggle_button)
    val votesNumberTextView = parentView.findViewById(R.id.votes_number_text_view) as TextView
    val missingVotesTextView = parentView.findViewById(R.id.missing_votes_text_view) as TextView?

    this.addEndListener {
        votesNumberTextView.text = politician.votesNumber.toString()
        missingVotesTextView?.setMissingVotesText(parentView.context.resources, politician.votesNumber)
    }
            .expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.centerBetweenViews(votesNumberTextView, addVoteCountToggleButton, true, true))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
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
            }
}

fun ExpectAnim.fadeOutSingleView(view: View){
    this.expect(view)
            .toBe(Expectations.alpha(0.0f))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .start()
}

fun ExpectAnim.fadeInSingleView(view: View){
    this.expect(view)
            .toBe(Expectations.alpha(1.0f))
            .toAnimation()
            .setDuration(DEFAULT_ANIMATIONS_DURATION)
            .start()
}