package com.andrehaueisen.listadejanot.i_main_lists.mvp

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_search_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.e_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.i_main_lists.MainListsAdapter
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.FabMenu
import kotlinx.android.synthetic.main.i_activity_main_lists.*

/**
 * Created by andre on 10/22/2017.
 */
class MainListsView(private val mPresenterActivity: MainListsPresenterActivity) {

    private val sortedDeputados = ArrayList<Politician>(10)
    private val sortedSenadores = ArrayList<Politician>(10)
    private val sortedGovernadores = ArrayList<Politician>(10)
    private var mLoadingDatabaseAlertDialog: AlertDialog? = null
    private var areButtonsVisible = true

    fun setViews(bundle: Bundle?) {
        if (bundle == null) {
            beginDatabaseLoadingAlertDialog()
        } else {
            setViewsVisibility(bundle)
            setListsTitles(bundle = bundle)
        }
        setToolbar()
        setRecyclerViews(bundle)
        setFabMenu()
        setButtons()
    }

    private fun beginDatabaseLoadingAlertDialog() {
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mPresenterActivity)
                .setCancelable(true)
                .setIcon(mPresenterActivity.getDrawable(R.drawable.ic_launcher))
                .setTitle(mPresenterActivity.getString(R.string.dialog_title_loading_database))
                .setMessage("")
                .create()

        mLoadingDatabaseAlertDialog?.show()
    }

    fun dismissAlertDialog() {
        val isAlertDialogActive = (mLoadingDatabaseAlertDialog != null && mLoadingDatabaseAlertDialog?.isShowing!!)
        if (isAlertDialogActive) {
            mLoadingDatabaseAlertDialog?.dismiss()
        }
    }

    private fun setViewsVisibility(bundle: Bundle?) {
        areButtonsVisible = bundle!!.getBoolean(BUNDLE_ARE_BUTTONS_VISIBLE)
        if (areButtonsVisible) showButtons() else showLists()
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
                sortedSenadores.addAll(bundle.getParcelableArrayList(BUNDLE_SENADORES_LIST))
                sortedGovernadores.addAll(bundle.getParcelableArrayList(BUNDLE_GOVERNADORES_LIST))
                sortedDeputados.addAll(bundle.getParcelableArrayList(BUNDLE_DEPUTADOS_LIST))
            }

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

    fun onBackPressed() {
        with(mPresenterActivity) {
            showButtons()
        }
    }

    private fun clearData() {

        if (sortedDeputados.isNotEmpty() || sortedGovernadores.isNotEmpty() || sortedSenadores.isNotEmpty()) {
            sortedDeputados.clear()
            sortedGovernadores.clear()
            sortedSenadores.clear()
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
                            invalidateUser()
                            startNewActivity(LoginActivity::class.java)
                        }
                    }
                }
            })
        }
    }

    private fun setButtons() {
        with(mPresenterActivity) {
            top_in_recommendations_view.setOnClickListener {
                clearData()

                /*setListsTitles(getString(R.string.senadores_sorted_by_vote_intentions),
                        getString(R.string.governadores_sorted_by_vote_intentions),
                        getString(R.string.deputados_sorted_by_vote_intentions))*/

                (senadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.RECOMMENDATIONS_COUNT)
                (governadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.RECOMMENDATIONS_COUNT)
                (deputados_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.RECOMMENDATIONS_COUNT)

                showLists()

                sortPoliticians(SortType.RECOMMENDATIONS_COUNT)
            }

            top_in_condemnations_view.setOnClickListener {
                clearData()

                /*setListsTitles(getString(R.string.senadores_sorted_by_suspicions),
                        getString(R.string.governadores_sorted_by_suspicions),
                        getString(R.string.deputados_sorted_by_suspicions))*/

                (senadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.CONDEMNATIONS_COUNT)
                (governadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.CONDEMNATIONS_COUNT)
                (deputados_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.CONDEMNATIONS_COUNT)

                showLists()

                sortPoliticians(SortType.CONDEMNATIONS_COUNT)
            }

            top_in_overall_grade_view.setOnClickListener {
                clearData()

                /*setListsTitles(getString(R.string.senadores_sorted_by_overall_grade),
                        getString(R.string.governadores_sorted_by_overall_grade),
                        getString(R.string.deputados_sorted_by_overall_grade))*/

                (senadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.OVERALL_GRADE)
                (governadores_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.OVERALL_GRADE)
                (deputados_recycler_view.adapter as MainListsAdapter).changeSortType(SortType.OVERALL_GRADE)

                showLists()

                sortPoliticians(SortType.OVERALL_GRADE)
            }

            search_view.setOnClickListener {
                val toolbarPair = Pair<View, String>(main_lists_toolbar as View, getString(R.string.transition_toolbar))
                val fabMenuPair = Pair<View, String>(menu_fab as View, this.getString(R.string.transition_button))
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, fabMenuPair, toolbarPair)
                startNewActivity(PoliticianSelectorPresenterActivity::class.java, options = options.toBundle())
            }
        }
    }

    private fun setListsTitles(senadoresListTitle: String = "", governadoresListTile: String = "", deputadosListTitle: String = "", bundle: Bundle? = null) {
        with(mPresenterActivity) {

            if (bundle != null) {
                senadores_title_text_view.text = bundle.getString(BUNDLE_SENADORES_LIST_TITLE)
                governadores_title_text_view.text = bundle.getString(BUNDLE_GOVERNADORES_LIST_TITLE)
                deputados_title_text_view.text = bundle.getString(BUNDLE_DEPUTADOS_LIST_TITLE)
            } else {
                senadores_title_text_view.text = senadoresListTitle
                governadores_title_text_view.text = governadoresListTile
                deputados_title_text_view.text = deputadosListTitle
            }
        }
    }

    private fun showButtons() {
        with(mPresenterActivity) {
            group_buttons.visibility = View.VISIBLE
            group_lists.visibility = View.INVISIBLE
            areButtonsVisible = true
        }
    }

    private fun showLists() {
        with(mPresenterActivity) {
            group_buttons.visibility = View.GONE
            group_lists.visibility = View.VISIBLE
            areButtonsVisible = false
        }
    }

    fun onSaveInstanceState(bundle: Bundle?) {
        with(mPresenterActivity) {
            bundle?.putParcelable(BUNDLE_SENADORES_LAYOUT_MANAGER, senadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_GOVERNADORES_LAYOUT_MANAGER, governadores_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelable(BUNDLE_DEPUTADOS_LAYOUT_MANAGER, deputados_recycler_view.layoutManager.onSaveInstanceState())
            bundle?.putParcelableArrayList(BUNDLE_SENADORES_LIST, sortedSenadores)
            bundle?.putParcelableArrayList(BUNDLE_GOVERNADORES_LIST, sortedGovernadores)
            bundle?.putParcelableArrayList(BUNDLE_DEPUTADOS_LIST, sortedDeputados)
            bundle?.putString(BUNDLE_SENADORES_LIST_TITLE, senadores_title_text_view.text.toString())
            bundle?.putString(BUNDLE_GOVERNADORES_LIST_TITLE, governadores_title_text_view.text.toString())
            bundle?.putString(BUNDLE_DEPUTADOS_LIST_TITLE, deputados_title_text_view.text.toString())
            bundle?.putBoolean(BUNDLE_ARE_BUTTONS_VISIBLE, areButtonsVisible)
        }
    }
}