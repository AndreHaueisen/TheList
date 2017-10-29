package com.andrehaueisen.listadejanot.d_search_politician.mvp

import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/11/2017.
 */
interface PoliticianSelectorMvpContract {

    interface View{
        fun setViews(isSavedState: Boolean)
        fun requestSearchableListUpdate()
        fun notifyPoliticianReady()

    }

    interface Presenter{
        fun showUserVoteListIfLogged()
    }

    interface Model

    interface IndividualPoliticianModel{
        fun initiateSinglePoliticianLoad(politicianName: String)
        fun loadSinglePoliticianPublisher(): PublishSubject<Politician>

        fun onDestroy()
    }
}