package com.andrehaueisen.listadejanot.f_information.mvp

import android.content.Context
import android.media.MediaPlayer
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.PlayerStatus
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 6/5/2017.
 */
class InformationModel(val context: Context) : InformationMvpContract.Model {

    private val mMediaPlayer : MediaPlayer = MediaPlayer.create(context, R.raw.on_boarding_audio)
    private val mPlayerStatusPublisher: PublishSubject<PlayerStatus> = PublishSubject.create()

    init {
        mMediaPlayer.setOnCompletionListener {
            mPlayerStatusPublisher.onNext(PlayerStatus.COMPLETE)
        }
    }

    override fun playOnBoardingAudio() {

        with(mMediaPlayer) {

            if(isPlaying) {
                pause()
                mPlayerStatusPublisher.onNext(PlayerStatus.PAUSED)
            } else {
                start()
                mPlayerStatusPublisher.onNext(PlayerStatus.PLAYING)
            }
        }
    }

    fun getPlayerStatusPublisher() = mPlayerStatusPublisher

    override fun onDestroy(){
        mMediaPlayer.release()
    }
}