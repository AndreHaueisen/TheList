package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.util.Log
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS_NO_IMAGE
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
class PoliticianSelectorModel(val mContext: Context,
                              val mLoaderManager: LoaderManager,
                              val mFirebaseRepository: FirebaseRepository,
                              val mSenadoresMainList: ArrayList<Politician>,
                              val mDeputadosMainList: ArrayList<Politician>) :
        PoliticianSelectorMvpContract.Model,
        LoaderManager.LoaderCallbacks<Cursor> {

    private val LOG_TAG: String = PoliticianSelectorModel::class.java.simpleName
    private val COLUMNS_INDEX_CARGO = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2

    private val mCompositeDisposable = CompositeDisposable()

    private var mSenadoresPreListPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private var mDeputadosPreListPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

    private lateinit var mSenadoresPreList: ArrayList<Politician>
    private lateinit var mDeputadosPreList: ArrayList<Politician>

    private val mSearchablePoliticianList = ArrayList<Politician>()
    private val mFinalSearchablePoliticianPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mOnListsReadyPublisher: PublishSubject<Boolean> = PublishSubject.create()
    private var mListCounter = 0

    init {
        mOnListsReadyPublisher
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(isListSearchComplete: Boolean) {
                        if (isListSearchComplete) {
                            mListCounter++
                        }
                        if (mListCounter == 2) {
                            mOnListsReadyPublisher.onComplete()
                        }
                    }

                    override fun onComplete() {
                        initiateDataLoad()
                    }

                    override fun onError(e: Throwable?) {

                    }
                })
    }

    fun connectToFirebase() {
        mSenadoresPreListPublisher = mFirebaseRepository.getSenadoresPreList()
        mDeputadosPreListPublisher = mFirebaseRepository.getDeputadosPreList()

        getSenadoresPreList()
        getDeputadosPreList()
    }

    private fun getSenadoresPreList() {
        mSenadoresPreListPublisher
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>?) {
                        if(searchablePoliticiansList != null) {
                            mSenadoresPreList = searchablePoliticiansList
                            mOnListsReadyPublisher.onNext(true)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun getDeputadosPreList() {
        mDeputadosPreListPublisher
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                    override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>?) {
                        if (searchablePoliticiansList != null) {
                            mDeputadosPreList = searchablePoliticiansList
                            mOnListsReadyPublisher.onNext(true)
                        }
                    }

                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(mContext, politiciansEntry.CONTENT_URI, POLITICIANS_COLUMNS_NO_IMAGE, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            for (i in 0..data.count - 1) {
                if (data.getString(COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name) {

                    val deputadoName = data.getString(COLUMNS_INDEX_NAME)
                    val deputadoEmail = data.getString(COLUMNS_INDEX_EMAIL)

                    addDeputadoToSearchableList(deputadoName, deputadoEmail)


                } else {
                    val senadorName = data.getString(COLUMNS_INDEX_NAME)
                    val senadorEmail = data.getString(COLUMNS_INDEX_EMAIL)

                    addSenadorToSearchableList(senadorName, senadorEmail)
                }
                data.moveToNext()
            }
            mFinalSearchablePoliticianPublisher.onNext(mSearchablePoliticianList)
            data.close()
        }
    }

    private fun addDeputadoToSearchableList(deputadoName: String, deputadoEmail: String) {

        val deputado = Politician(Politician.Post.DEPUTADO, deputadoName, deputadoEmail)
        try {
            mDeputadosPreList.first { it.name == deputadoName }
                    .also {
                        deputado.votesNumber = it.votesNumber
                        deputado.condemnedBy = it.condemnedBy
                        mDeputadosPreList.remove(it)
                    }

            mSearchablePoliticianList.add(deputado)

        } catch (noSuchElement: NoSuchElementException) {
            mSearchablePoliticianList.add(deputado)
        }
    }

    private fun addSenadorToSearchableList(senadorName: String, senadorEmail: String) {

        val senador = Politician(Politician.Post.SENADOR, senadorName, senadorEmail)
        try {
            mSenadoresPreList.first { it.name == senadorName }
                    .also {
                        senador.votesNumber = it.votesNumber
                        senador.condemnedBy = it.condemnedBy
                        mSenadoresPreList.remove(it)
                    }

            mSearchablePoliticianList.add(senador)

        } catch (noSuchElement: NoSuchElementException) {
            mSearchablePoliticianList.add(senador)
        }
    }

    fun getSearchablePoliticiansList () = mSearchablePoliticianList

    override fun loadSearchablePoliticiansList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalSearchablePoliticianPublisher }
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mFirebaseRepository.onDestroy()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}