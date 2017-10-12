package com.andrehaueisen.listadejanot.e_information.mvp

import android.view.MenuItem
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.utilities.SHARED_MINIMUM_VALUE_TO_MAIN_LIST
import com.andrehaueisen.listadejanot.utilities.pullIntFromSharedPreferences
import kotlinx.android.synthetic.main.e_activity_information_presenter.*


/**
 * Created by andre on 6/5/2017.
 */
class InformationView(private val mPresenterActivity: InformationPresenterActivity) : InformationMvpContract.View {

    init {
        mPresenterActivity.setContentView(R.layout.e_activity_information_presenter)
    }

    override fun setViews() = with(mPresenterActivity) {
        fun setToolbar() {
            val actionBar = information_toolbar
            setSupportActionBar(actionBar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }

        fun setMotivationTextView(){
            val minVoteNumberToMainList = mPresenterActivity.pullIntFromSharedPreferences(SHARED_MINIMUM_VALUE_TO_MAIN_LIST)
            app_motivation_text_view.text = mPresenterActivity.getString(R.string.understand_app_motivation, minVoteNumberToMainList)
        }

        setToolbar()
        setMotivationTextView()
    }

    override fun onOptionsItemSelected(item: MenuItem) {

        when (item.itemId) {
            android.R.id.home -> mPresenterActivity.finish()
        }
    }
}