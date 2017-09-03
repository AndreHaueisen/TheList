package com.andrehaueisen.listadejanot.d_main_list.mvp

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
import com.andrehaueisen.listadejanot.utilities.createFormattedString
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by andre on 4/21/2017.
 */
class MainListModel(val context: Context, private val loaderManager: LoaderManager, val mFirebaseRepository: FirebaseRepository) :
        MainListMvpContract.Model,
        LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE = 3

    private val LOG_TAG: String = MainListModel::class.java.simpleName

    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mSenadoresMainListObservable: PublishSubject<ArrayList<Politician>>
    lateinit private var mDeputadosMainListObservable: PublishSubject<ArrayList<Politician>>
    lateinit private var mGovernadoresMainListObservable: PublishSubject<ArrayList<Politician>>

    private lateinit var mSenadoresMainList: ArrayList<Politician>
    private lateinit var mDeputadosMainList: ArrayList<Politician>
    private lateinit var mGovernadoresMainList: ArrayList<Politician>

    private val mCompleteSenadoresMainList = ArrayList<Politician>()
    private val mCompleteDeputadosMainList = ArrayList<Politician>()
    private val mCompleteGovernadoresMainList = ArrayList<Politician>()

    lateinit private var mFinalMainListSenadoresPublisher: PublishSubject<ArrayList<Politician>>
    lateinit private var mFinalMainListDeputadosPublisher: PublishSubject<ArrayList<Politician>>
    lateinit private var mFinalMainListGovernadoresPublisher: PublishSubject<ArrayList<Politician>>

    private val mOnListsReadyPublisher: PublishSubject<Boolean> = PublishSubject.create()
    private val mOnListReadyObserver = object : Observer<Boolean> {
        override fun onNext(isListSearchComplete: Boolean) {
            if (isListSearchComplete) {
                mListCounter++
            }

            if (mListCounter != 0 && mListCounter % 3 == 0 && !areAllMainListsEmpty()) {
                initiateDataLoad()
            }else if(mListCounter != 0 && mListCounter % 3 == 0 && areAllMainListsEmpty()){
                mFinalMainListDeputadosPublisher.onNext(mCompleteDeputadosMainList)
                mFinalMainListSenadoresPublisher.onNext(mCompleteSenadoresMainList)
                mFinalMainListGovernadoresPublisher.onNext(mCompleteGovernadoresMainList)
            }
        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(e: Throwable) = Unit

        override fun onComplete() = Unit
    }

    private fun areAllMainListsEmpty(): Boolean = mGovernadoresMainList.isEmpty() && mSenadoresMainList.isEmpty() && mDeputadosMainList.isEmpty()

    private var mListCounter = 0

    init {
        mOnListsReadyPublisher
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mOnListReadyObserver)

        mFirebaseRepository.listenForMinimumVotes()
    }

    fun connectToDeputadosOnFirebase(){
        mDeputadosMainListObservable = mFirebaseRepository.getDeputadosMainList()
        getDeputadosMainList()
    }

    fun connectToSenadoresOnFirebase(){
        mSenadoresMainListObservable = mFirebaseRepository.getSenadoresMainList()
        getSenadoresMainList()
    }

    fun connectToGovernadoresOnFirebase(){
        mGovernadoresMainListObservable = mFirebaseRepository.getGovernadoresMainList()
        getGovernadoresMainList()
    }

    private fun getSenadoresMainList() = mSenadoresMainListObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<Politician>> {
                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onNext(searchablePoliticiansList: ArrayList<Politician>) {
                    mSenadoresMainList = searchablePoliticiansList
                    mOnListsReadyPublisher.onNext(true)

                }

                override fun onComplete() = Unit
            })

    private fun getDeputadosMainList() = mDeputadosMainListObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<Politician>> {
                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onNext(deputadosMainList: ArrayList<Politician>) {
                    mDeputadosMainList = deputadosMainList
                    mOnListsReadyPublisher.onNext(true)
                }

                override fun onComplete() = Unit
            })

    private fun getGovernadoresMainList() = mGovernadoresMainListObservable
            .subscribeOn(Schedulers.io())
            .observeOn((AndroidSchedulers.mainThread()))
            .subscribe(object : Observer<ArrayList<Politician>> {
                override fun onSubscribe(disposable: Disposable) {
                    mCompositeDisposable.add(disposable)
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, e.toString())
                    mOnListsReadyPublisher.onNext(false)
                }

                override fun onNext(governadoresMainList: ArrayList<Politician>) {
                    mGovernadoresMainList = governadoresMainList
                    mOnListsReadyPublisher.onNext(true)
                }

                override fun onComplete() = Unit
            })

    override fun initiateDataLoad() {
        if (loaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            loaderManager.initLoader(LOADER_ID, null, this)

        } else {
            loaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        val politiciansNames = ArrayList<String>()
        mDeputadosMainList.forEach { politiciansNames.add(it.name) }
        mSenadoresMainList.forEach { politiciansNames.add(it.name) }
        mGovernadoresMainList.forEach { politiciansNames.add(it.name) }


        val selectionArgsPlaceholders = politiciansNames.createFormattedString("(", "?,", "?)", ignoreCollectionValues = true)

        return CursorLoader(context,
                politiciansEntry.CONTENT_URI,
                POLITICIANS_COLUMNS,
                "${politiciansEntry.COLUMN_NAME} IN $selectionArgsPlaceholders",
                politiciansNames.toTypedArray(),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            if (mCompleteDeputadosMainList.isNotEmpty()) mCompleteDeputadosMainList.clear()
            if (mCompleteSenadoresMainList.isNotEmpty()) mCompleteSenadoresMainList.clear()
            if (mCompleteGovernadoresMainList.isNotEmpty()) mCompleteGovernadoresMainList.clear()

            for (i in 0 until data.count) {
                val politicianPost = data.getString(COLUMNS_INDEX_POST)
                val politicianName = data.getString(COLUMNS_INDEX_NAME)
                val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)
                val politicianImage = data.getBlob(COLUMNS_INDEX_IMAGE)

                when (politicianPost) {
                    Politician.Post.DEPUTADO.name ->
                        mergeFirebaseDeputadoToDatabase(Politician.Post.DEPUTADO, politicianName, politicianEmail, politicianImage)

                    Politician.Post.DEPUTADA.name ->
                        mergeFirebaseDeputadoToDatabase(Politician.Post.DEPUTADA, politicianName, politicianEmail, politicianImage)

                    Politician.Post.SENADOR.name ->
                        mergeFirebaseSenadorToDatabase(Politician.Post.SENADOR, politicianName, politicianEmail, politicianImage)

                    Politician.Post.SENADORA.name ->
                        mergeFirebaseSenadorToDatabase(Politician.Post.SENADORA, politicianName, politicianEmail, politicianImage)

                    Politician.Post.GOVERNADOR.name ->
                        mergeFirebaseGovernadorToDatabase(Politician.Post.GOVERNADOR, politicianName, politicianEmail, politicianImage)

                    Politician.Post.GOVERNADORA.name ->
                        mergeFirebaseGovernadorToDatabase(Politician.Post.GOVERNADORA, politicianName, politicianEmail, politicianImage)

                }

                data.moveToNext()
            }

            Collections.sort(mCompleteDeputadosMainList, Politician.Comparators.NAME)
            Collections.sort(mCompleteSenadoresMainList, Politician.Comparators.NAME)
            Collections.sort(mCompleteGovernadoresMainList, Politician.Comparators.NAME)
            mFinalMainListDeputadosPublisher.onNext(mCompleteDeputadosMainList)
            mFinalMainListDeputadosPublisher.onComplete()
            mFinalMainListSenadoresPublisher.onNext(mCompleteSenadoresMainList)
            mFinalMainListSenadoresPublisher.onComplete()
            mFinalMainListGovernadoresPublisher.onNext(mCompleteGovernadoresMainList)
            mFinalMainListGovernadoresPublisher.onComplete()
            data.close()
        }
    }

    private fun mergeFirebaseDeputadoToDatabase(deputadoPost: Politician.Post, deputadoName: String, deputadoEmail: String, deputadoImage: ByteArray) {
        mDeputadosMainList
                .find { mainListDeputado -> mainListDeputado.name == deputadoName }
                .also { mainListDeputado ->
                    if (mainListDeputado != null) {
                        mainListDeputado.post = deputadoPost
                        mainListDeputado.email = deputadoEmail
                        mainListDeputado.image = deputadoImage
                        mCompleteDeputadosMainList.add(mainListDeputado)
                    }
                }
    }

    private fun mergeFirebaseSenadorToDatabase(senadorPost: Politician.Post, senadorName: String, senadorEmail: String, senadorImage: ByteArray) {
        mSenadoresMainList
                .find { mainListSenador -> mainListSenador.name == senadorName }
                .also { mainListSenador ->
                    if (mainListSenador != null) {
                        mainListSenador.post = senadorPost
                        mainListSenador.email = senadorEmail
                        mainListSenador.image = senadorImage
                        mCompleteSenadoresMainList.add(mainListSenador)
                    }
                }
    }

    private fun mergeFirebaseGovernadorToDatabase(governadorPost: Politician.Post, governadorName: String, governadorEmail: String, governadorImage: ByteArray) {
        mGovernadoresMainList
                .find { mainListGovernador -> mainListGovernador.name == governadorName }
                .also { mainListGovernador ->
                    if (mainListGovernador != null) {
                        mainListGovernador.post = governadorPost
                        mainListGovernador.email = governadorEmail
                        mainListGovernador.image = governadorImage
                        mCompleteGovernadoresMainList.add(mainListGovernador)
                    }
                }
    }

    override fun loadSenadoresMainList(): Observable<ArrayList<Politician>> {
        mFinalMainListSenadoresPublisher = PublishSubject.create()

        return mFinalMainListSenadoresPublisher
    }


    override fun loadDeputadosMainList(): Observable<ArrayList<Politician>> {
        mFinalMainListDeputadosPublisher = PublishSubject.create()
        return mFinalMainListDeputadosPublisher
    }

    override fun loadGovernadoresMainList(): Observable<ArrayList<Politician>> {
        mFinalMainListGovernadoresPublisher = PublishSubject.create()
        return mFinalMainListGovernadoresPublisher
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mFirebaseRepository.removeMinimumVoteListener()
        mFirebaseRepository.onDestroy()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) = Unit
}