package com.andrehaueisen.listadejanot.e_main_lists

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.i_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.j_login.LoginActivity
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.FabMenu
import kotlinx.android.synthetic.main.e_activity_main_lists.*


/**
 * Created by andre on 11/1/2017.
 */
class MainListsView(private val mPresenterActivity: MainListsPresenterActivity) {

    fun setViews(bundle: Bundle?) {
        setRecyclerViews(bundle)
        setToolbar()
        setFabMenu()
        loadData()
    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(main_lists_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)

            when (getSortType()) {
                SortType.RECOMMENDATIONS_COUNT -> supportActionBar?.title = getString(R.string.top_recommendations)
                SortType.CONDEMNATIONS_COUNT -> supportActionBar?.title = getString(R.string.top_suspicions)
                SortType.TOP_OVERALL_GRADE -> supportActionBar?.title = getString(R.string.top_overall_grade)
                SortType.WORST_OVERALL_GRADE -> supportActionBar?.title = getString(R.string.worst_overall_grade)
                SortType.MEDIA_HIGHLIGHT -> supportActionBar?.title = getString(R.string.media_highlight)
            }
        }
    }

    private fun setRecyclerViews(bundle: Bundle?) {
        with(mPresenterActivity) {
            val presidentesLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val senadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val governadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val deputadosLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            if (bundle != null) {
                presidentesLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_PRESIDENTES_LAYOUT_MANAGER))
                senadoresLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_SENADORES_LAYOUT_MANAGER))
                governadoresLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_GOVERNADORES_LAYOUT_MANAGER))
                deputadosLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_DEPUTADOS_LAYOUT_MANAGER))
                getSortedPresidentes().addAll(bundle.getParcelableArrayList(BUNDLE_PRESIDENTES_LIST))
                getSortedSenadores().addAll(bundle.getParcelableArrayList(BUNDLE_SENADORES_LIST))
                getSortedGovernadores().addAll(bundle.getParcelableArrayList(BUNDLE_GOVERNADORES_LIST))
                getSortedDeputados().addAll(bundle.getParcelableArrayList(BUNDLE_DEPUTADOS_LIST))
            }

            presidentes_recycler_view.layoutManager = presidentesLayoutManager
            presidentes_recycler_view.setHasFixedSize(true)

            senadores_recycler_view.layoutManager = senadoresLayoutManager
            senadores_recycler_view.setHasFixedSize(true)

            governadores_recycler_view.layoutManager = governadoresLayoutManager
            governadores_recycler_view.setHasFixedSize(true)

            deputados_recycler_view.layoutManager = deputadosLayoutManager
            deputados_recycler_view.setHasFixedSize(true)

        }
    }

    private fun setFabMenu() {
        with(mPresenterActivity) {

            menu_fab.setOptionsClick(object : FabMenu.OptionsClick {
                override fun onOptionClick(optionId: Int?) {
                    when (optionId) {
                        R.id.action_user_lists -> showUserVoteListIfLogged()
                        R.id.action_app_info -> startNewActivity(InformationPresenterActivity::class.java)
                        R.id.action_logout -> {
                            mFirebaseAuthenticator.logout()
                            startNewActivity(LoginActivity::class.java)
                        }
                    }
                }
            })
        }
    }

    private fun loadData() {
        with(mPresenterActivity) {

            if (getSortedPresidentes().isNotEmpty()) {
                presidentes_recycler_view.adapter = MainListsAdapter(this, getSortedPresidentes(), getSortType()!!)
            } else {
                presidentes_recycler_view.visibility = View.GONE
                presidentes_title_text_view.visibility = View.GONE
            }

            if (getSortedSenadores().isNotEmpty()) {
                senadores_recycler_view.adapter = MainListsAdapter(this, getSortedSenadores(), getSortType()!!)
            } else {
                senadores_recycler_view.visibility = View.GONE
                senadores_title_text_view.visibility = View.GONE
            }

            if (getSortedGovernadores().isNotEmpty()) {
                governadores_recycler_view.adapter = MainListsAdapter(this, getSortedGovernadores(), getSortType()!!)
            } else {
                governadores_recycler_view.visibility = View.GONE
                governadores_title_text_view.visibility = View.GONE
            }

            if (getSortedDeputados().isNotEmpty()) {
                deputados_recycler_view.adapter = MainListsAdapter(this, getSortedDeputados(), getSortType()!!)
            } else {
                deputados_recycler_view.visibility = View.GONE
                deputados_title_text_view.visibility = View.GONE
            }
        }
    }

    fun onSaveInstanceState(bundle: Bundle?) {
        with(mPresenterActivity) {
            bundle?.putParcelable(BUNDLE_PRESIDENTES_LAYOUT_MANAGER, presidentes_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_SENADORES_LAYOUT_MANAGER, senadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_GOVERNADORES_LAYOUT_MANAGER, governadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_DEPUTADOS_LAYOUT_MANAGER, deputados_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelableArrayList(BUNDLE_PRESIDENTES_LIST, getSortedPresidentes())
            bundle?.putParcelableArrayList(BUNDLE_SENADORES_LIST, getSortedSenadores())
            bundle?.putParcelableArrayList(BUNDLE_GOVERNADORES_LIST, getSortedGovernadores())
            bundle?.putParcelableArrayList(BUNDLE_DEPUTADOS_LIST, getSortedDeputados())
        }
    }
}