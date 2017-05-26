package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.andrehaueisen.listadejanot.E_add_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations.*
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.e_activity_politician_selector.*


/**
 * Created by andre on 5/11/2017.
 */

class PoliticianSelectorView(val mPresenterActivity: PoliticianSelectorPresenterActivity) : PoliticianSelectorMvpContract.View {

    private val DEFAULT_ANIM_DURATION = 500L
    private val mGlide = Glide.with(mPresenterActivity)
    private val mBlurry = Blurry.with(mPresenterActivity)
    private val mPoliticianThiefAnimation: AnimatedVectorDrawable
    private val mThiefPoliticianAnimation: AnimatedVectorDrawable

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null

    private var mIsShowingPoliticianDrawable = true

    init {
        mPresenterActivity.setContentView(R.layout.e_activity_politician_selector)

        mPoliticianThiefAnimation = mPresenterActivity.getDrawable(R.drawable.politician_thief_animated_vector) as AnimatedVectorDrawable
        mThiefPoliticianAnimation = mPresenterActivity.getDrawable(R.drawable.thief_politician_animated_vector) as AnimatedVectorDrawable
    }

    override fun setViews(isSavedState: Boolean) {
        setToolbar()
        setViewsInitialState()
        if (isSavedState) {
            setAutoCompleteTextView()
            if (mPresenterActivity.getSinglePolitician() != null) {
                bindPoliticianDataToViews(mPresenterActivity.getSinglePolitician() as Politician)
                initiateShowAnimations()
                notifyPoliticianReady()
            }
        } else {
            beginDatabaseLoadingAlertDialog()
        }

    }

    private fun setToolbar() {
        val toolbar = mPresenterActivity.select_politician_toolbar
        mPresenterActivity.setSupportActionBar(toolbar)
        mPresenterActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setViewsInitialState() {
        val backGroundImage = ImageProcessor.resamplePic(mPresenterActivity, mPresenterActivity.resources, R.drawable.simbolo_justica)
        mPresenterActivity.blur_background_image_view.setImageBitmap(backGroundImage)

        ExpectAnim()
                .expect(mPresenterActivity.constraint_layout)
                .toBe(alpha(0.0f), outOfScreen(Gravity.TOP))
                .expect(mPresenterActivity.delete_text_image_button)
                .toBe(alpha(0.0f))
                .expect(mPresenterActivity.plus_one_text_view)
                .toBe(alpha(0.0f))
                .toAnimation()
                .setNow()
    }

    private fun setAutoCompleteTextView() {
        val nonReliablePoliticiansList = ArrayList<Politician>()
        nonReliablePoliticiansList.addAll(mPresenterActivity.getOriginalSearchablePoliticiansList())

        val adapter = AutoCompletionAdapter(mPresenterActivity,
                R.layout.item_politician_identifier,
                nonReliablePoliticiansList,
                mPresenterActivity.getOriginalSearchablePoliticiansList())

        mPresenterActivity.auto_complete_text_view.setAdapter<ArrayAdapter<Politician>>(adapter)
        setOnCompleteTextViewClickListener()
        setOnDeleteTextClickListener()

        dismissAlertDialog()
    }

    private fun setOnCompleteTextViewClickListener() {

        mPresenterActivity
                .auto_complete_text_view
                .onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            dismissKeyBoard()

            val politicianName = mPresenterActivity.auto_complete_text_view.text.toString()
            mPresenterActivity.subscribeToSinglePoliticianModel(politicianName)
        }
    }

    fun setOnDeleteTextClickListener() {
        mPresenterActivity.delete_text_image_button.setOnClickListener {

            mPresenterActivity.auto_complete_text_view.text.clear()
            ExpectAnim()
                    .expect(mPresenterActivity.delete_text_image_button)
                    .toBe(alpha(0.0f))
                    .toAnimation()
                    .setDuration(DEFAULT_ANIM_DURATION)
                    .start()
        }
    }

    private fun beginDatabaseLoadingAlertDialog() {
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mPresenterActivity)
                .setCancelable(false)
                .setTitle(mPresenterActivity.getString(R.string.dialog_title_loading_database))
                .setMessage(mPresenterActivity.getString(R.string.dialog_message_loading_database))
                .create()

        mLoadingDatabaseAlertDialog?.show()
    }

    fun dismissAlertDialog() {
        val isAlertDialogActive = (mLoadingDatabaseAlertDialog != null && mLoadingDatabaseAlertDialog?.isShowing!!)
        if (isAlertDialogActive) {
            mLoadingDatabaseAlertDialog?.dismiss()
        }
    }

    private fun dismissKeyBoard() {
        val manager = mPresenterActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(mPresenterActivity.auto_complete_text_view.windowToken, 0)
    }

    override fun notifySearchablePoliticiansNewList() {
        setAutoCompleteTextView()
    }

    override fun notifyPoliticianReady() {

        val politician = mPresenterActivity.getSinglePolitician()

        if (politician != null) {
            bindPoliticianDataToViews(politician)
            initiateShowAnimations()

            if (politician.condemnedBy.contains(FAKE_USER_EMAIL.encodeEmail())) {
                configureInitialCondemnStatus(politician)
            } else {
                configureInitialAbsolveStatus(politician)
            }
            setVoteButtonClickListener(politician)
        }
    }

    private fun bindPoliticianDataToViews(politician: Politician) {

        mGlide.load(politician.image)
                .crossFade()
                .placeholder(R.drawable.politician_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPresenterActivity.politician_image_view)

        mPresenterActivity.post_text_view.text = politician.post.name
        mPresenterActivity.name_text_view.text = politician.name
        mPresenterActivity.votes_number_text_view.text = politician.votesNumber.toString()
        mPresenterActivity.email_text_view.text = politician.email
    }

    fun initiateShowAnimations() {
        val backgroundBlurImage = ImageProcessor.resamplePic(mPresenterActivity, mPresenterActivity.resources, R.drawable.simbolo_justica)

        mBlurry
                .sampling(5)
                .animate(2000)
                .from(backgroundBlurImage)
                .into(mPresenterActivity.blur_background_image_view)

        ExpectAnim()
                .expect(mPresenterActivity.explanation_text_view)
                .toBe(invisible(), outOfScreen(Gravity.BOTTOM))
                .expect(mPresenterActivity.constraint_layout)
                .toBe(alpha(1.0f), atItsOriginalPosition())
                .expect(mPresenterActivity.delete_text_image_button)
                .toBe(alpha(1.0f))

                .toAnimation()
                .setDuration(DEFAULT_ANIM_DURATION)
                .start()

    }

    private fun configureInitialCondemnStatus(politician: Politician){
        ExpectAnim()
                .expect(mPresenterActivity.plus_one_text_view)
                .toBe(sameCenterAs(mPresenterActivity.votes_number_text_view, true, true))
                .toAnimation().setNow()

        mPresenterActivity.votes_number_text_view.text = politician.votesNumber.toString()
        mPresenterActivity.add_to_vote_count_toggle_button.isChecked = true
        mPresenterActivity.badge_image_view.setImageDrawable(mThiefPoliticianAnimation)

        mIsShowingPoliticianDrawable = true
        changeBadgeStatus()
    }

    private fun configureInitialAbsolveStatus(politician: Politician){

        mPresenterActivity.votes_number_text_view.text = politician.votesNumber.toString()
        mPresenterActivity.add_to_vote_count_toggle_button.isChecked = false
        mPresenterActivity.badge_image_view.setImageDrawable(mPoliticianThiefAnimation)

        mIsShowingPoliticianDrawable = false
        changeBadgeStatus()
    }

    fun setVoteButtonClickListener(politician: Politician) {
        mPresenterActivity.add_to_vote_count_toggle_button.setOnClickListener {
            mPresenterActivity.updatePoliticianVote(politician, this)
        }
    }

    override fun initiateCondemnAnimations(politician: Politician) {
        mPresenterActivity.plus_one_text_view.text = mPresenterActivity.getString(R.string.plus_one)
        changeBadgeStatus()
        ExpectAnim().plusOneCondemnAnimation(mPresenterActivity.window.decorView.rootView, politician)
    }

    override fun initiateAbsolveAnimations(politician: Politician) {
        mPresenterActivity.plus_one_text_view.text = mPresenterActivity.getString(R.string.minus_one)
        changeBadgeStatus()
        ExpectAnim().minusOneAbsolveAnimation(mPresenterActivity.window.decorView.rootView, politician)
    }

    private fun changeBadgeStatus() {

        if (mPresenterActivity.badge_image_view.drawable == mPoliticianThiefAnimation && mIsShowingPoliticianDrawable) {
            (mPresenterActivity.badge_image_view.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = false

        } else if (mPresenterActivity.badge_image_view.drawable == mPoliticianThiefAnimation && !mIsShowingPoliticianDrawable) {
            mPresenterActivity.badge_image_view.setImageDrawable(mThiefPoliticianAnimation)
            (mPresenterActivity.badge_image_view.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = true

        } else if (mPresenterActivity.badge_image_view.drawable == mThiefPoliticianAnimation && mIsShowingPoliticianDrawable) {
            mPresenterActivity.badge_image_view.setImageDrawable(mPoliticianThiefAnimation)
            (mPresenterActivity.badge_image_view.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = false

        } else if (mPresenterActivity.badge_image_view.drawable == mThiefPoliticianAnimation && !mIsShowingPoliticianDrawable) {
            (mPresenterActivity.badge_image_view.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
    }
}