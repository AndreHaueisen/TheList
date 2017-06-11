package com.andrehaueisen.listadejanot.F_information.mvp

import android.graphics.drawable.AnimatedVectorDrawable
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.PlayerStatus
import com.andrehaueisen.listadejanot.utilities.animateVectorDrawable
import kotlinx.android.synthetic.main.activity_information_presenter.*


/**
 * Created by andre on 6/5/2017.
 */
class InformationView(val mPresenterActivity: InformationPresenterActivity) : InformationMvpContract.View {

    private val mPlayPauseAnimation = mPresenterActivity.getDrawable(R.drawable.play_pause_animated_vector) as AnimatedVectorDrawable
    private val mPausePlayAnimation = mPresenterActivity.getDrawable(R.drawable.pause_play_animated_vector) as AnimatedVectorDrawable
    private val mPlayAudioFab : FloatingActionButton

    init {
        mPresenterActivity.setContentView(R.layout.activity_information_presenter)
        mPlayAudioFab = mPresenterActivity.play_on_boarding_audio_fab
    }

    override fun setViews() {

        with(mPresenterActivity) {
            fun setToolbar() {
                val actionBar = information_toolbar
                setSupportActionBar(actionBar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
            }

            fun setOnBoardingAudioButton() {
                mPlayAudioFab.setImageDrawable(mPlayPauseAnimation)
                mPlayAudioFab.setOnClickListener { playOnBoardingAudio() }
            }

            setToolbar()
            setOnBoardingAudioButton()
        }

    }

    override fun newPlayerEventReported(playerStatus: PlayerStatus) {

        with(mPlayAudioFab) {

            when (playerStatus) {
                PlayerStatus.PLAYING -> {
                    animateVectorDrawable(
                            mPlayPauseAnimation,
                            mPausePlayAnimation,
                            useInitialToFinalFlow = true)
                }

                PlayerStatus.PAUSED, PlayerStatus.COMPLETE -> {
                    animateVectorDrawable(
                            mPlayPauseAnimation,
                            mPausePlayAnimation,
                            useInitialToFinalFlow = false)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) {

        when (item.itemId) {
            android.R.id.home -> mPresenterActivity.finish()
        }
    }
}