package com.andrehaueisen.listadejanot.i_main_lists_choices.mvp

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AlertDialog
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_search_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.e_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.j_main_lists.MainListsPresenterActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.FabMenu
import kotlinx.android.synthetic.main.i_activity_main_lists_choices.*

/**
 * Created by andre on 10/22/2017.
 */
class MainListsChoicesView(private val mPresenterActivity: MainListsChoicesPresenterActivity) {

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null
    private var mSortType = SortType.RECOMMENDATIONS_COUNT

    fun setViews() {
        setToolbar()
        setFabMenu()
        setButtons()
    }

    fun beginDatabaseLoadingAlertDialog() {
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

    private fun setToolbar() {
        with(mPresenterActivity) {
            setSupportActionBar(main_lists_choices_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    fun notifyPoliticiansReady(senadores: ArrayList<Politician>, governadores: ArrayList<Politician>, deputados: ArrayList<Politician>) {
        with(mPresenterActivity) {
            val extras = Bundle()
            extras.putParcelableArrayList(BUNDLE_SENADORES_LIST, senadores)
            extras.putParcelableArrayList(BUNDLE_GOVERNADORES_LIST, governadores)
            extras.putParcelableArrayList(BUNDLE_DEPUTADOS_LIST, deputados)
            extras.putString(BUNDLE_SORT_TYPE, mSortType.name)

            val toolbarPair = Pair<View, String>(main_lists_choices_toolbar as View, getString(R.string.transition_toolbar))
            val fabMenuPair = Pair<View, String>(menu_fab as View, this.getString(R.string.transition_button))
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, fabMenuPair, toolbarPair)

            startNewActivity(MainListsPresenterActivity::class.java, extras = extras, options = options.toBundle())
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

    private fun setButtons() {
        with(mPresenterActivity) {
            top_recommendations_view.setOnClickListener {
                mSortType = SortType.RECOMMENDATIONS_COUNT
                sortPoliticians(mSortType)
            }

            top_condemnations_view.setOnClickListener {
                mSortType = SortType.CONDEMNATIONS_COUNT
                sortPoliticians(mSortType)
            }

            top_overall_grade_view.setOnClickListener {
                mSortType = SortType.TOP_OVERALL_GRADE
                sortPoliticians(mSortType)
            }

            worst_overall_grade_view.setOnClickListener {
                mSortType = SortType.WORST_OVERALL_GRADE
                sortPoliticians(mSortType)

            }

            search_view.setOnClickListener {
                val toolbarPair = Pair<View, String>(main_lists_choices_toolbar as View, getString(R.string.transition_toolbar))
                val fabMenuPair = Pair<View, String>(menu_fab as View, this.getString(R.string.transition_button))
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, fabMenuPair, toolbarPair)
                startNewActivity(PoliticianSelectorPresenterActivity::class.java, options = options.toBundle())
            }
        }
    }
}