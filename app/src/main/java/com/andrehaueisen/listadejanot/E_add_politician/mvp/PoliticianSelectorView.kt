package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.andrehaueisen.listadejanot.E_add_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.e_activity_politician_selector.*


/**
 * Created by andre on 5/11/2017.
 */
class PoliticianSelectorView(val mActivityPresenter: PoliticianSelectorPresenterActivity): PoliticianSelectorMvpContract.View {

    private val mAutoCompleteTextView : AutoCompleteTextView
    private val mGlide = Glide.with(mActivityPresenter)
    private val mBlurry = Blurry.with(mActivityPresenter)
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

    private fun setAutoCompleteTextView(){

        val adapter = AutoCompletionAdapter(mActivityPresenter, R.layout.item_politician_identifier, mSearchablePoliticiansList)
        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        mAutoCompleteTextView.threshold = 1

    }

    override fun notifySearchablePoliticiansNewList(politicians: ArrayList<Politician>) {
        setAutoCompleteTextView(politicians)
    }

    private fun setAutoCompleteTextView(politicians: ArrayList<Politician>){

        mSearchablePoliticiansList = politicians
        val adapter = AutoCompletionAdapter(mActivityPresenter, R.layout.item_politician_identifier, politicians)
        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        mAutoCompleteTextView.threshold = 1
        mAutoCompleteTextView.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val politicianName = (view.findViewById(R.id.name_text_view) as TextView).text.toString()
                mActivityPresenter.subscribeToIndividualModel(politicianName)
            }
        }

        mLoadingDatabaseAlertDialog.dismiss()
    }

    override fun notifyPoliticianReady(politician: Politician) {
        val mCardView: CardView = mActivityPresenter.card_view
        val mMoldView: View = mActivityPresenter.mold_image_view
        val mBlurImageView = mActivityPresenter.blur_background_image_view
        val mPoliticianImageView: ImageView = mActivityPresenter.politician_image_view
        val mNameTextView: TextView = mActivityPresenter.name_text_view
        val mEmailTextView = mActivityPresenter.email_text_view
        val mVotesNumberTextView : TextView = mActivityPresenter.votes_number_text_view
        val mAnimatedBadgeImageView: ImageView = mActivityPresenter.badge_image_view
        val mVoteButton: ToggleButton = mActivityPresenter.add_to_vote_count_image_view

        val completePolitician = mSearchablePoliticiansList.first { it.name == politician.name }
        completePolitician.image = politician.image

        mGlide.load(politician.image)
                .crossFade()
                .placeholder(R.drawable.politician_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPoliticianImageView)

        val politicianImage = BitmapFactory.decodeResource(mActivityPresenter.resources, R.drawable.congresso_nacional_portrait)

        mBlurry.async()
                .sampling(5)
                .animate(1000)
                .from(politicianImage)
                .into(mBlurImageView)

        mNameTextView.text = completePolitician.name
        mVotesNumberTextView.text = completePolitician.votesNumber.toString()
        mEmailTextView.text = completePolitician.email

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