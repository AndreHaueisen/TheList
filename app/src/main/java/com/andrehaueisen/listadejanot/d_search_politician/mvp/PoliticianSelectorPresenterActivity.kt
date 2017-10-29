package com.andrehaueisen.listadejanot.d_search_politician.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.d_search_politician.dagger.DaggerPoliticianSelectorComponent
import com.andrehaueisen.listadejanot.d_search_politician.dagger.ImageFetcherModule
import com.andrehaueisen.listadejanot.d_search_politician.dagger.PoliticianSelectorModule
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.images.ImageFetcherModel
import com.andrehaueisen.listadejanot.models.Item
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.d_activity_politician_selector.*
import javax.inject.Inject

class PoliticianSelectorPresenterActivity : AppCompatActivity(), PoliticianSelectorMvpContract.Presenter {

    private val ACTIVITY_REQUEST_CODE = 1
    private val LOG_TAG = PoliticianSelectorPresenterActivity::class.java.simpleName

    @Inject
    lateinit var mImageFetcherModel: ImageFetcherModel
    @Inject
    lateinit var mSelectorModel: PoliticianSelectorModel
    @Inject
    lateinit var mSinglePoliticianModel: SinglePoliticianModel
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    @Inject
    lateinit var mUser : User

    private var mIsActivityVisible = true
    private var mView: PoliticianSelectorView? = null
    private var mPolitician: Politician? = null
    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mLastSelectedPoliticianName: String

    private var mNameFromNotification: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(INTENT_POLITICIAN_NAME)) {
            mNameFromNotification = intent.getStringExtra(INTENT_POLITICIAN_NAME)
        }

        DaggerPoliticianSelectorComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .politicianSelectorModule(PoliticianSelectorModule(supportLoaderManager))
                .imageFetcherModule(ImageFetcherModule())
                .build()
                .inject(this)


        if (savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = false)
            refreshPoliticianData()

        } else {
            mSelectorModel.setSearchablePoliticiansList( savedInstanceState.getParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS ))
            if (savedInstanceState.containsKey(BUNDLE_POLITICIAN)) {
                mPolitician = savedInstanceState.getParcelable(BUNDLE_POLITICIAN)
            }

            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = true)
        }
    }

    override fun onStart() {
        super.onStart()
        mIsActivityVisible = true
        mView?.initiateBackgroundAnimations()
    }

    private fun refreshPoliticianData() {

        mView?.requestSearchableListUpdate()

        val politicianName = auto_complete_text_view.text.toString()
        if (mNameFromNotification != null || politicianName.isNotEmpty()) {
            if(mNameFromNotification != null) {
                mView?.performOnCompleteTextViewAutoSearch(mNameFromNotification as String)
            }else{
                mView?.performOnCompleteTextViewAutoSearch(politicianName)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {

        intent?.let {
            if (intent.hasExtra(INTENT_POLITICIAN_NAME)) {
                mNameFromNotification = intent.getStringExtra(INTENT_POLITICIAN_NAME)
                refreshPoliticianData()
            }
        }

        super.onNewIntent(intent)
    }

    fun subscribeToSinglePolitician() {

        mSinglePoliticianModel.loadSinglePoliticianPublisher()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({politician ->
                    mPolitician = politician
                    mView?.notifyPoliticianReady()})

    }

    fun subscribeToImageFetcher(politician: Politician?){
        politician?.let {
            mImageFetcherModel.getPoliticianImages(politician.name, politician.post!!)
                    .firstElement()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mImagesObserver)
        }
    }

    fun initiateSinglePoliticianLoad(politicianName: String){
        mLastSelectedPoliticianName = politicianName
        mSinglePoliticianModel.initiateSinglePoliticianLoad(mLastSelectedPoliticianName)
    }

    private val mImagesObserver = object: MaybeObserver<Item>{
        override fun onSuccess(imageItem: Item) {
            mView?.notifyImageReady(imageItem)
        }

        override fun onComplete() {}

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(error: Throwable) {}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS, mSelectorModel.getSearchablePoliticiansList())
        if (mPolitician != null) {
            outState.putParcelable(BUNDLE_POLITICIAN, mPolitician)
        }
        super.onSaveInstanceState(outState)
    }

    fun getUser() = mUser

    fun updateLists(action: ListAction, politician: Politician) = if(mFirebaseAuthenticator.isUserLoggedIn()){
        mSinglePoliticianModel.updateLists(action, politician)
    } else {
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    fun updateGrade(voteType: RatingBarType, outdatedGrade: Float, newGrade: Float, politician: Politician) = if(mFirebaseAuthenticator.isUserLoggedIn()){
        mSinglePoliticianModel.updateGrade(voteType, outdatedGrade, newGrade, politician, getUser())
    } else {
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    override fun showUserVoteListIfLogged() = if (mFirebaseAuthenticator.isUserLoggedIn()) {
        val intent = Intent(this, UserVoteListPresenterActivity::class.java)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)

    } else {
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshPoliticianData()
        }
    }

    fun getSearchablePoliticiansList() = mSelectorModel.getSearchablePoliticiansList()

    fun getSinglePolitician() = mPolitician

    fun isVisible() = mIsActivityVisible

    override fun onStop() {
        mIsActivityVisible = false
        super.onStop()
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSinglePoliticianModel.onDestroy()
        mView?.onDestroy()
        mView = null
        super.onDestroy()
    }
}
