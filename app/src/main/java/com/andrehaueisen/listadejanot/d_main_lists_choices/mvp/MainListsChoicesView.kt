package com.andrehaueisen.listadejanot.d_main_lists_choices.mvp

import android.app.ActivityOptions
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.Window
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.e_main_lists.MainListsPresenterActivity
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.i_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.j_login.LoginActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.FabMenu
import kotlinx.android.synthetic.main.d_activity_main_lists_choices.*


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
        with(mPresenterActivity) {
            mLoadingDatabaseAlertDialog = AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setIcon(getDrawable(R.drawable.ic_launcher))
                    .setTitle(mPresenterActivity.getString(R.string.dialog_title_loading_database))
                    .setMessage("")
                    .create()

            mLoadingDatabaseAlertDialog?.show()

        }
    }

    fun verifyUserTokenValidity() {
        with(mPresenterActivity) {
            getAuthenticator().getCurrentUser()?.getIdToken(false)?.addOnFailureListener(this,
                    { exception ->
                        val message = exception.message

                        Log.e("ListsChoicesActivity", message)
                        showToast(getString(R.string.login_required))

                        mLoadingDatabaseAlertDialog?.dismiss()
                        mFirebaseAuthenticator.logout()
                        startNewActivity(LoginActivity::class.java)
                    })

        }
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

            startNewActivity(MainListsPresenterActivity::class.java, extras = extras)
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

                val statusBar = findViewById<View>(android.R.id.statusBarBackground)
                val navigationBar = findViewById<View>(android.R.id.navigationBarBackground)

                val statusBarPair = Pair(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
                val navigationBarPair = Pair(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)
                val toolbarPair = Pair(main_lists_choices_toolbar as View, getString(R.string.transition_toolbar))
                val fabMenuPair = Pair(menu_fab as View, this.getString(R.string.transition_button))

                val options = ActivityOptions.makeSceneTransitionAnimation(this, statusBarPair, navigationBarPair, fabMenuPair, toolbarPair)
                startNewActivity(PoliticianSelectorPresenterActivity::class.java, options = options.toBundle())
            }
        }
    }
}