package com.andrehaueisen.listadejanot.g_user_vote_list.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.g_user_vote_list.dagger.DaggerUserVoteListComponent
import com.andrehaueisen.listadejanot.g_user_vote_list.dagger.UserVoteListModule
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_VOTED_POLITICIANS
import com.andrehaueisen.listadejanot.utilities.ImageFetcherModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.g_activity_user_vote_list.*
import javax.inject.Inject

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListPresenterActivity: AppCompatActivity() {

    @Inject
    lateinit var mModel: UserVoteListModel
    @Inject
    lateinit var mThumbnailFetcherModel: ImageFetcherModel

    private lateinit var mView: UserVoteListView
    private lateinit var mVotedPoliticians: ArrayList<Politician>

    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerUserVoteListComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .userVoteListModule(UserVoteListModule(supportLoaderManager))
                .build()
                .inject(this)

        mView = UserVoteListView(this)

        if(savedInstanceState == null) {
            mView.setViews(null)
            mModel.initiatePoliticianLoad()
            subscribeToVotedPoliticians()
        }else{
            mVotedPoliticians = savedInstanceState.getParcelableArrayList(BUNDLE_VOTED_POLITICIANS)
            mView.setViews(savedInstanceState)

        }
    }

    private fun subscribeToVotedPoliticians() = mModel.loadVotedPoliticians()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mVotedPoliticiansObserver)

    private val mVotedPoliticiansObserver = object: Observer<ArrayList<Politician>>{
        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onComplete() = Unit

        override fun onError(e: Throwable) = Unit

        override fun onNext(userVoteList: ArrayList<Politician>) {
            mVotedPoliticians = userVoteList
            mView.notifyVotesListReady(getUser())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_VOTED_POLITICIANS, getVotedPoliticians())
        outState.putParcelable(BUNDLE_MANAGER, votes_recycler_view.layoutManager.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    fun getVotedPoliticians() = mVotedPoliticians

    fun getUser() = mModel.getUser()

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}