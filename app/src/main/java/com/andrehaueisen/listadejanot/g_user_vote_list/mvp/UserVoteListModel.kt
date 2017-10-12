package com.andrehaueisen.listadejanot.g_user_vote_list.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListModel(private val mContext: Context,
                        private val mLoaderManager: LoaderManager,
                        val mFirebaseRepository: FirebaseRepository,
                        private val mFirebaseAuthenticator: FirebaseAuthenticator) : LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2

    private lateinit var mOnUserReadyPublisher: PublishSubject<User>
    private lateinit var mOnVoteCountMapReadyPublisher: PublishSubject<HashMap<String, Long>>
    private lateinit var mUser: User
    private lateinit var mVoteCountMap: HashMap<String, Long>

    private val mOnUserVoteListReadyPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()

    fun initiateUserLoad() {
        val userEmail = mFirebaseAuthenticator.getUserEmail()
        if (userEmail == null) {
            mContext.showToast(mContext.getString(R.string.null_email_found))

        } else {
            mOnUserReadyPublisher = mFirebaseRepository.getUser(userEmail)
            mOnUserReadyPublisher
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mOnUserReadyObservable)
        }
    }

    private val mOnUserReadyObservable = object : Observer<User> {

        override fun onNext(user: User) {
            mUser = user
            if (mUser.condemnations.isNotEmpty()) {
                mOnVoteCountMapReadyPublisher = mFirebaseRepository.getVoteCountList()
                getVoteCountMap()
            } else {
                mOnUserVoteListReadyPublisher.onNext(arrayListOf())
            }
            mOnUserReadyPublisher.onComplete()
        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(e: Throwable) = Unit
        override fun onComplete() = Unit
    }

    fun getVoteCountMap() = mOnVoteCountMapReadyPublisher
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mOnVoteListCountReadyObservable)

    private val mOnVoteListCountReadyObservable: Observer<HashMap<String, Long>> = object : Observer<HashMap<String, Long>> {
        override fun onNext(voteCountMap: HashMap<String, Long>) {
            mVoteCountMap = voteCountMap
            initiateDataLoad()
        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(e: Throwable) = Unit
        override fun onComplete() = Unit
    }

    private fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(p0: Int, userBundle: Bundle?): Loader<Cursor> {

        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        val politiciansEmails = mUser.condemnations.keys.map { email -> email.decodeEmail() }
        val selectionArgsPlaceholders = politiciansEmails.createFormattedString("(", "?,", "?)", ignoreCollectionValues = true)
        return CursorLoader(
                mContext,
                politiciansEntry.CONTENT_URI,
                POLITICIANS_COLUMNS,
                "${politiciansEntry.COLUMN_EMAIL} IN $selectionArgsPlaceholders",
                politiciansEmails.toTypedArray(),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            val listOfVotedPoliticians = arrayListOf<Politician>()

            for (i in 0 until data.count) {

                val politicianPost = data.getString(COLUMNS_INDEX_POST)
                val politicianName = data.getString(COLUMNS_INDEX_NAME)
                val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)


                when (politicianPost) {
                    Politician.Post.DEPUTADO.name -> {
                        val politician = createPolitician(Politician.Post.DEPUTADO, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }

                    Politician.Post.DEPUTADA.name -> {
                        val politician = createPolitician(Politician.Post.DEPUTADA, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }

                    Politician.Post.SENADOR.name -> {
                        val politician = createPolitician(Politician.Post.SENADOR, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }

                    Politician.Post.SENADORA.name -> {
                        val politician = createPolitician(Politician.Post.SENADORA, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }

                    Politician.Post.GOVERNADOR.name -> {
                        val politician = createPolitician(Politician.Post.GOVERNADOR, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }

                    Politician.Post.GOVERNADORA.name -> {
                        val politician = createPolitician(Politician.Post.GOVERNADORA, politicianName, politicianEmail)
                        listOfVotedPoliticians.add(politician)
                    }
                }

                data.moveToNext()
            }

            mOnUserVoteListReadyPublisher.onNext(listOfVotedPoliticians)
            mOnUserVoteListReadyPublisher.onComplete()
            data.close()
        }
    }

    private fun createPolitician(post: Politician.Post, politicianName: String, politicianEmail: String) =
            Politician(post, politicianName, politicianEmail, mVoteCountMap[politicianEmail.encodeEmail()] ?: 0)


    fun loadUserVotesList(): Observable<ArrayList<Politician>> = Observable.defer { mOnUserVoteListReadyPublisher }
    fun getUser() = mUser

    fun onDestroy() = mCompositeDisposable.dispose()

    override fun onLoaderReset(p0: Loader<Cursor>?) = Unit
}