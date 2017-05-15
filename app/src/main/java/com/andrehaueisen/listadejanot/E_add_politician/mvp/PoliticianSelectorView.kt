package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import com.andrehaueisen.listadejanot.E_add_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import kotlinx.android.synthetic.main.e_activity_politician_selector.*


/**
 * Created by andre on 5/11/2017.
 */
class PoliticianSelectorView(val mActivityPresenter: PoliticianSelectorPresenterActivity): PoliticianSelectorMvpContract.View {


    private val mAutoCompleteTextView : AppCompatMultiAutoCompleteTextView
    lateinit private var mSearchablePoliticiansList: ArrayList<Politician>
    lateinit private var mLoadingDatabaseAlertDialog : AlertDialog

    init {
        mActivityPresenter.setContentView(R.layout.e_activity_politician_selector)
        mAutoCompleteTextView = mActivityPresenter.auto_complete_text_view
    }

    constructor(activitySelectorPresenter: PoliticianSelectorPresenterActivity, savedInstanceState: Bundle) : this(activitySelectorPresenter){
        if (!savedInstanceState.isEmpty){
            mSearchablePoliticiansList = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_SEARCHABLE_POLITICIANS)
        }
    }

    override fun setViews(isSavedState: Boolean) {
        setToolbar()

        if(!isSavedState){
            beginLoadingDatabaseAlertDialog()
        }else{
            setAutoCompleteTextView()
        }
    }

    private fun setToolbar(){
        val toolbar = mActivityPresenter.select_politician_toolbar
        mActivityPresenter.setSupportActionBar(toolbar)
        mActivityPresenter.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun beginLoadingDatabaseAlertDialog(){
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mActivityPresenter)
                .setCancelable(false)
                .setTitle(mActivityPresenter.getString(R.string.dialog_title_loading_database))
                .setMessage(mActivityPresenter.getString(R.string.dialog_message_loading_database))
                .create()

        mLoadingDatabaseAlertDialog.show()
    }

    private fun setAutoCompleteTextView(politicians: ArrayList<Politician>){

        mSearchablePoliticiansList = politicians
        val adapter = AutoCompletionAdapter(mActivityPresenter, R.layout.item_politician_identifier, politicians)
        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        mAutoCompleteTextView.threshold = 1
        mAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        mLoadingDatabaseAlertDialog.dismiss()
    }

    override fun notifySearchablePoliticiansNewList(politicians: ArrayList<Politician>) {
        setAutoCompleteTextView(politicians)
    }

    private fun setAutoCompleteTextView(){

        val adapter = AutoCompletionAdapter(mActivityPresenter, R.layout.item_politician_identifier, mSearchablePoliticiansList)
        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        mAutoCompleteTextView.threshold = 1
        mAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

    }

    override fun onCreateOptionsMenu(menu: Menu?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
    }

    override fun onSaveInstanceState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelableArrayList(Constants.BUNDLE_SEARCHABLE_POLITICIANS, mSearchablePoliticiansList)

        return bundle
    }


}