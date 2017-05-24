package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.util.Log
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.MaybeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 4/21/2017.
 */
class MainListModel(val context: Context, val loaderManager: LoaderManager, val mFirebaseRepository: FirebaseRepository) :
        MainListMvpContract.Model,
        LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_CARGO = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE = 3

    private val LOG_TAG: String = MainListModel::class.java.simpleName

    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mSenadoresMainListObservable : PublishSubject<ArrayList<Politician>>
    lateinit private var mDeputadosMainListObservable : PublishSubject<ArrayList<Politician>>

    private lateinit var mSenadoresMainList: ArrayList<Politician>
    private lateinit var mDeputadosMainList: ArrayList<Politician>

    private val mFilteredSenadoresMainList = ArrayList<Politician>()
    private val mFilteredDeputadosMainList = ArrayList<Politician>()

    private val mFinalMainListSenadoresPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mFinalMainListDeputadosPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

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

    fun connectToFirebase(){
        mSenadoresMainListObservable = mFirebaseRepository.getSenadoresMainList()
        mDeputadosMainListObservable = mFirebaseRepository.getDeputadosMainList()

        getSenadoresMainList()
        getDeputadosMainList()
    }

    private fun getSenadoresMainList() {
        mSenadoresMainListObservable
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
                        mSenadoresMainList = searchablePoliticiansList
                        mOnListsReadyPublisher.onNext(true)
                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun getDeputadosMainList() {
        mDeputadosMainListObservable
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onSuccess(deputadosMainList: ArrayList<Politician>) {
                        mDeputadosMainList = deputadosMainList
                        mOnListsReadyPublisher.onNext(true)
                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun initiateDataLoad() {
        if (loaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            loaderManager.initLoader(LOADER_ID, null, this)

        } else {
            loaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(context, politiciansEntry.CONTENT_URI, POLITICIANS_COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            for (i in 0..data.count - 1) {
                if (data.getString(COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name) {

                    val deputadoName = data.getString(COLUMNS_INDEX_NAME)
                    val deputadoEmail = data.getString(COLUMNS_INDEX_EMAIL)
                    val deputadoImage = data.getBlob(COLUMNS_INDEX_IMAGE)

                    searchForDeputadoOnList(deputadoName, deputadoEmail, deputadoImage)


                } else {
                    val senadorName = data.getString(COLUMNS_INDEX_NAME)
                    val senadorEmail = data.getString(COLUMNS_INDEX_EMAIL)
                    val senadorImage = data.getBlob(COLUMNS_INDEX_IMAGE)

                    searchForSenadorOnList(senadorName, senadorEmail, senadorImage)
                }
                data.moveToNext()
            }
            mFinalMainListDeputadosPublisher.onNext(mFilteredDeputadosMainList)
            mFinalMainListSenadoresPublisher.onNext(mFilteredSenadoresMainList)
            data.close()
        }
    }

    private fun searchForDeputadoOnList(deputadoName: String, deputadoEmail: String, deputadoImage: ByteArray) {
        try {
            mDeputadosMainList
                    .first { mainListDeputado -> mainListDeputado.name == deputadoName }
                    .also { mainListDeputado ->
                        mainListDeputado.post = Politician.Post.DEPUTADO
                        mainListDeputado.email = deputadoEmail
                        mainListDeputado.image = deputadoImage
                        mFilteredDeputadosMainList.add(mainListDeputado)
                    }
        } catch (noSuchElement: NoSuchElementException) {

        }
    }

    private fun searchForSenadorOnList(senadorName: String, senadorEmail: String, senadorImage: ByteArray) {
        try {
            mSenadoresMainList
                    .first { mainListSenador -> mainListSenador.name == senadorName }
                    .also { mainListSenador ->
                        mainListSenador.post = Politician.Post.SENADOR
                        mainListSenador.email = senadorEmail
                        mainListSenador.image = senadorImage
                        mFilteredSenadoresMainList.add(mainListSenador)
                    }

        } catch (noSuchElement: NoSuchElementException) {

        }
    }

    override fun loadSenadoresMainList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalMainListSenadoresPublisher }
    }


    override fun loadDeputadosMainList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalMainListDeputadosPublisher }
    }


    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mFirebaseRepository.onDestroy()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}