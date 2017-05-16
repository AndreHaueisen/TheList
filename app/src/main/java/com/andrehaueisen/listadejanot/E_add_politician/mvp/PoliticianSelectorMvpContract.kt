package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable
import io.reactivex.internal.observers.FutureSingleObserver

/**
 * Created by andre on 5/11/2017.
 */
interface PoliticianSelectorMvpContract {

    interface View{
        fun setViews(isSavedState: Boolean)
        fun notifySearchablePoliticiansNewList(politicians: ArrayList<Politician>)
        fun notifyPoliticianReady(politician: Politician)

        fun onCreateOptionsMenu(menu: Menu?)
        fun onOptionsItemSelected(item: MenuItem?): Boolean
        fun onSaveInstanceState(): Bundle
    }

    interface Presenter{

        fun subscribeToModel()
        fun subscribeToIndividualModel(politicianName: String)
    }

    interface Model{
        fun initiateDataLoad()
        fun loadSearchablePoliticiansList(): Observable<ArrayList<Politician>>

        fun onDestroy()
    }

    interface IndividualPoliticianModel{
        fun initiateSinglePoliticianLoad(politicianName: String)
        fun loadSinglePoliticianObservable(): FutureSingleObserver<Politician>

        fun onDestroy()
    }
}