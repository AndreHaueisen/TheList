package com.andrehaueisen.listadejanot.f_politician_selector.mvp

import android.os.AsyncTask
import com.andrehaueisen.listadejanot.models.Politician

/**
 * Created by andre on 5/11/2017.
 */
class PoliticianSelectorModel(deputados: ArrayList<Politician>,
                              senadores: ArrayList<Politician>,
                              governadores: ArrayList<Politician>) :
        PoliticianSelectorMvpContract.Model {

    private val LOG_TAG: String = PoliticianSelectorModel::class.java.simpleName
    private var mSearchablePoliticianList = ArrayList<Politician>()

    init {
        MergeListsTask().execute(deputados, senadores, governadores)
    }

    fun getSearchablePoliticiansList() = mSearchablePoliticianList

    fun setSearchablePoliticiansList(originalPoliticiansList: ArrayList<Politician>) {
        mSearchablePoliticianList = originalPoliticiansList
    }

    inner class MergeListsTask : AsyncTask<ArrayList<Politician>, Unit, Unit>(){

        override fun doInBackground(vararg politiciansLists: ArrayList<Politician>) {
            mSearchablePoliticianList.addAll(politiciansLists[0])
            mSearchablePoliticianList.addAll(politiciansLists[1])
            mSearchablePoliticianList.addAll(politiciansLists[2])
        }
    }

}