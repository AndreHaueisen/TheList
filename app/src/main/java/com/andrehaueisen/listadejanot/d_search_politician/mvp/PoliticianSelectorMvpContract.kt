package com.andrehaueisen.listadejanot.d_search_politician.mvp

import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/11/2017.
 */
interface PoliticianSelectorMvpContract {

    interface View{
        fun setViews(isSavedState: Boolean)
        fun notifySearchablePoliticiansNewList()
        fun notifyPoliticianReady()

        fun initiateCondemnAnimations(politician: Politician)
        fun initiateAbsolveAnimations(politician: Politician)
    }

    interface Presenter{
        fun subscribeToPoliticianSelectorModel()

        fun showUserVoteListIfLogged()
    }

    interface Model{
        fun initiateDataLoad()
        fun loadSearchablePoliticiansList(): Observable<ArrayList<Politician>>

        fun onDestroy()
    }

    interface IndividualPoliticianModel{
        fun initiateSinglePoliticianLoad(politicianName: String)
        fun loadSinglePoliticianPublisher(): PublishSubject<Politician>

        fun onDestroy()
    }
}