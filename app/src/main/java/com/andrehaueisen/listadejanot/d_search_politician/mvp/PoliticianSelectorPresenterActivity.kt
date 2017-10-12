package com.andrehaueisen.listadejanot.d_search_politician.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.d_search_politician.dagger.DaggerPoliticianSelectorComponent
import com.andrehaueisen.listadejanot.d_search_politician.dagger.ImageFetcherModule
import com.andrehaueisen.listadejanot.d_search_politician.dagger.PoliticianSelectorModule
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.models.Item
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.google.firebase.database.ValueEventListener
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
    lateinit var mFirebaseRepository: FirebaseRepository
    @Inject
    lateinit var mUserValueEventListener: ValueEventListener

    private val mUser = User()

    private var mView: PoliticianSelectorView? = null
    private var mPolitician: Politician? = null
    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mOriginalSearchablePoliticiansList: ArrayList<Politician>
    lateinit private var mLastSelectedPoliticianName: String

    private var mNameFromNotification: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val senadoresMainList = ArrayList<Politician>()
        val deputadosMainList = ArrayList<Politician>()
        val governadoresMainList = ArrayList<Politician>()

        if (intent.hasExtra(INTENT_SENADORES_MAIN_LIST)) {
            senadoresMainList.addAll(intent.getParcelableArrayListExtra(INTENT_SENADORES_MAIN_LIST))
        }

        if (intent.hasExtra(INTENT_DEPUTADOS_MAIN_LIST)) {
            deputadosMainList.addAll(intent.getParcelableArrayListExtra(INTENT_DEPUTADOS_MAIN_LIST))
        }

        if (intent.hasExtra(INTENT_GOVERNADORES_MAIN_LIST)){
            governadoresMainList.addAll(intent.getParcelableArrayListExtra(INTENT_GOVERNADORES_MAIN_LIST))
        }

        if (intent.hasExtra(INTENT_POLITICIAN_NAME)) {
            mNameFromNotification = intent.getStringExtra(INTENT_POLITICIAN_NAME)
        }

        DaggerPoliticianSelectorComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .politicianSelectorModule(PoliticianSelectorModule(supportLoaderManager, mUser))
                .imageFetcherModule(ImageFetcherModule())
                .build()
                .inject(this)

        mFirebaseRepository.listenToUser(mUserValueEventListener, getUserEmail())

        if (savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = false)
            refreshPoliticianData()

        } else {
            mOriginalSearchablePoliticiansList = savedInstanceState.getParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS)
            mSelectorModel.setSearchablePoliticiansList(mOriginalSearchablePoliticiansList)
            if (savedInstanceState.containsKey(BUNDLE_POLITICIAN)) {
                mPolitician = savedInstanceState.getParcelable(BUNDLE_POLITICIAN)
            }

            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = true)
        }
    }

    private fun refreshPoliticianData() {
        mSelectorModel.connectToFirebase()
        subscribeToPoliticianSelectorModel()
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

    override fun subscribeToPoliticianSelectorModel() = mSelectorModel.loadSearchablePoliticiansList()
            .firstElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mMaybeListObserver)

    private val mMaybeListObserver = object : MaybeObserver<ArrayList<Politician>> {

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
            mOriginalSearchablePoliticiansList = searchablePoliticiansList
            mView?.notifySearchablePoliticiansNewList()

            val politicianName = auto_complete_text_view.text.toString()
            if (mNameFromNotification != null || politicianName.isNotEmpty()) {
                if(mNameFromNotification != null) {
                    mView?.performOnCompleteTextViewAutoSearch(mNameFromNotification as String)
                }else{
                    mView?.performOnCompleteTextViewAutoSearch(politicianName)
                }
            }

        }

        override fun onComplete() = Unit

        override fun onError(error: Throwable) {
            Log.e(LOG_TAG, error.message)
        }
    }

    fun subscribeToSinglePolitician(politicianName: String) {
        mLastSelectedPoliticianName = politicianName
        mSinglePoliticianModel.loadSinglePoliticianPublisher()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSinglePoliticianObserver)


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

    private val mSinglePoliticianObserver = object : MaybeObserver<Politician> {
        override fun onError(t: Throwable) {
            Log.e(LOG_TAG, t.toString())
        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
            mSinglePoliticianModel.initiateSinglePoliticianLoad(mLastSelectedPoliticianName)
        }

        override fun onSuccess(politician: Politician) {
            mPolitician = politician
            mView?.notifyPoliticianReady()
        }

        override fun onComplete() = Unit
    }

    private val mImagesObserver = object: MaybeObserver<Item>{
        override fun onSuccess(imageItem: Item) {
            mView?.notifyImageReady(imageItem)
        }

        override fun onComplete() {

        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(error: Throwable) {}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS, mOriginalSearchablePoliticiansList)
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
        mSinglePoliticianModel.updateGrade(voteType, outdatedGrade, newGrade, politician, mUser)
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

    fun getOriginalSearchablePoliticiansList() = mOriginalSearchablePoliticiansList

    fun getSinglePolitician() = mPolitician

    fun getUserEmail() = mFirebaseAuthenticator.getUserEmail()

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSelectorModel.onDestroy()
        mSinglePoliticianModel.onDestroy()
        mFirebaseRepository.destroyUserListener(mUserValueEventListener, getUserEmail())
        mView?.onDestroy()
        mView = null
        super.onDestroy()
    }
}
