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
import io.reactivex.MaybeObserver
import io.reactivex.Observable
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
                        private val mFirebaseRepository: FirebaseRepository,
                        private val mFirebaseAuthenticator: FirebaseAuthenticator,
                        private val mUser: User) : LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2

    private lateinit var mOnDeputadosPreListReady: PublishSubject<ArrayList<Politician>>
    private lateinit var mOnSenadoresPreListReady: PublishSubject<ArrayList<Politician>>
    private lateinit var mOnGovernadoresPreListReady: PublishSubject<ArrayList<Politician>>

    private val mOnVotedPoliticiansReadyPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()
    private val mChosenPoliticians = ArrayList<Politician>()
    private var mListReadyCounter = 0

    fun initiatePoliticianLoad() {
        val userEmail = mFirebaseAuthenticator.getUserEmail()
        if (userEmail == null) {
            mContext.showToast(mContext.getString(R.string.null_email_found))

        } else {
            initiateDataLoad()
        }
    }

    private fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(p0: Int, userBundle: Bundle?): Loader<Cursor> {
        val politiciansEmails = mutableSetOf<String>()
        politiciansEmails.addAll(mUser.condemnations.keys.map { email -> email.decodeEmail() })
        politiciansEmails.addAll(mUser.recommendations.keys.map { email -> email.decodeEmail() })

        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()

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

            mOnDeputadosPreListReady = mFirebaseRepository.getDeputadosPreList()
            mOnSenadoresPreListReady = mFirebaseRepository.getSenadoresPreList()
            mOnGovernadoresPreListReady = mFirebaseRepository.getGovernadoresPreList()
            listenToPoliticiansLists()
            data.close()
        }
    }

    private fun listenToPoliticiansLists(){
        mOnDeputadosPreListReady
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deputadosPreListObserver)

        mOnSenadoresPreListReady
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(senadoresPreListObserver)

        mOnGovernadoresPreListReady
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(governadoresPreListObserver)
    }

    private val deputadosPreListObserver = object : MaybeObserver<ArrayList<Politician>> {

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onSuccess(deputados: ArrayList<Politician>) {
            mChosenPoliticians.addAll(extractChosenPoliticians(deputados))
            mListReadyCounter++

            if (mListReadyCounter % 3 == 0) {
                mOnVotedPoliticiansReadyPublisher.onNext(mChosenPoliticians)
                mOnVotedPoliticiansReadyPublisher.onComplete()
            }
        }

        override fun onError(e: Throwable) {}
        override fun onComplete() {}
    }
    private val senadoresPreListObserver = object : MaybeObserver<ArrayList<Politician>> {
        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onSuccess(senadores: ArrayList<Politician>) {
            mChosenPoliticians.addAll(extractChosenPoliticians(senadores))
            mListReadyCounter++

            if (mListReadyCounter % 3 == 0) {
                mOnVotedPoliticiansReadyPublisher.onNext(mChosenPoliticians)
                mOnVotedPoliticiansReadyPublisher.onComplete()
            }
        }

        override fun onComplete() {}
        override fun onError(e: Throwable) {}
    }
    private val governadoresPreListObserver = object : MaybeObserver<ArrayList<Politician>> {

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onSuccess(governadores: ArrayList<Politician>) {
            mChosenPoliticians.addAll(extractChosenPoliticians(governadores))
            mListReadyCounter++

            if (mListReadyCounter % 3 == 0) {
                mOnVotedPoliticiansReadyPublisher.onNext(mChosenPoliticians)
                mOnVotedPoliticiansReadyPublisher.onComplete()
            }
        }

        override fun onComplete() {}
        override fun onError(e: Throwable) {}
    }

    private fun extractChosenPoliticians(politicians: ArrayList<Politician>): List<Politician> {
        val userVotedPoliticiansEmails = mutableSetOf<String>()
        userVotedPoliticiansEmails.addAll(mUser.recommendations.keys)
        userVotedPoliticiansEmails.addAll(mUser.condemnations.keys)

        return politicians.filter { politician -> userVotedPoliticiansEmails.contains(politician.email?.encodeEmail()) }
    }

    private fun createPolitician(post: Politician.Post, politicianName: String, politicianEmail: String) =
            Politician(post, politicianName, politicianEmail)


    fun loadVotedPoliticians(): Observable<ArrayList<Politician>> = Observable.defer { mOnVotedPoliticiansReadyPublisher }
    fun getUser() = mUser

    fun onDestroy() = mCompositeDisposable.dispose()

    override fun onLoaderReset(p0: Loader<Cursor>?) = Unit
}