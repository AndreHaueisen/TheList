package com.andrehaueisen.listadejanot.j_main_lists

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.e_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.FabMenu
import kotlinx.android.synthetic.main.j_activity_main_lists.*

/**
 * Created by andre on 11/1/2017.
 */
class MainListsView(private val mPresenterActivity: MainListsPresenterActivity) {

    fun setViews(bundle: Bundle?) {
        setRecyclerViews(bundle)
        setToolbar()
        setFabMenu()
    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(main_lists_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    private fun setRecyclerViews(bundle: Bundle?) {
        with(mPresenterActivity) {
            val senadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val governadoresLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val deputadosLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            if (bundle != null) {
                senadoresLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_SENADORES_LAYOUT_MANAGER))
                governadoresLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_GOVERNADORES_LAYOUT_MANAGER))
                deputadosLayoutManager.onRestoreInstanceState(bundle.getParcelable<LinearLayoutManager.SavedState>(BUNDLE_DEPUTADOS_LAYOUT_MANAGER))
                getSortedSenadores().addAll(bundle.getParcelableArrayList(BUNDLE_SENADORES_LIST))
                getSortedGovernadores().addAll(bundle.getParcelableArrayList(BUNDLE_GOVERNADORES_LIST))
                getSortedDeputados().addAll(bundle.getParcelableArrayList(BUNDLE_DEPUTADOS_LIST))
            }

            senadores_recycler_view.layoutManager = senadoresLayoutManager
            senadores_recycler_view.setHasFixedSize(true)
            senadores_recycler_view.adapter = MainListsAdapter(this, getSortedSenadores(), getSortType()!!)

            governadores_recycler_view.layoutManager = governadoresLayoutManager
            governadores_recycler_view.setHasFixedSize(true)
            governadores_recycler_view.adapter = MainListsAdapter(this, getSortedGovernadores(), getSortType()!!)

            deputados_recycler_view.layoutManager = deputadosLayoutManager
            deputados_recycler_view.setHasFixedSize(true)
            deputados_recycler_view.adapter = MainListsAdapter(this, getSortedDeputados(), getSortType()!!)

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

    fun onSaveInstanceState(bundle: Bundle?) {
        with(mPresenterActivity) {
            bundle?.putParcelable(BUNDLE_SENADORES_LAYOUT_MANAGER, senadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_GOVERNADORES_LAYOUT_MANAGER, governadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_DEPUTADOS_LAYOUT_MANAGER, deputados_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelableArrayList(BUNDLE_SENADORES_LIST, getSortedSenadores())
            bundle?.putParcelableArrayList(BUNDLE_GOVERNADORES_LIST, getSortedGovernadores())
            bundle?.putParcelableArrayList(BUNDLE_DEPUTADOS_LIST, getSortedDeputados())
        }
    }
}