package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.andrehaueisen.listadejanot.E_add_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.ImageProcessor
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations.*
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.e_activity_politician_selector.*


/**
 * Created by andre on 5/11/2017.
 */
class PoliticianSelectorView(val mActivityPresenter: PoliticianSelectorPresenterActivity): PoliticianSelectorMvpContract.View {

    private val mAutoCompleteTextView : AutoCompleteTextView
    private val mDeleteTextImageButton : ImageButton
    private val mCardView : CardView
    private val mMoldView : View
    private val mBlurImageView : ImageView
    private val mPoliticianImageView: ImageView
    private val mAnimatedBadgeImageView: ImageView
    private val mExplanationTextView : TextView
    private val mNameTextView: TextView
    private val mEmailTextView : TextView
    private val mVotesNumberTextView : TextView
    private val mVoteButton: ToggleButton

    private val mGlide = Glide.with(mActivityPresenter)
    private val mBlurry = Blurry.with(mActivityPresenter)

    private var mLoadingDatabaseAlertDialog : AlertDialog? = null

    private val DEFAULT_ANIM_DURATION = 500L

    init {
        mActivityPresenter.setContentView(R.layout.e_activity_politician_selector)

        mAutoCompleteTextView = mActivityPresenter.auto_complete_text_view
        mDeleteTextImageButton = mActivityPresenter.delete_text_image_button
        mCardView = mActivityPresenter.card_view
        mMoldView = mActivityPresenter.mold_image_view
        mBlurImageView = mActivityPresenter.blur_background_image_view
        mPoliticianImageView = mActivityPresenter.politician_image_view
        mExplanationTextView = mActivityPresenter.selector_explanation_text_view
        mNameTextView = mActivityPresenter.name_text_view
        mEmailTextView = mActivityPresenter.email_text_view
        mVotesNumberTextView = mActivityPresenter.votes_number_text_view
        mAnimatedBadgeImageView = mActivityPresenter.badge_image_view
        mVoteButton = mActivityPresenter.add_to_vote_count_image_view
    }

    override fun setViews(isSavedState: Boolean) {
        setToolbar()
        setViewsInitialState()

        if(isSavedState) {
            setAutoCompleteTextView()
            if(mActivityPresenter.getNameImagePair() != null) {
                bindPoliticianDataToViews()
                initiateShowAnimations()
            }
        }else {
            beginDatabaseLoadingAlertDialog()
        }

    }

    private fun setToolbar(){
        val toolbar = mActivityPresenter.select_politician_toolbar
        mActivityPresenter.setSupportActionBar(toolbar)
        mActivityPresenter.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setViewsInitialState(){
        val backGroundImage = ImageProcessor.resamplePic(mActivityPresenter, mActivityPresenter.resources, R.drawable.congresso_nacional_portrait)
        mBlurImageView.setImageBitmap(backGroundImage)

        ExpectAnim()
                .expect(mCardView)
                .toBe(alpha(0.0f), outOfScreen(Gravity.TOP))
                .expect(mPoliticianImageView)
                .toBe(alpha(0.0f), outOfScreen(Gravity.TOP))
                .expect(mDeleteTextImageButton)
                .toBe(alpha(0.0f))
                .toAnimation()
                .setNow()
    }

    private fun setAutoCompleteTextView(){
        val nonReliablePoliticiansList = ArrayList<Politician>()
        nonReliablePoliticiansList.addAll(mActivityPresenter.getOriginalSearchablePoliticiansList())

        val adapter = AutoCompletionAdapter(mActivityPresenter, R.layout.item_politician_identifier, nonReliablePoliticiansList)
        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        setOnCompleteTextViewClickListener()
        setOnDeleteTextClickListener()
        dismissAlertDialog()

    }

    private fun setOnCompleteTextViewClickListener(){

        mAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { view, _, _, _ ->
            dismissKeyBoard()

            val politicianName = mAutoCompleteTextView.text.toString()
            mActivityPresenter.subscribeToIndividualModel(politicianName)
        }
    }

    fun setOnDeleteTextClickListener(){
        mDeleteTextImageButton.setOnClickListener {
            mAutoCompleteTextView.text.clear()
            ExpectAnim()
                    .expect(mDeleteTextImageButton)
                    .toBe(alpha(0.0f))
                    .toAnimation()
                    .setDuration(DEFAULT_ANIM_DURATION)
                    .start()
        }
    }

    private fun beginDatabaseLoadingAlertDialog(){
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mActivityPresenter)
                .setCancelable(false)
                .setTitle(mActivityPresenter.getString(R.string.dialog_title_loading_database))
                .setMessage(mActivityPresenter.getString(R.string.dialog_message_loading_database))
                .create()

        mLoadingDatabaseAlertDialog?.show()
    }

    private fun dismissAlertDialog(){
        val isAlertDialogActive = (mLoadingDatabaseAlertDialog != null && mLoadingDatabaseAlertDialog?.isShowing!!)
        if(isAlertDialogActive) {
            mLoadingDatabaseAlertDialog?.dismiss()
        }
    }

    override fun notifySearchablePoliticiansNewList() {
        setAutoCompleteTextView()
    }

    private fun dismissKeyBoard(){
        val manager = mActivityPresenter.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(mAutoCompleteTextView.windowToken, 0)
    }

    override fun notifyPoliticianImageReady() {
        bindPoliticianDataToViews()
        initiateShowAnimations()
    }

    private fun bindPoliticianDataToViews(){

        val politicianName = mActivityPresenter.getNameImagePair()?.first
        val politicianImage = mActivityPresenter.getNameImagePair()?.second

        val completePolitician = mActivityPresenter.getOriginalSearchablePoliticiansList().first { it.name == politicianName }
        completePolitician.image = politicianImage

        mGlide.load(completePolitician.image)
                .crossFade()
                .placeholder(R.drawable.politician_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPoliticianImageView)

        mNameTextView.text = completePolitician.name
        mVotesNumberTextView.text = completePolitician.votesNumber.toString()
        mEmailTextView.text = completePolitician.email

    }

    fun initiateShowAnimations(){
        val backgroundBlurImage = ImageProcessor.resamplePic(mActivityPresenter, mActivityPresenter.resources, R.drawable.congresso_nacional_portrait)
        mBlurry
                .sampling(5)
                .animate(2000)
                .from(backgroundBlurImage)
                .into(mBlurImageView)

        ExpectAnim()
                .expect(mExplanationTextView)
                .toBe(invisible(), outOfScreen(Gravity.BOTTOM))
                .expect(mCardView)
                .toBe(alpha(0.6f), atItsOriginalPosition())
                .expect(mPoliticianImageView)
                .toBe(alpha(1.0f), atItsOriginalPosition())
                .expect(mDeleteTextImageButton)
                .toBe(alpha(1.0f))

                .toAnimation()
                .setDuration(DEFAULT_ANIM_DURATION)
                .start()

    }

    override fun onCreateOptionsMenu(menu: Menu?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
    }



}