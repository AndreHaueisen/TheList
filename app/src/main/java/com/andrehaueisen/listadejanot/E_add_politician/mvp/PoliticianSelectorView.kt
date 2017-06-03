package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
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
    private val mPoliticianThiefAnimation: AnimatedVectorDrawable
    private val mThiefPoliticianAnimation: AnimatedVectorDrawable

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null

    private var mIsInitialRequest = true
    private var mIsShowingPoliticianDrawable = true

    private var mTempFilePath: String = ""

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
        ExpectAnim()
                .expect(mPresenterActivity.select_politician_toolbar)
                .toBe(centerInParent(false, true))
                .toAnimation().setDuration(DEFAULT_ANIM_DURATION)
                .start()
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

        val GLIDE_TRANSFORM_RADIUS: Int
        val GLIDE_TRANSFORM_MARGIN: Int

        if (politician.post == Politician.Post.DEPUTADO) {
            GLIDE_TRANSFORM_RADIUS = 2
            GLIDE_TRANSFORM_MARGIN = 2

        } else {
            GLIDE_TRANSFORM_RADIUS = 4
            GLIDE_TRANSFORM_MARGIN = 10
        }

        mGlide.load(politician.image)
                .crossFade()
                .placeholder(R.drawable.politician_placeholder)
                .bitmapTransform(RoundedCornersTransformation(mPresenterActivity, GLIDE_TRANSFORM_RADIUS, GLIDE_TRANSFORM_MARGIN))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPresenterActivity.politician_image_view)

        val votesMissingToThreshold = VOTES_TO_MAIN_LIST_THRESHOLD - politician.votesNumber

        mPresenterActivity.post_text_view.text = politician.post.name
        mPresenterActivity.name_text_view.text = politician.name
        mPresenterActivity.missing_votes_text_view.text = mPresenterActivity.getString(R.string.missing_votes_to_threshold, votesMissingToThreshold)
        mPresenterActivity.votes_number_text_view.text = politician.votesNumber.toString()
        mPresenterActivity.email_text_view.text = politician.email
    }

    private fun initiateShowAnimations() {

        if (mIsInitialRequest) {
            ExpectAnim()
                    .expect(mPresenterActivity.select_politician_toolbar)
                    .toBe(atItsOriginalPosition())
                    .expect(mPresenterActivity.constraint_layout)
                    .toBe(alpha(1.0f), atItsOriginalPosition())
                    .expect(mPresenterActivity.delete_text_image_button)
                    .toBe(alpha(1.0f))
                    .toAnimation()
                    .setDuration(DEFAULT_ANIM_DURATION)
                    .start()

            mIsInitialRequest = false

        } else {
            ExpectAnim()
                    .expect(mPresenterActivity.delete_text_image_button)
                    .toBe(alpha(1.0f))
                    .toAnimation()
                    .setDuration(DEFAULT_ANIM_DURATION)
                    .start()
        }

    }

    private fun configureInitialCondemnStatus(politician: Politician) {
        ExpectAnim()
                .expect(mPresenterActivity.plus_one_text_view)
                .toBe(sameCenterAs(mPresenterActivity.votes_number_text_view, true, true))
                .toAnimation().setNow()

        val votesMissingToThreshold = VOTES_TO_MAIN_LIST_THRESHOLD - politician.votesNumber
        mPresenterActivity.missing_votes_text_view.text = mPresenterActivity.getString(R.string.missing_votes_to_threshold, votesMissingToThreshold)
        mPresenterActivity.votes_number_text_view.text = politician.votesNumber.toString()
        mPresenterActivity.add_to_vote_count_toggle_button.isChecked = true
        mPresenterActivity.badge_image_view.setImageDrawable(mThiefPoliticianAnimation)

        mIsShowingPoliticianDrawable = true
        changeBadgeStatus()
    }

    private fun configureInitialAbsolveStatus(politician: Politician) {

        val votesMissingToThreshold = VOTES_TO_MAIN_LIST_THRESHOLD - politician.votesNumber
        mPresenterActivity.missing_votes_text_view.text = mPresenterActivity.getString(R.string.missing_votes_to_threshold, votesMissingToThreshold)
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
        mPresenterActivity.menuInflater.inflate(R.menu.menu_share_content, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.menu_item_share -> {
                //TODO put link to appstore / make a better sharing message
                val politician = mPresenterActivity.getSinglePolitician()
                if (politician != null) {

                    fun getTemporaryFile(): File {

                        val tempFile = File (Environment.getExternalStorageDirectory(), "/tempPoliticiansPic/")
                        if (!tempFile.exists()) {
                            tempFile.mkdirs()
                        }

                        val tempPic = File(tempFile, "politician_pic.png")
                        var outStream : FileOutputStream? = null

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

                    fun getShareIntent(uri: Uri): Intent {
                        val shareIntent = Intent()

                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, mPresenterActivity.getString(R.string.share_boarding_message, politician.name))
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        shareIntent.type = "image/*"

                        return shareIntent
                    }

                    val tempFile = getTemporaryFile()
                    mTempFilePath = tempFile.path
                    val uri = Uri.fromFile(tempFile)
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

    fun deleteTemporaryPicture() {
        if (File(mTempFilePath).delete()) {
            Log.i(LOG_TAG, "Temporary picture file deleted")
        } else {
            Log.i(LOG_TAG, "Temporary picture file not deleted")
        }
    }
}