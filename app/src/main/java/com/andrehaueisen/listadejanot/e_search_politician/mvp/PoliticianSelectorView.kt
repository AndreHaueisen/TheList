package com.andrehaueisen.listadejanot.e_search_politician.mvp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.e_search_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations.*
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.e_activity_politician_selector.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by andre on 5/11/2017.
 */

class PoliticianSelectorView(val mPresenterActivity: PoliticianSelectorPresenterActivity) : PoliticianSelectorMvpContract.View {

    private val LOG_TAG = PoliticianSelectorView::class.java.simpleName
    private val DEFAULT_ANIM_DURATION = 500L
    private val mGlide = Glide.with(mPresenterActivity)
    private val mPoliticianThiefAnimation = mPresenterActivity.getDrawable(R.drawable.politician_thief_animated_vector) as AnimatedVectorDrawable
    private val mThiefPoliticianAnimation = mPresenterActivity.getDrawable(R.drawable.thief_politician_animated_vector) as AnimatedVectorDrawable

    private val mConstraintAnimatorAbsolve = ObjectAnimator().animateBackgroundToColor(mPresenterActivity, R.color.colorAccentDark, R.color.colorPrimaryDark, "backgroundColor")
    private val mMoldViewObjectAnimatorAbsolve = ObjectAnimator().animateBackgroundToColor(mPresenterActivity, R.color.colorAccent, R.color.colorPrimary, "backgroundColor")
    private val mConstraintAnimatorCondemn = ObjectAnimator().animateBackgroundToColor(mPresenterActivity, R.color.colorPrimaryDark, R.color.colorAccentDark, "backgroundColor")
    private val mMoldViewObjectAnimatorCondemn = ObjectAnimator().animateBackgroundToColor(mPresenterActivity, R.color.colorPrimary, R.color.colorAccent, "backgroundColor")

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null
    private var mIsInitialRequest = true
    private var mTempFilePath: String = ""

    init {
        mPresenterActivity.setContentView(R.layout.e_activity_politician_selector)
    }

    override fun setViews(isSavedState: Boolean) {
        setToolbar()
        setViewsInitialState()

        if (isSavedState) {
            setAutoCompleteTextView()
            if (mPresenterActivity.getSinglePolitician() != null) {
                bindPoliticianDataToViews(mPresenterActivity.getSinglePolitician() as Politician)
                notifyPoliticianReady()
            }
        } else {
            beginDatabaseLoadingAlertDialog()
        }

    }

    private fun setToolbar() {
        with(mPresenterActivity) {
            val toolbar = select_politician_toolbar
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    private fun setViewsInitialState() {

        with(mPresenterActivity) {
            ExpectAnim()
                    .expect(delete_text_image_button)
                    .toBe(alpha(0.0f))
                    .expect(constraint_layout)
                    .toBe(outOfScreen(Gravity.BOTTOM))
                    .expect(plus_one_text_view)
                    .toBe(alpha(0.0f))
                    .toAnimation()
                    .setNow()
        }
    }

    private fun setAutoCompleteTextView() {
        with(mPresenterActivity) {
            val nonReliablePoliticiansList = ArrayList<Politician>()
            nonReliablePoliticiansList.addAll(getOriginalSearchablePoliticiansList())

            val adapter = AutoCompletionAdapter(this,
                    R.layout.item_politician_identifier,
                    nonReliablePoliticiansList,
                    getOriginalSearchablePoliticiansList())

            auto_complete_text_view.setAdapter<ArrayAdapter<Politician>>(adapter)
            setOnCompleteTextViewClickListener()
            setOnDeleteTextClickListener()

            dismissAlertDialog()
            ExpectAnim()
                    .expect(select_politician_toolbar)
                    .toBe(centerInParent(false, true))
                    .toAnimation().setDuration(DEFAULT_ANIM_DURATION)
                    .start()
        }
    }

    private fun setOnCompleteTextViewClickListener() {

        with(mPresenterActivity) {
            auto_complete_text_view.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                dismissKeyBoard()

                val politicianName = auto_complete_text_view.text.toString()
                subscribeToSinglePoliticianModel(politicianName)
            }
        }
    }

    fun setOnDeleteTextClickListener() {
        mPresenterActivity.delete_text_image_button.setOnClickListener {

            mPresenterActivity.auto_complete_text_view.text.clear()
            ExpectAnim().fadeOutSingleView(mPresenterActivity.delete_text_image_button)

        }
    }

    private fun beginDatabaseLoadingAlertDialog() {
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mPresenterActivity)
                .setCancelable(false)
                .setIcon(mPresenterActivity.getDrawable(R.drawable.ic_janot_24dp))
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

            if (politician.condemnedBy.contains(mPresenterActivity.getUserEmail()?.encodeEmail())) {
                configureInitialCondemnStatus(politician)
            } else {
                configureInitialAbsolveStatus(politician)
            }
            setVoteButtonClickListener(politician)
        }
    }

    private fun bindPoliticianDataToViews(politician: Politician) {

        val GLIDE_TRANSFORM_RADIUS: Int
        val GLIDE_TRANSFORM_MARGIN: Int

        if (politician.post == Politician.Post.DEPUTADO) {
            GLIDE_TRANSFORM_RADIUS = 2
            GLIDE_TRANSFORM_MARGIN = 2

        } else {
            GLIDE_TRANSFORM_RADIUS = 4
            GLIDE_TRANSFORM_MARGIN = 10
        }

        with(mPresenterActivity) {
            mGlide.load(politician.image)
                    .crossFade()
                    .placeholder(R.drawable.politician_placeholder)
                    .bitmapTransform(RoundedCornersTransformation(this, GLIDE_TRANSFORM_RADIUS, GLIDE_TRANSFORM_MARGIN))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(politician_image_view)
            politician_image_view.contentDescription = getString(R.string.description_politician_image, politician.name)

            post_text_view.text = Politician.getPostText(politician, mPresenterActivity)
            name_text_view.text = politician.name
            missing_votes_text_view.setMissingVotesText(this.resources, politician.votesNumber)
            votes_number_text_view.text = politician.votesNumber.toString()
            email_text_view.text = politician.email
        }
    }

    private fun initiateShowAnimations() {

        with(mPresenterActivity) {
            if (mIsInitialRequest) {
                ExpectAnim()
                        .expect(select_politician_toolbar)
                        .toBe(atItsOriginalPosition())
                        .expect(constraint_layout)
                        .toBe(atItsOriginalPosition())
                        .expect(delete_text_image_button)
                        .toBe(alpha(1.0f))
                        .toAnimation()
                        .setDuration(DEFAULT_ANIM_DURATION)
                        .start()

                mIsInitialRequest = false

            } else {
                ExpectAnim()
                        .expect(delete_text_image_button)
                        .toBe(alpha(1.0f))
                        .toAnimation()
                        .setDuration(DEFAULT_ANIM_DURATION)
                        .start()
            }
        }

    }

    private fun configureInitialCondemnStatus(politician: Politician) {
        with(mPresenterActivity) {

            ExpectAnim()
                    .expect(plus_one_text_view)
                    .toBe(sameCenterAs(votes_number_text_view, true, true))
                    .toAnimation().setNow()

            constraint_layout.setBackgroundColor( ContextCompat.getColor(this, R.color.colorAccentDark) )
            mold_view.setBackgroundColor( ContextCompat.getColor(this, R.color.colorAccent) )

            missing_votes_text_view.setMissingVotesText(this.resources, politician.votesNumber)
            votes_number_text_view.text = politician.votesNumber.toString()
            add_to_vote_count_toggle_button.isChecked = true
            badge_image_view.setImageDrawable(mThiefPoliticianAnimation)
            badge_image_view.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = true)
            badge_image_view.contentDescription = getString(R.string.description_badge_thief_politician)
        }
    }

    private fun configureInitialAbsolveStatus(politician: Politician) {

        with(mPresenterActivity) {
            constraint_layout.setBackgroundColor( ContextCompat.getColor(this, R.color.colorPrimaryDark) )
            mold_view.setBackgroundColor( ContextCompat.getColor(this, R.color.colorPrimary) )

            missing_votes_text_view.setMissingVotesText(this.resources, politician.votesNumber)
            votes_number_text_view.text = politician.votesNumber.toString()
            add_to_vote_count_toggle_button.isChecked = false
            badge_image_view.setImageDrawable(mPoliticianThiefAnimation)
            badge_image_view.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = false)
            badge_image_view.contentDescription = getString(R.string.description_badge_honest_politician)
        }
    }

    fun setVoteButtonClickListener(politician: Politician) {
        with(mPresenterActivity) {
            add_to_vote_count_toggle_button.setOnClickListener {
                if (isConnectedToInternet()) {
                    updatePoliticianVote(politician, this@PoliticianSelectorView)
                } else {
                    showToast(getString(R.string.no_network))
                    add_to_vote_count_toggle_button.isChecked = !add_to_vote_count_toggle_button.isChecked
                }
            }
        }
    }

    override fun initiateCondemnAnimations(politician: Politician) {
        with(mPresenterActivity) {
            mConstraintAnimatorCondemn.target = constraint_layout
            mMoldViewObjectAnimatorCondemn.target = mold_view

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mConstraintAnimatorCondemn, mMoldViewObjectAnimatorCondemn)
            animatorSet.start()

            plus_one_text_view.text = mPresenterActivity.getString(R.string.plus_one)
            badge_image_view.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = true)
            badge_image_view.contentDescription = getString(R.string.description_badge_thief_politician)

            ExpectAnim().plusOneCondemnAnimation(window.decorView.rootView, politician)
        }
    }

    override fun initiateAbsolveAnimations(politician: Politician) {
        with(mPresenterActivity) {
            mConstraintAnimatorAbsolve.target = constraint_layout
            mMoldViewObjectAnimatorAbsolve.target = mold_view

            val animatorSet = AnimatorSet()
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.playTogether(mConstraintAnimatorAbsolve, mMoldViewObjectAnimatorAbsolve)
            animatorSet.start()

            plus_one_text_view.text = mPresenterActivity.getString(R.string.minus_one)
            badge_image_view.animateVectorDrawable(
                    mPoliticianThiefAnimation,
                    mThiefPoliticianAnimation,
                    useInitialToFinalFlow = false)
            badge_image_view.contentDescription = getString(R.string.description_badge_honest_politician)
            ExpectAnim().minusOneAbsolveAnimation(window.decorView.rootView, politician)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?) {
        mPresenterActivity.menuInflater.inflate(R.menu.menu_share_content, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.menu_item_show_vote_list -> {
                mPresenterActivity.showUserVoteListIfLogged()
            }

            R.id.menu_item_share -> {
                //TODO put link to appstore / make a better sharing message
                val politician = mPresenterActivity.getSinglePolitician()
                if (politician != null) {

                    fun getTemporaryFile(): File {

                        val tempFile = File(mPresenterActivity.filesDir, "politicianTempImage")
                        if (!tempFile.exists()) {
                            tempFile.mkdirs()
                        }

                        val tempPic = File(tempFile, "politician_pic.png")
                        var outStream: FileOutputStream? = null

                        try {
                            outStream = FileOutputStream(tempPic)
                            val mBitmap = BitmapFactory.decodeByteArray(politician.image, 0, politician.image.size)
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)

                        } catch (e: Exception) {
                            e.printStackTrace()

                        } finally {
                            try {
                                outStream?.close()

                            } catch (ioe: IOException) {
                                ioe.printStackTrace()
                            }
                        }

                        return tempPic
                    }
                    fun getShareIntent(uri: Uri) = with(Intent()) {

                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, mPresenterActivity.getString(R.string.share_boarding_message, politician.name))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        type = "image/*"

                        this
                    }

                    val tempFile = getTemporaryFile()
                    mTempFilePath = tempFile.path
                    val uri = FileProvider.getUriForFile(mPresenterActivity, "${mPresenterActivity.applicationContext.packageName}.fileprovider", getTemporaryFile())
                    mPresenterActivity.startActivity(Intent.createChooser(getShareIntent(uri),
                            mPresenterActivity.getString(R.string.share_title)))
                }
            }
        }

        return true
    }

    internal fun onDestroy() {
        deleteTemporaryPicture()
    }

    fun deleteTemporaryPicture() = with(File(mTempFilePath)) {
        if (delete()) {
            Log.i(LOG_TAG, "Temporary picture file deleted")
        } else {
            Log.i(LOG_TAG, "Temporary picture file not deleted")
        }
    }
}