package com.andrehaueisen.listadejanot.d_search_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.util.Log
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS
import io.reactivex.MaybeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/11/2017.
 */
class PoliticianSelectorModel(private val mContext: Context,
                              private val mLoaderManager: LoaderManager,
                              val mFirebaseRepository: FirebaseRepository) :
        PoliticianSelectorMvpContract.Model,
        LoaderManager.LoaderCallbacks<Cursor> {

    private val LOG_TAG: String = PoliticianSelectorModel::class.java.simpleName
    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2

    private val mCompositeDisposable = CompositeDisposable()

    private var mSenadoresListPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private var mDeputadosListPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private var mGovernadoresListPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

    private lateinit var mSenadoresList: ArrayList<Politician>
    private lateinit var mDeputadosList: ArrayList<Politician>
    private lateinit var mGovernadoresList: ArrayList<Politician>

    private var mSearchablePoliticianList = ArrayList<Politician>()
    private val mFinalSearchablePoliticianPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mOnListsReadyPublisher: PublishSubject<Boolean> = PublishSubject.create()
    private var mListCounter = 0

    init {
        mOnListsReadyPublisher
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(disposable: Disposable) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(isListSearchComplete: Boolean) {
                        if (isListSearchComplete) {
                            mListCounter++
                        }
                        if (mListCounter % 3 == 0) {
                            initiateDataLoad()
                        }
                    }

                    override fun onComplete() = Unit

                    override fun onError(e: Throwable) = Unit
                })
    }

    fun connectToFirebase() {
        mSenadoresListPublisher = mFirebaseRepository.getSenadoresList()
        mDeputadosListPublisher = mFirebaseRepository.getDeputadosList()
        mGovernadoresListPublisher = mFirebaseRepository.getGovernadoresList()

        getSenadoresList()
        getDeputadosList()
        getGovernadoresList()
    }

    private fun getSenadoresList() = mSenadoresListPublisher
            .firstElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
                    mSenadoresList = searchablePoliticiansList
                    mOnListsReadyPublisher.onNext(true)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onComplete() = Unit
            })

    private fun getDeputadosList() = mDeputadosListPublisher
            .firstElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
                    mDeputadosList = searchablePoliticiansList
                    mOnListsReadyPublisher.onNext(true)
                }

                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onComplete() = Unit
            })

    private fun getGovernadoresList() = mGovernadoresListPublisher
            .firstElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<ArrayList<Politician>>{
                override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
                    mGovernadoresList = searchablePoliticiansList
                    mOnListsReadyPublisher.onNext(true)
                }

                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onComplete() = Unit
            })

    override fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(mContext, politiciansEntry.CONTENT_URI, POLITICIANS_COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if(mSearchablePoliticianList.isNotEmpty()){
            mSearchablePoliticianList.clear()
        }

        if (data != null && data.count != 0) {
            data.moveToFirst()

            for (i in 0 until data.count) {
                val politicianName = data.getString(COLUMNS_INDEX_NAME)
                val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)

                when (data.getString(COLUMNS_INDEX_POST)) {
                    Politician.Post.DEPUTADO.name ->
                        addDeputadoToSearchableList(Politician.Post.DEPUTADO, politicianName, politicianEmail)

                    Politician.Post.DEPUTADA.name ->
                        addDeputadoToSearchableList(Politician.Post.DEPUTADA, politicianName, politicianEmail)

                    Politician.Post.SENADOR.name ->
                        addSenadorToSearchableList(Politician.Post.SENADOR, politicianName, politicianEmail)

                    Politician.Post.SENADORA.name ->
                        addSenadorToSearchableList(Politician.Post.SENADORA, politicianName, politicianEmail)

                    Politician.Post.GOVERNADOR.name ->
                        addGovernadorToSearchableList(Politician.Post.GOVERNADOR, politicianName, politicianEmail)

                    Politician.Post.GOVERNADORA.name ->
                        addGovernadorToSearchableList(Politician.Post.GOVERNADORA, politicianName, politicianEmail)
                }

                data.moveToNext()
            }
            mFinalSearchablePoliticianPublisher.onNext(mSearchablePoliticianList)
            data.close()
        }
    }

    private fun addDeputadoToSearchableList(post: Politician.Post, deputadoName: String, deputadoEmail: String) {
        val deputado = Politician(post, deputadoName, deputadoEmail)
        try {
            mDeputadosList.first { it.name == deputadoName }
                    .also {
                        deputado.recommendationsCount = it.recommendationsCount
                        deputado.condemnationsCount = it.condemnationsCount

                        deputado.honestyGrade = it.honestyGrade
                        deputado.honestyCount = it.honestyCount
                        deputado.leaderGrade = it.leaderGrade
                        deputado.leaderCount = it.leaderCount
                        deputado.promiseKeeperGrade = it.promiseKeeperGrade
                        deputado.promiseKeeperCount = it.promiseKeeperCount
                        deputado.rulesForThePeopleGrade = it.rulesForThePeopleGrade
                        deputado.rulesForThePeopleCount = it.rulesForThePeopleCount
                        deputado.answerVotersGrade = it.answerVotersGrade
                        deputado.answerVotersCount = it.answerVotersCount

                        mDeputadosList.remove(it)
                    }

            mSearchablePoliticianList.add(deputado)

        } catch (noSuchElement: NoSuchElementException) {
            mSearchablePoliticianList.add(deputado)
        }
    }

    private fun addSenadorToSearchableList(post: Politician.Post, senadorName: String, senadorEmail: String) {
        val senador = Politician(post, senadorName, senadorEmail)
        try {
            mSenadoresList.first { it.name == senadorName }
                    .also {
                        senador.recommendationsCount = it.recommendationsCount
                        senador.condemnationsCount = it.condemnationsCount

                        senador.honestyGrade = it.honestyGrade
                        senador.honestyCount = it.honestyCount
                        senador.leaderGrade = it.leaderGrade
                        senador.leaderCount = it.leaderCount
                        senador.promiseKeeperGrade = it.promiseKeeperGrade
                        senador.promiseKeeperCount = it.promiseKeeperCount
                        senador.rulesForThePeopleGrade = it.rulesForThePeopleGrade
                        senador.rulesForThePeopleCount = it.rulesForThePeopleCount
                        senador.answerVotersGrade = it.answerVotersGrade
                        senador.answerVotersCount = it.answerVotersCount

                        mSenadoresList.remove(it)
                    }

            mSearchablePoliticianList.add(senador)

        } catch (noSuchElement: NoSuchElementException) {
            mSearchablePoliticianList.add(senador)
        }
    }

    private fun addGovernadorToSearchableList(post: Politician.Post, governadorName: String, governadorEmail: String){
        val governador = Politician(post, governadorName, governadorEmail)
        try{
            mGovernadoresList.first { it.name == governadorName }
                    .also{
                        governador.recommendationsCount = it.recommendationsCount
                        governador.condemnationsCount = it.condemnationsCount

                        governador.honestyGrade = it.honestyGrade
                        governador.honestyCount = it.honestyCount
                        governador.leaderGrade = it.leaderGrade
                        governador.leaderCount = it.leaderCount
                        governador.promiseKeeperGrade = it.promiseKeeperGrade
                        governador.promiseKeeperCount = it.promiseKeeperCount
                        governador.rulesForThePeopleGrade = it.rulesForThePeopleGrade
                        governador.rulesForThePeopleCount = it.rulesForThePeopleCount
                        governador.answerVotersGrade = it.answerVotersGrade
                        governador.answerVotersCount = it.answerVotersCount

                        mGovernadoresList.remove(it)
                    }

            mSearchablePoliticianList.add(governador)
        } catch (noSuchElement: NoSuchElementException){
            mSearchablePoliticianList.add(governador)
        }
    }

    fun getSearchablePoliticiansList() = mSearchablePoliticianList

    fun setSearchablePoliticiansList(originalPoliticiansList: ArrayList<Politician>) {
        mSearchablePoliticianList = originalPoliticiansList
    }

    override fun loadSearchablePoliticiansList(): Observable<ArrayList<Politician>> = mFinalSearchablePoliticianPublisher

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mFirebaseRepository.onDestroy()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) = Unit
}