package com.andrehaueisen.listadejanot.i_main_lists.mvp

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_search_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.i_main_lists.MainListsAdapter
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.SortType
import com.andrehaueisen.listadejanot.utilities.startNewActivity
import kotlinx.android.synthetic.main.i_activity_main_lists.*

/**
 * Created by andre on 10/22/2017.
 */
class MainListsView(private val mPresenterActivity: MainListsPresenterActivity) {

    private val sortedDeputados = ArrayList<Politician>(10)
    private val sortedSenadores = ArrayList<Politician>(10)
    private val sortedGovernadores = ArrayList<Politician>(10)

    fun setViews(bundle: Bundle?) {
        setToolbar()
        setRecyclerViews()
        setButtons()
    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(main_lists_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    private fun setRecyclerViews() {
        with(mPresenterActivity) {
            val senadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val governadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val deputadosLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            senadores_recycler_view.layoutManager = senadoresLayoutManager
            senadores_recycler_view.setHasFixedSize(true)
            senadores_recycler_view.adapter = MainListsAdapter(this, sortedSenadores)

            governadores_recycler_view.layoutManager = governadoresLayoutManager
            governadores_recycler_view.setHasFixedSize(true)
            governadores_recycler_view.adapter = MainListsAdapter(this, sortedGovernadores)

            deputados_recycler_view.layoutManager = deputadosLayoutManager
            deputados_recycler_view.setHasFixedSize(true)
            deputados_recycler_view.adapter = MainListsAdapter(this, sortedDeputados)
        }
    }


    fun notifyNewDeputado(politicians: ArrayList<Politician>) {
        sortedDeputados.addAll(politicians)
        (mPresenterActivity.deputados_recycler_view.adapter as MainListsAdapter).notifyDataSetChanged()
    }

    fun notifyNewSenador(politicians: ArrayList<Politician>) {
        sortedSenadores.addAll(politicians)
        (mPresenterActivity.senadores_recycler_view.adapter as MainListsAdapter).notifyDataSetChanged()
    }

    fun notifyNewGovernador(politicians: ArrayList<Politician>) {
        sortedGovernadores.addAll(politicians)
        (mPresenterActivity.governadores_recycler_view.adapter as MainListsAdapter).notifyDataSetChanged()
    }

    fun onBackPressed(){
        with(mPresenterActivity) {
            group_buttons.visibility = View.VISIBLE
            group_lists.visibility = View.INVISIBLE
        }
    }

    private fun clearData(){

        if(sortedDeputados.isNotEmpty() || sortedGovernadores.isNotEmpty() || sortedSenadores.isNotEmpty()) {
            sortedDeputados.clear()
            sortedGovernadores.clear()
            sortedSenadores.clear()
        }
    }

    private fun setButtons() {
        with(mPresenterActivity) {
            top_in_recommendations_view.setOnClickListener {
                clearData()

                senadores_title_text_view.text = getString(R.string.senadores_sorted_by_vote_intentions)
                governadores_title_text_view.text = getString(R.string.governadores_sorted_by_vote_intentions)
                deputados_title_text_view.text = getString(R.string.deputados_sorted_by_vote_intentions)

                group_buttons.visibility = View.GONE
                group_lists.visibility = View.VISIBLE

                sortPoliticians(SortType.RECOMMENDATIONS_COUNT)
            }

            top_in_condemnations_view.setOnClickListener {
                clearData()

                senadores_title_text_view.text = getString(R.string.senadores_sorted_by_suspicions)
                governadores_title_text_view.text = getString(R.string.governadores_sorted_by_suspicions)
                deputados_title_text_view.text = getString(R.string.deputados_sorted_by_suspicions)

                group_buttons.visibility = View.GONE
                group_lists.visibility = View.VISIBLE

                sortPoliticians(SortType.CONDEMNATIONS_COUNT)
            }

            top_in_overall_grade_view.setOnClickListener {
                clearData()

                senadores_title_text_view.text = getString(R.string.senadores_sorted_by_overall_grade)
                governadores_title_text_view.text = getString(R.string.governadores_sorted_by_overall_grade)
                deputados_title_text_view.text = getString(R.string.deputados_sorted_by_overall_grade)

                group_buttons.visibility = View.GONE
                group_lists.visibility = View.VISIBLE

                sortPoliticians(SortType.OVERALL_GRADE)
            }

            search_view.setOnClickListener { startNewActivity(PoliticianSelectorPresenterActivity::class.java) }
        }
    }
}