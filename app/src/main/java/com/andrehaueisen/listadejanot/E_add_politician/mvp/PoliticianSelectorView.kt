package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.andrehaueisen.listadejanot.E_add_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
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
class PoliticianSelectorView(val mActivityPresenter: PoliticianSelectorPresenterActivity) : PoliticianSelectorMvpContract.View {

    private val DEFAULT_ANIM_DURATION = 500L

    private val mAutoCompleteTextView: AutoCompleteTextView
    private val mDeleteTextImageButton: ImageButton
    private val mConstraintLayout: ConstraintLayout
    private val mMoldView: View
    private val mBlurImageView: ImageView
    private val mPoliticianImageView: ImageView
    private val mAnimatedBadgeImageView: ImageView
    private val mExplanationTextView: TextView
    private val mNameTextView: TextView
    private val mEmailTextView: TextView
    private val mVotesNumberTextView: TextView
    private val mVoteButton: ToggleButton
    private val mPoliticianThiefAnimation: AnimatedVectorDrawable
    private val mThiefPoliticianAnimation: AnimatedVectorDrawable

    private val mGlide = Glide.with(mActivityPresenter)
    private val mBlurry = Blurry.with(mActivityPresenter)

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null

    private var mIsShowingPoliticianDrawable = true

    init {
        mActivityPresenter.setContentView(R.layout.e_activity_politician_selector)
        mAutoCompleteTextView = mActivityPresenter.auto_complete_text_view
        mDeleteTextImageButton = mActivityPresenter.delete_text_image_button
        mConstraintLayout = mActivityPresenter.constraint_layout
        mMoldView = mActivityPresenter.mold_view
        mBlurImageView = mActivityPresenter.blur_background_image_view
        mPoliticianImageView = mActivityPresenter.politician_image_view
        mExplanationTextView = mActivityPresenter.selector_explanation_text_view
        mNameTextView = mActivityPresenter.name_text_view
        mEmailTextView = mActivityPresenter.email_text_view
        mVotesNumberTextView = mActivityPresenter.votes_number_text_view
        mAnimatedBadgeImageView = mActivityPresenter.badge_image_view
        mVoteButton = mActivityPresenter.add_to_vote_count_image_view

        mPoliticianThiefAnimation = mActivityPresenter.getDrawable(R.drawable.politician_thief_animated_vector) as AnimatedVectorDrawable
        mThiefPoliticianAnimation = mActivityPresenter.getDrawable(R.drawable.thief_politician_animated_vector) as AnimatedVectorDrawable
    }

    override fun setViews(isSavedState: Boolean) {
        setToolbar()
        setViewsInitialState()

        if (isSavedState) {
            setAutoCompleteTextView()
            if (mActivityPresenter.getSinglePolitician() != null) {
                bindPoliticianDataToViews(mActivityPresenter.getSinglePolitician() as Politician)
                initiateShowAnimations()
            }
        } else {
            beginDatabaseLoadingAlertDialog()
        }

    }

    private fun setToolbar() {
        val toolbar = mActivityPresenter.select_politician_toolbar
        mActivityPresenter.setSupportActionBar(toolbar)
        mActivityPresenter.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setViewsInitialState() {
        val backGroundImage = ImageProcessor.resamplePic(mActivityPresenter, mActivityPresenter.resources, R.drawable.simbolo_justica)
        mBlurImageView.setImageBitmap(backGroundImage)

        ExpectAnim()
                .expect(mConstraintLayout)
                .toBe(alpha(0.0f), outOfScreen(Gravity.TOP))
                .expect(mPoliticianImageView)
                .toBe(alpha(0.0f), outOfScreen(Gravity.TOP))
                .expect(mDeleteTextImageButton)
                .toBe(alpha(0.0f))
                .toAnimation()
                .setNow()
    }

    private fun setAutoCompleteTextView() {
        val nonReliablePoliticiansList = ArrayList<Politician>()
        nonReliablePoliticiansList.addAll(mActivityPresenter.getOriginalSearchablePoliticiansList())

        val adapter = AutoCompletionAdapter(mActivityPresenter,
                R.layout.item_politician_identifier,
                nonReliablePoliticiansList,
                mActivityPresenter.getOriginalSearchablePoliticiansList())

        mAutoCompleteTextView.setAdapter<ArrayAdapter<Politician>>(adapter)
        setOnCompleteTextViewClickListener()
        setOnDeleteTextClickListener()
        dismissAlertDialog()

    }

    private fun setOnCompleteTextViewClickListener() {

        mAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            dismissKeyBoard()

            val politicianName = mAutoCompleteTextView.text.toString()
            mActivityPresenter.subscribeToSinglePoliticianModel(politicianName)
        }
    }

    fun setOnDeleteTextClickListener() {
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

    private fun beginDatabaseLoadingAlertDialog() {
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mActivityPresenter)
                .setCancelable(false)
                .setTitle(mActivityPresenter.getString(R.string.dialog_title_loading_database))
                .setMessage(mActivityPresenter.getString(R.string.dialog_message_loading_database))
                .create()

        mLoadingDatabaseAlertDialog?.show()
    }

    private fun dismissAlertDialog() {
        val isAlertDialogActive = (mLoadingDatabaseAlertDialog != null && mLoadingDatabaseAlertDialog?.isShowing!!)
        if (isAlertDialogActive) {
            mLoadingDatabaseAlertDialog?.dismiss()
        }
    }

    private fun dismissKeyBoard() {
        val manager = mActivityPresenter.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(mAutoCompleteTextView.windowToken, 0)
    }

    override fun notifySearchablePoliticiansNewList() {
        setAutoCompleteTextView()
    }

    override fun notifyPoliticianReady() {

        val politician = mActivityPresenter.getSinglePolitician()

        if (politician != null) {
            bindPoliticianDataToViews(politician)
            initiateShowAnimations()

            if (politician.condemnedBy.contains(Constants.FAKE_USER_EMAIL)) {
                initiateCondemnAnimations()
            } else {
                initiateAbsolveAnimations()
            }
        }
    }

    private fun bindPoliticianDataToViews(politician: Politician) {

        mGlide.load(politician.image)
                .crossFade()
                .placeholder(R.drawable.politician_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPoliticianImageView)

        mNameTextView.text = politician.name
        mVotesNumberTextView.text = politician.votesNumber.toString()
        mEmailTextView.text = politician.email
        mVotesNumberTextView.text = politician.votesNumber.toString()

    }

    fun initiateShowAnimations() {
        val backgroundBlurImage = ImageProcessor.resamplePic(mActivityPresenter, mActivityPresenter.resources, R.drawable.simbolo_justica)
        mBlurry
                .sampling(5)
                .animate(2000)
                .from(backgroundBlurImage)
                .into(mBlurImageView)

        ExpectAnim()
                .expect(mExplanationTextView)
                .toBe(invisible(), outOfScreen(Gravity.BOTTOM))
                .expect(mConstraintLayout)
                .toBe(alpha(1.0f), atItsOriginalPosition())
                .expect(mPoliticianImageView)
                .toBe(alpha(1.0f), atItsOriginalPosition())
                .expect(mDeleteTextImageButton)
                .toBe(alpha(1.0f))

                .toAnimation()
                .setDuration(DEFAULT_ANIM_DURATION)
                .start()

    }

    fun initiateCondemnAnimations() {
        ExpectAnim()
                .expect(mMoldView)
                .toBe(alpha(0.5f))

        changeButtonAnimation()
    }

    fun initiateAbsolveAnimations() {
        ExpectAnim()
                .expect(mMoldView)
                .toBe(alpha(1.0f))

        changeButtonAnimation()
    }

    private fun changeButtonAnimation() {

        if (mAnimatedBadgeImageView.drawable == mPoliticianThiefAnimation && mIsShowingPoliticianDrawable) {
            (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = false

        } else if (mAnimatedBadgeImageView.drawable == mPoliticianThiefAnimation && !mIsShowingPoliticianDrawable) {
            mAnimatedBadgeImageView.setImageDrawable(mThiefPoliticianAnimation)
            (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = true

        } else if (mAnimatedBadgeImageView.drawable == mThiefPoliticianAnimation && mIsShowingPoliticianDrawable) {
            mAnimatedBadgeImageView.setImageDrawable(mPoliticianThiefAnimation)
            (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = false

        } else if (mAnimatedBadgeImageView.drawable == mThiefPoliticianAnimation && !mIsShowingPoliticianDrawable) {
            (mAnimatedBadgeImageView.drawable as AnimatedVectorDrawable).start()
            mIsShowingPoliticianDrawable = true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
    }


}