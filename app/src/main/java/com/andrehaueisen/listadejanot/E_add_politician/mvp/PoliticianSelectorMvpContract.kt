package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.view.Menu
import android.view.MenuItem
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

        fun onCreateOptionsMenu(menu: Menu?)
        fun onOptionsItemSelected(item: MenuItem?): Boolean
    }

    interface Presenter{

        fun subscribeToPoliticianSelectorModel()
        fun subscribeToSinglePoliticianModel(politicianName: String)
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