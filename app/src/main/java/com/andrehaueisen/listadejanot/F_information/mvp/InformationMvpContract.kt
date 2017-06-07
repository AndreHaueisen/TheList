package com.andrehaueisen.listadejanot.F_information.mvp

import android.view.MenuItem
import com.andrehaueisen.listadejanot.models.PlayerStatus

/**
 * Created by andre on 6/5/2017.
 */
interface InformationMvpContract {

    interface Model{
        fun playOnBoardingAudio()
        fun onDestroy()
    }

    interface Presenter{
        fun playOnBoardingAudio()
        fun listenToPlayerStatus()

    }

    interface View{
        fun setViews()
        fun onOptionsItemSelected(item: MenuItem)
        fun newPlayerEventReported(playerStatus: PlayerStatus)
    }

}