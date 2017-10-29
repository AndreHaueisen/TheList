package com.andrehaueisen.listadejanot.g_user_vote_list.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.g_user_vote_list.dagger.DaggerUserVoteListComponent
import com.andrehaueisen.listadejanot.g_user_vote_list.dagger.UserVoteListModule
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_CURRENT_SHOWING_LIST
import com.andrehaueisen.listadejanot.utilities.BUNDLE_MANAGER
import com.andrehaueisen.listadejanot.utilities.BUNDLE_VOTED_POLITICIANS
import com.andrehaueisen.listadejanot.utilities.showToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.g_activity_user_vote_list.*
import javax.inject.Inject

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListPresenterActivity : AppCompatActivity() {

    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator
    @Inject
    lateinit var mModel: UserVoteListModel

    private lateinit var mView: UserVoteListView
    private lateinit var mOnUserListsPoliticians: ArrayList<Politician>

    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerUserVoteListComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .userVoteListModule(UserVoteListModule(supportLoaderManager))
                .build()
                .inject(this)

        mView = UserVoteListView(this)
        subscribeToOnUserListsPoliticians()

        if (savedInstanceState == null) {
            mView.setViews(null)
            fetchChosenPoliticians()
        } else {
            mOnUserListsPoliticians = savedInstanceState.getParcelableArrayList(BUNDLE_VOTED_POLITICIANS)
            mView.setViews(savedInstanceState)

        }
    }

    private fun subscribeToOnUserListsPoliticians() = mModel.subscribeToOnUserListsPoliticians()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ politicians ->
                mOnUserListsPoliticians = politicians
                mView.notifyOnUserListsPoliticiansReady(getUser())
            })

    private fun fetchChosenPoliticians() {
        if (mFirebaseAuthenticator.getUserEmail() == null) {
            this.showToast(this.getString(R.string.null_email_found))
        } else {
            mModel.fetchChosenPoliticians()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(BUNDLE_VOTED_POLITICIANS, getOnUserListsPoliticians())
        outState.putParcelable(BUNDLE_MANAGER, votes_recycler_view.layoutManager.onSaveInstanceState())
        outState.putInt(BUNDLE_CURRENT_SHOWING_LIST, mView.getCurrentShowingList())
        super.onSaveInstanceState(outState)
    }

    fun getOnUserListsPoliticians() = mOnUserListsPoliticians

    fun getUser() = mModel.getUser()

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}