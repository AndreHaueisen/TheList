package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.andrehaueisen.listadejanot.D_main_list.PoliticianListAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import kotlinx.android.synthetic.main.activity_main_list.*


/**
 * Created by andre on 4/20/2017.
 */
class MainListView(val presenterActivity: MainListPresenterActivity) : MainListMvpContract.View {

    private lateinit var mPoliticiansRecyclerView: RecyclerView
    private lateinit var mBottomNavigationView: BottomNavigationView

    private val mPoliticiansList = ArrayList<Politician>()
    private val mDeputadosList = ArrayList<Politician>()
    private val mSenadorList = ArrayList<Politician>()
    private val mAdapter = PoliticianListAdapter(presenterActivity, mPoliticiansList)
    private var mActiveList = ActiveList.SENADOR_LIST

    private enum class ActiveList{
        DEPUTADO_LIST, SENADOR_LIST
    }

    init {
        presenterActivity.setContentView(R.layout.activity_main_list)
        setViews()
    }

    override fun setViews() {
        setRecyclerView()
        setBottomNavigationView()
    }

    private fun setRecyclerView() {
        mPoliticiansRecyclerView = presenterActivity.politicians_recycler_view
        mPoliticiansRecyclerView.layoutManager = LinearLayoutManager(presenterActivity)
        //mPoliticiansRecyclerView.setHasFixedSize(true)
        mPoliticiansRecyclerView.adapter = mAdapter
    }

    private fun setBottomNavigationView() {
        mBottomNavigationView = presenterActivity.navigation
        mBottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.navigation_senadores -> {
                    mPoliticiansList.clear()
                    mPoliticiansList.addAll(mSenadorList)
                    mActiveList = ActiveList.SENADOR_LIST
                    mAdapter.notifyDataSetChanged()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_deputados -> {
                    mPoliticiansList.clear()
                    mPoliticiansList.addAll(mDeputadosList)
                    mActiveList = ActiveList.DEPUTADO_LIST
                    mAdapter.notifyDataSetChanged()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_notifications -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun notifyDeputadoAddition(deputado: Politician) {
        mDeputadosList.add(deputado)
        if(mActiveList == ActiveList.DEPUTADO_LIST) {
            mPoliticiansList.add(deputado)
            mAdapter.notifyItemInserted(mDeputadosList.size)
        }
    }

    override fun notifySenadorAddition(senador: Politician) {
        mSenadorList.add(senador)
        if(mActiveList == ActiveList.SENADOR_LIST) {
            mPoliticiansList.add(senador)
            mAdapter.notifyItemInserted(mSenadorList.size)
        }
    }
}