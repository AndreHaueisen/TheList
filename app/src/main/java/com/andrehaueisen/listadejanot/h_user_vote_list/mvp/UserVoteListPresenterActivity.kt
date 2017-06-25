package com.andrehaueisen.listadejanot.h_user_vote_list.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.h_user_vote_list.dagger.DaggerUserVoteListComponent
import com.andrehaueisen.listadejanot.h_user_vote_list.dagger.UserVoteListModule
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_USER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_USER_VOTES_LIST
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.h_activity_user_vote_list.*
import javax.inject.Inject

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListPresenterActivity: AppCompatActivity(), UserVoteListMvpContract.Presenter {

    @Inject
    lateinit var mModel: UserVoteListModel
    private lateinit var mView: UserVoteListView

    private lateinit var mUserVotesList: ArrayList<Politician>
    private lateinit var mUser: User
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
            mModel.initiateUserLoad()
            subscribeToUserVotesList()
        }else{
            mUserVotesList = savedInstanceState.getParcelableArrayList(BUNDLE_USER_VOTES_LIST)
            mUser = savedInstanceState.getParcelable(BUNDLE_USER)
            mView.setViews(savedInstanceState)

        }
    }

    fun subscribeToUserVotesList() {
        mModel.loadUserVotesList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mUserVoteListObserver)
    }

    val mUserVoteListObserver = object: Observer<ArrayList<Politician>>{
        override fun onSubscribe(disposable: Disposable?) {
            mCompositeDisposable.add(disposable)
        }

        override fun onComplete() {

        }

        override fun onError(e: Throwable?) {

        }

        override fun onNext(userVoteList: ArrayList<Politician>?) {
            mUserVotesList = userVoteList ?: ArrayList()
            mUser = mModel.getUser()
            mView.notifyVotesListReady(mUser)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_USER_VOTES_LIST, getUserVotesList())
        outState.putParcelable(BUNDLE_MANAGER, votes_recycler_view.layoutManager.onSaveInstanceState())
        outState.putParcelable(BUNDLE_USER, mUser)
        super.onSaveInstanceState(outState)
    }

    fun getUserVotesList() = mUserVotesList

    fun getUser() = mUser

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}