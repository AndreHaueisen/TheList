package com.andrehaueisen.listadejanot.d_search_politician.mvp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RatingBar
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_search_politician.AutoCompletionAdapter
import com.andrehaueisen.listadejanot.h_opinions.OpinionsActivity
import com.andrehaueisen.listadejanot.models.Item
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations.alpha
import kotlinx.android.synthetic.main.d_activity_politician_selector.*
import kotlinx.android.synthetic.main.group_layout_buttons.*
import kotlinx.android.synthetic.main.group_layout_grades.*
import kotlinx.android.synthetic.main.group_layout_rating_bars.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by andre on 5/11/2017.
 */

class PoliticianSelectorView(private val mPresenterActivity: PoliticianSelectorPresenterActivity) : PoliticianSelectorMvpContract.View {

    private val LOG_TAG = PoliticianSelectorView::class.java.simpleName
    private val DEFAULT_ANIM_DURATION = 500L
    private val mGlide = Glide.with(mPresenterActivity)

    private val mPostTextAnimatorAbsolve = ObjectAnimator().animatePropertyToColor(mPresenterActivity, R.color.colorAccent, R.color.colorPrimary, "textColor")
    private val mPostTextAnimatorCondemn = ObjectAnimator().animatePropertyToColor(mPresenterActivity, R.color.colorPrimary, R.color.colorAccent, "textColor")
    private val mNameTextAnimatorAbsolve = ObjectAnimator().animatePropertyToColor(mPresenterActivity, R.color.colorAccent, R.color.colorPrimary, "textColor")
    private val mNameTextAnimatorCondemn = ObjectAnimator().animatePropertyToColor(mPresenterActivity, R.color.colorPrimary, R.color.colorAccent, "textColor")

    private var mLoadingDatabaseAlertDialog: AlertDialog? = null
    private var mIsInitialRequest = true
    private var mTempFilePath: String = ""
    private var mLastButtonPosition = 0

    private var mCurrentPoliticianPicture: Drawable? = null

    init {
        mPresenterActivity.setContentView(R.layout.d_activity_politician_selector)
    }

    override fun setViews(isSavedState: Boolean) {
        setVoteListButton()
        setViewsInitialState()

        if (isSavedState) {
            setAutoCompleteTextView()
            if (mPresenterActivity.getSinglePolitician() != null) {
                bindPoliticianDataToViews(mPresenterActivity.getSinglePolitician() as Politician)
                notifyPoliticianReady()
            }
        } else {
            with(mPresenterActivity) {
                if (isConnectedToInternet()) {
                    beginDatabaseLoadingAlertDialog()
                } else {
                    constraint_layout.showSnackbar(getString(R.string.no_network))
                }
            }
        }

    }

    private fun setVoteListButton() {
        with(mPresenterActivity) {
            vote_list_image_button.setOnClickListener { mPresenterActivity.showUserVoteListIfLogged() }
        }
    }

    private fun setViewsInitialState() = with(mPresenterActivity) {
        ExpectAnim()
                .expect(delete_text_image_button)
                .toBe(alpha(0.0f))
                .toAnimation()
                .setNow()
    }

    private fun setAutoCompleteTextView() = with(mPresenterActivity) {
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
    }

    private fun setRatingBarsClickListeners(politician: Politician) = with(mPresenterActivity) {
        val user = getUser()
        val politicianEncodedEmail = politician.email?.encodeEmail()

        honesty_rating_bar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, newGrade, changedByUser ->
            val outdatedUserGrade = user.honestyGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE

            if (changedByUser) {
                with(politician) {

                    mPresenterActivity.updateGrade(RatingBarType.HONESTY, outdatedUserGrade, newGrade, this)
                    val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                    val isNotFirstGrade = honestyGrade != UNEXISTING_GRADE_VALUE

                    honestyGrade = if (isNotFirstGrade) {
                        if (containsUserPastGrade) {
                            ((honestyGrade * honestyCount) - outdatedUserGrade + newGrade) / honestyCount
                        } else {
                            honestyCount++
                            ((honestyGrade * (honestyCount - 1)) + newGrade) / honestyCount
                        }
                    } else {
                        honestyCount++
                        newGrade
                    }

                    honesty_grade_text_view.text = String.format("%.1f", honestyGrade)
                    your_grade_honesty_text_view.text = getString(R.string.your_grade, newGrade)
                    ExpectAnim().scaleRatingBarUpAndDown(ratingBar, rating_bars_view_flipper, mPresenterActivity)
                }
            }
        }

        leader_rating_bar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, newGrade, changedByUser ->
            val outdatedUserGrade = user.leaderGrades[politician.email?.encodeEmail()] ?: UNEXISTING_GRADE_VALUE

            if (changedByUser) {
                with(politician) {

                    mPresenterActivity.updateGrade(RatingBarType.LEADER, outdatedUserGrade, newGrade, this)
                    val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                    val isNotFirstGrade = leaderGrade != UNEXISTING_GRADE_VALUE

                    leaderGrade = if (isNotFirstGrade) {
                        if (containsUserPastGrade) {
                            ((leaderGrade * leaderCount) - outdatedUserGrade + newGrade) / leaderCount
                        } else {
                            leaderCount++
                            ((leaderGrade * (leaderCount - 1)) + newGrade) / leaderCount
                        }
                    } else {
                        leaderCount++
                        newGrade
                    }

                    leader_grade_text_view.text = String.format("%.1f", leaderGrade)
                    your_grade_leader_text_view.text = getString(R.string.your_grade, newGrade)
                    ExpectAnim().scaleRatingBarUpAndDown(ratingBar, rating_bars_view_flipper, mPresenterActivity)
                }
            }
        }

        promise_keeper_rating_bar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, newGrade, changedByUser ->
            val outdatedUserGrade = user.promiseKeeperGrades[politician.email?.encodeEmail()] ?: UNEXISTING_GRADE_VALUE

            if (changedByUser) {
                with(politician) {

                    mPresenterActivity.updateGrade(RatingBarType.PROMISE_KEEPER, outdatedUserGrade, newGrade, this)
                    val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                    val isNotFirstGrade = promiseKeeperGrade != UNEXISTING_GRADE_VALUE

                    promiseKeeperGrade = if (isNotFirstGrade) {
                        if (containsUserPastGrade) {
                            ((promiseKeeperGrade * promiseKeeperCount) - outdatedUserGrade + newGrade) / promiseKeeperCount
                        } else {
                            promiseKeeperCount++
                            ((promiseKeeperGrade * (promiseKeeperCount - 1)) + newGrade) / promiseKeeperCount
                        }
                    } else {
                        promiseKeeperCount++
                        newGrade
                    }

                    promise_keeper_grade_text_view.text = String.format("%.1f", promiseKeeperGrade)
                    your_grade_promise_keeper_text_view.text = getString(R.string.your_grade, newGrade)

                    ExpectAnim().scaleRatingBarUpAndDown(ratingBar, rating_bars_view_flipper, mPresenterActivity)
                }
            }
        }

        rules_for_the_people_rating_bar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, newGrade, changedByUser ->
            val outdatedUserGrade = user.rulesForThePeopleGrades[politician.email?.encodeEmail()] ?: UNEXISTING_GRADE_VALUE

            if (changedByUser) {
                with(politician) {

                    mPresenterActivity.updateGrade(RatingBarType.RULES_FOR_PEOPLE, outdatedUserGrade, newGrade, this)
                    val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                    val isNotFirstGrade = rulesForThePeopleGrade != UNEXISTING_GRADE_VALUE

                    rulesForThePeopleGrade = if (isNotFirstGrade) {
                        if (containsUserPastGrade) {
                            ((rulesForThePeopleGrade * rulesForThePeopleCount) - outdatedUserGrade + newGrade) / rulesForThePeopleCount
                        } else {
                            rulesForThePeopleCount++
                            ((rulesForThePeopleGrade * (rulesForThePeopleCount - 1)) + newGrade) / rulesForThePeopleCount
                        }
                    } else {
                        rulesForThePeopleCount++
                        newGrade
                    }

                    rules_for_the_people_grade_text_view.text = String.format("%.1f", rulesForThePeopleGrade)
                    your_grade_rules_for_people_text_view.text = getString(R.string.your_grade, newGrade)

                    ExpectAnim().scaleRatingBarUpAndDown(ratingBar, rating_bars_view_flipper, mPresenterActivity)
                }
            }
        }

        answer_voters_rating_bar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, newGrade, changedByUser ->
            val outdatedUserGrade = user.answerVotersGrades[politician.email?.encodeEmail()] ?: UNEXISTING_GRADE_VALUE

            if (changedByUser) {
                with(politician) {

                    mPresenterActivity.updateGrade(RatingBarType.ANSWER_VOTERS, outdatedUserGrade, newGrade, this)
                    val containsUserPastGrade = outdatedUserGrade != UNEXISTING_GRADE_VALUE
                    val isNotFirstGrade = answerVotersGrade != UNEXISTING_GRADE_VALUE

                    answerVotersGrade = if (isNotFirstGrade) {
                        if (containsUserPastGrade) {
                            ((answerVotersGrade * answerVotersCount) - outdatedUserGrade + newGrade) / answerVotersCount
                        } else {
                            answerVotersCount++
                            ((answerVotersGrade * (answerVotersCount - 1)) + newGrade) / answerVotersCount
                        }
                    } else {
                        answerVotersCount++
                        newGrade
                    }

                    answer_voters_grade_text_view.text = String.format("%.1f", answerVotersGrade)
                    your_grade_answer_voters_text_view.text = getString(R.string.your_grade, newGrade)

                    ExpectAnim().scaleRatingBarUpAndDown(ratingBar, rating_bars_view_flipper, mPresenterActivity)
                }
            }
        }
    }

    private fun setOnCompleteTextViewClickListener() = with(mPresenterActivity) {
        auto_complete_text_view.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            dismissKeyBoard()

            val politicianName = auto_complete_text_view.text.toString()
            subscribeToSinglePolitician(politicianName)
        }
    }

    fun performOnCompleteTextViewAutoSearch(politicianName: String) {
        mPresenterActivity.auto_complete_text_view.setText(politicianName)
        dismissKeyBoard()
        mPresenterActivity.subscribeToSinglePolitician(politicianName)
    }

    private fun setOnDeleteTextClickListener() =
            mPresenterActivity.delete_text_image_button.setOnClickListener {

                mPresenterActivity.auto_complete_text_view.text.clear()
                ExpectAnim().fadeOutSingleView(mPresenterActivity.delete_text_image_button)

            }

    private fun beginDatabaseLoadingAlertDialog() {
        mLoadingDatabaseAlertDialog = AlertDialog.Builder(mPresenterActivity)
                .setCancelable(true)
                .setIcon(mPresenterActivity.getDrawable(R.drawable.ic_launcher))
                .setTitle(mPresenterActivity.getString(R.string.dialog_title_loading_database))
                .setMessage("")
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
        val manager = mPresenterActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(mPresenterActivity.auto_complete_text_view.windowToken, 0)
    }

    override fun notifySearchablePoliticiansNewList() = setAutoCompleteTextView()

    override fun notifyPoliticianReady() {

        with(mPresenterActivity) {
            val politician = getSinglePolitician()
            subscribeToImageFetcher(politician)

            if (politician != null) {
                bindPoliticianDataToViews(politician)
                initiateShowAnimations()

                setButtonsClickListener(politician)
                setRatingBarsClickListeners(politician)
                setShareButtonClickListener(politician)
                setSearchOnWebButtonClickListener(politician)
                setEmailButtonClickListener(politician)
                setViewFlipperClickListener()
            }
        }
    }

    fun notifyImageReady(imageItem: Item) {
        val imageUrl = imageItem.link
        val height = imageItem.image?.height!!
        val width = imageItem.image?.width!!

        val requestOption: RequestOptions
        val transitionOptions = DrawableTransitionOptions().crossFade()
        if (height > width) {
            requestOption = RequestOptions
                    .encodeFormatOf(Bitmap.CompressFormat.JPEG)
                    .encodeQuality(20)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)

        } else {
            requestOption = RequestOptions
                    .encodeFormatOf(Bitmap.CompressFormat.JPEG)
                    .encodeQuality(30)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
        }

        with(mPresenterActivity) {
            search_politician_image_view.visibility = View.VISIBLE
            mGlide.load(imageUrl)
                    .apply(requestOption)
                    .transition(transitionOptions)
                    .listener(object: RequestListener<Drawable>{
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            mCurrentPoliticianPicture = resource
                            return false
                        }

                        override fun onLoadFailed(error: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Log.e(LOG_TAG, error?.message)
                            return false
                        }
                    })
                    .into(search_politician_image_view)

            search_politician_image_view.contentDescription = getString(R.string.description_politician_image, getSinglePolitician()?.name)
        }
    }

    private fun bindPoliticianDataToViews(politician: Politician) {

        with(mPresenterActivity) {
            val user = getUser()

            post_text_view.text = politician.post?.name
            name_text_view.text = politician.name

            val politicianEncodedEmail = politician.email?.encodeEmail()

            val honestyGrade = user.honestyGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE + 1
            val leaderGrade = user.leaderGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE + 1
            val promiseKeeperGrade = user.promiseKeeperGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE + 1
            val rulesForThePeopleGrade = user.rulesForThePeopleGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE + 1
            val answer_voters_grade = user.answerVotersGrades[politicianEncodedEmail] ?: UNEXISTING_GRADE_VALUE + 1

            honesty_rating_bar.rating = honestyGrade
            leader_rating_bar.rating = leaderGrade
            promise_keeper_rating_bar.rating = promiseKeeperGrade
            rules_for_the_people_rating_bar.rating = rulesForThePeopleGrade
            answer_voters_rating_bar.rating = answer_voters_grade

            your_grade_honesty_text_view.text = getString(R.string.your_grade, honestyGrade)
            your_grade_leader_text_view.text = getString(R.string.your_grade, leaderGrade)
            your_grade_promise_keeper_text_view.text = getString(R.string.your_grade, promiseKeeperGrade)
            your_grade_rules_for_people_text_view.text = getString(R.string.your_grade, rulesForThePeopleGrade)
            your_grade_answer_voters_text_view.text = getString(R.string.your_grade, answer_voters_grade)

            honesty_grade_text_view.text = String.format("%.1f", politician.honestyGrade)
            leader_grade_text_view.text = String.format("%.1f", politician.leaderGrade)
            promise_keeper_grade_text_view.text = String.format("%.1f", politician.promiseKeeperGrade)
            rules_for_the_people_grade_text_view.text = String.format("%.1f", politician.rulesForThePeopleGrade)
            answer_voters_grade_text_view.text = String.format("%.1f", politician.answerVotersGrade)

            val hasRecommendationVote = user.recommendations.containsKey(politicianEncodedEmail)
            val hasCondemnationVote = user.condemnations.containsKey(politicianEncodedEmail)

            when {
                hasRecommendationVote -> {
                    vote_radio_group.position = 1
                    mLastButtonPosition = 1
                }
                hasCondemnationVote -> {
                    vote_radio_group.position = 0
                    mLastButtonPosition = 0
                }
                else -> {
                    vote_radio_group.position = -1
                    mLastButtonPosition = -1
                }
            }
        }
    }

    private fun initiateShowAnimations() {

        with(mPresenterActivity) {
            if (mIsInitialRequest) {

                politician_info_group.visibility = View.VISIBLE
                initial_screen_group.visibility = View.GONE

                ExpectAnim()
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

    private var initialX: Float = 0F

    private fun setViewFlipperClickListener() {
        with(mPresenterActivity) {

            rating_bars_view_flipper.setOnTouchListener({ _, motionEvent ->
                when (motionEvent.action) {

                    MotionEvent.ACTION_DOWN -> {
                        initialX = motionEvent.x
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val finalX = motionEvent.x

                        if (finalX > initialX) {
                            rating_bars_view_flipper.setInAnimation(this, android.R.anim.slide_in_left)
                            rating_bars_view_flipper.setOutAnimation(this, android.R.anim.slide_out_right)
                            rating_bars_view_flipper.showPrevious()

                        } else if (finalX < initialX) {
                            rating_bars_view_flipper.setInAnimation(this, R.anim.slide_in_right)
                            rating_bars_view_flipper.setOutAnimation(this, R.anim.slide_out_left)
                            rating_bars_view_flipper.showNext()
                        }
                        true
                    }
                    else -> false
                }

            })
        }
    }

    private fun setButtonsClickListener(politician: Politician) =
            with(mPresenterActivity) {

                vote_radio_group.setOnClickedButtonListener { _, position ->

                    val listAction: ListAction

                    if (position != mLastButtonPosition) {
                        if (isConnectedToInternet()) {

                            listAction = when (position) {
                                0 -> { politician_search_coordinator_layout.showSnackbar(getString(R.string.politician_added_to_suspect_list, politician.name), Snackbar.LENGTH_SHORT)
                                    ListAction.ADD_TO_SUSPECT_LIST }

                                1 -> { politician_search_coordinator_layout.showSnackbar(getString(R.string.politician_added_to_voting_list, politician.name), Snackbar.LENGTH_SHORT)
                                    ListAction.ADD_TO_VOTE_LIST }

                                -1 -> { politician_search_coordinator_layout.showSnackbar(getString(R.string.politician_removed_from_list, politician.name), Snackbar.LENGTH_SHORT)
                                    ListAction.REMOVE_FROM_LISTS }

                                else -> ListAction.REMOVE_FROM_LISTS
                            }
                            updateLists(listAction, politician)
                            mLastButtonPosition = position

                        } else {
                            showToast(getString(R.string.no_network))
                            vote_radio_group.position = mLastButtonPosition
                        }
                    }
                }

                opinions_button.setOnClickListener {

                    val extras = Bundle()
                    extras.putString(BUNDLE_POLITICIAN_EMAIL, politician.email)
                    extras.putString(BUNDLE_POLITICIAN_NAME, politician.name)
                    if (mFirebaseAuthenticator.getUserEmail() != null) {
                        extras.putString(BUNDLE_USER_EMAIL, mFirebaseAuthenticator.getUserEmail())
                    }

                    val namePair = android.support.v4.util.Pair<View, String>(name_text_view as View, getString(R.string.transition_name))
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, namePair)

                    startNewActivity(OpinionsActivity::class.java, null, extras, options.toBundle())
                }
            }

    private fun setShareButtonClickListener(politician: Politician?) =
            mPresenterActivity.share_button.setOnClickListener {
                //TODO put link to playstore

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
                            val mBitmap = drawableToBitmap(mPresenterActivity.search_politician_image_view.drawable)
                            mBitmap?.compress(Bitmap.CompressFormat.PNG, 100, outStream)

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

                        val post: String
                        val determiner: String

                        when (politician.post) {
                            Politician.Post.DEPUTADO -> {
                                post = mPresenterActivity.getString(R.string.congressman)
                                determiner = mPresenterActivity.getString(R.string.determiner_male)
                            }

                            Politician.Post.DEPUTADA -> {
                                post = mPresenterActivity.getString(R.string.congresswoman)
                                determiner = mPresenterActivity.getString(R.string.determiner_female)
                            }

                            Politician.Post.SENADOR -> {
                                post = mPresenterActivity.getString(R.string.senator)
                                determiner = mPresenterActivity.getString(R.string.determiner_male)
                            }

                            Politician.Post.SENADORA -> {
                                post = mPresenterActivity.getString(R.string.senatora)
                                determiner = mPresenterActivity.getString(R.string.determiner_female)
                            }

                            Politician.Post.GOVERNADOR -> {
                                post = mPresenterActivity.getString(R.string.governor)
                                determiner = mPresenterActivity.getString(R.string.determiner_male)
                            }

                            Politician.Post.GOVERNADORA -> {
                                post = mPresenterActivity.getString(R.string.governora)
                                determiner = mPresenterActivity.getString(R.string.determiner_female)
                            }

                            else -> {
                                post = "PLACE HOLDER"
                                determiner = mPresenterActivity.getString(R.string.determiner_male)
                            }
                        }

                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, mPresenterActivity.getString(R.string.share_boarding_message, determiner, post, politician.name, PLAY_STORE_LINK))
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

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? =
            drawable?.let {
                val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

    private fun setSearchOnWebButtonClickListener(politician: Politician) =
            with(mPresenterActivity) {
                search_on_web_button.setOnClickListener {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, getString(R.string.name_plus_corruption, politician.name))

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)

                    } else {
                        val alertDialog = AlertDialog.Builder(mPresenterActivity)
                                .createNeutralDialog(getString(R.string.dialog_title_no_app_detected), getString(R.string.dialog_message_no_browser_app))
                        alertDialog.show()
                    }
                }
            }

    private fun setEmailButtonClickListener(politician: Politician) = mPresenterActivity.email_button.setOnClickListener {
        with(mPresenterActivity) {
            val intent = Intent(Intent.ACTION_SENDTO)

            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(politician.email))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, getString(R.string.intent_email_chooser)))

            } else {
                val alertDialog = AlertDialog.Builder(mPresenterActivity)
                        .createNeutralDialog(getString(R.string.dialog_title_no_app_detected), getString(R.string.dialog_message_no_email_app))
                alertDialog.show()
            }
        }
    }

     fun initiateCondemnAnimations(politician: Politician) = with(mPresenterActivity) {
        mPostTextAnimatorCondemn.target = name_text_view
        mNameTextAnimatorCondemn.target = post_text_view

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(
                mPostTextAnimatorCondemn,
                mNameTextAnimatorCondemn)

        animatorSet.start()

    }

    internal fun onDestroy() {
        deleteTemporaryPicture()
    }

    private fun deleteTemporaryPicture() = with(File(mTempFilePath)) {
        if (delete()) {
            Log.i(LOG_TAG, "Temporary picture file deleted")
        } else {
            Log.i(LOG_TAG, "Temporary picture file not deleted")
        }
    }
}