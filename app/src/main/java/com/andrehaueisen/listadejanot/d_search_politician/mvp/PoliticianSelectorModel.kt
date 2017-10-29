package com.andrehaueisen.listadejanot.d_search_politician.mvp

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
        mSearchablePoliticianList.addAll(deputados)
        mSearchablePoliticianList.addAll(senadores)
        mSearchablePoliticianList.addAll(governadores)
    }

    fun getSearchablePoliticiansList() = mSearchablePoliticianList

    fun setSearchablePoliticiansList(originalPoliticiansList: ArrayList<Politician>) {
        mSearchablePoliticianList = originalPoliticiansList
    }

}