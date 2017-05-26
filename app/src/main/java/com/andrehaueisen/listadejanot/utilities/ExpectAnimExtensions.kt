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
    val moldView = parentView.findViewById(R.id.mold_view)

    this.expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.centerBetweenViews(addVoteCountToggleButton, votesNumberTextView, true, true))
            .expect(moldView)
            .toBe(Expectations.alpha(0.5f))
            .toAnimation()
            .setDuration(1000)
            .start()
            .setEndListener {
                ExpectAnim()
                        .expect(plusOneTextView)
                        .toBe(Expectations.alpha(0.0f),
                                Expectations.atItsOriginalScale(),
                                Expectations.sameCenterAs(votesNumberTextView, true, true))
                        .toAnimation()
                        .setDuration(500)
                        .start()
                        .setEndListener {
                            votesNumberTextView.text = politician.votesNumber.toString()
                        }
            }
}

fun ExpectAnim.minusOneAbsolveAnimation(parentView : View, politician: Politician) {

    val plusOneTextView = parentView.findViewById(R.id.plus_one_text_view)
    val addVoteCountToggleButton = parentView.findViewById(R.id.add_to_vote_count_toggle_button)
    val votesNumberTextView = parentView.findViewById(R.id.votes_number_text_view) as TextView
    val moldView = parentView.findViewById(R.id.mold_view)

    this.setStartListener {
        votesNumberTextView.text = politician.votesNumber.toString()
    }
            .expect(plusOneTextView)
            .toBe(Expectations.alpha(1.0f),
                    Expectations.scale(1.5f, 1.5f),
                    Expectations.centerBetweenViews(votesNumberTextView, addVoteCountToggleButton, true, true))
            .expect(moldView)
            .toBe(Expectations.alpha(1.0f))
            .toAnimation()
            .setDuration(1000)
            .start()
            .setEndListener {
                ExpectAnim()
                        .expect(plusOneTextView)
                        .toBe(Expectations.alpha(0.0f),
                                Expectations.atItsOriginalScale(),
                                Expectations.atItsOriginalPosition())
                        .toAnimation()
                        .setDuration(500)
                        .start()
            }
}

