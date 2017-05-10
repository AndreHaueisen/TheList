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
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
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

    private val LOG_TAG: String = MainListModel::class.java.simpleName

    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mSenadoresMainListObservable : Observable<ArrayList<Politician>>
    lateinit private var mSenadoresPreListObservable : Observable<ArrayList<Politician>>
    lateinit private var mDeputadosMainListObservable : Observable<ArrayList<Politician>>
    lateinit private var mDeputadosPreListObservable : Observable<ArrayList<Politician>>

    private lateinit var mSenadoresMainList: ArrayList<Politician>
    private lateinit var mSenadoresPreList: ArrayList<Politician>
    private lateinit var mDeputadosMainList: ArrayList<Politician>
    private lateinit var mDeputadosPreList: ArrayList<Politician>

    private val mFilteredSenadoresMainList = ArrayList<Politician>()
    private val mFilteredSenadoresPreList = ArrayList<Politician>()
    private val mFilteredDeputadosMainList = ArrayList<Politician>()
    private val mFilteredDeputadosPreList = ArrayList<Politician>()

    private val mFinalMainListSenadoresPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mFinalPreListSenadoresPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mFinalMainListDeputadosPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mFinalPreListDeputadosPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

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
                        if (mListCounter == 4) {
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
        mSenadoresPreListObservable = mFirebaseRepository.getSenadoresPreList()
        mDeputadosMainListObservable = mFirebaseRepository.getDeputadosMainList()
        mDeputadosPreListObservable = mFirebaseRepository.getDeputadosPreList()

        getSenadoresMainList()
        getSenadoresPreList()
        getDeputadosMainList()
        getDeputadosPreList()
    }

    private fun getSenadoresMainList() {
        mSenadoresMainListObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onNext(deputadosPreList: ArrayList<Politician>) {
                        mSenadoresMainList = deputadosPreList
                        mOnListsReadyPublisher.onNext(true)
                    }
                })
    }

    private fun getSenadoresPreList() {
        mSenadoresPreListObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onNext(deputadosPreList: ArrayList<Politician>) {
                        mSenadoresPreList = deputadosPreList
                        mOnListsReadyPublisher.onNext(true)
                    }
                })
    }

    private fun getDeputadosMainList() {
        mDeputadosMainListObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onNext(deputadosMainList: ArrayList<Politician>) {
                        mDeputadosMainList = deputadosMainList
                        mOnListsReadyPublisher.onNext(true)
                    }
                })
    }

    private fun getDeputadosPreList() {
        mDeputadosPreListObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                        mOnListsReadyPublisher.onNext(false)
                    }

                    override fun onNext(deputadosPreList: ArrayList<Politician>) {
                        mDeputadosPreList = deputadosPreList
                        mOnListsReadyPublisher.onNext(true)
                    }
                })
    }

    override fun initiateDataLoad() {
        if (loaderManager.getLoader<Cursor>(Constants.LOADER_ID) == null) {
            loaderManager.initLoader(Constants.LOADER_ID, null, this)

        } else {
            loaderManager.restartLoader(Constants.LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(context, politiciansEntry.CONTENT_URI, Constants.POLITICIANS_COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            for (i in 0..data.count - 1) {
                if (data.getString(Constants.COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name) {

                    val deputadoName = data.getString(Constants.COLUMNS_INDEX_NAME)
                    val deputadoEmail = data.getString(Constants.COLUMNS_INDEX_EMAIL)
                    val deputadoImage = data.getBlob(Constants.COLUMNS_INDEX_IMAGE)

                    searchForDeputadoOnList(deputadoName, deputadoEmail, deputadoImage)


                } else {
                    val senadorName = data.getString(Constants.COLUMNS_INDEX_NAME)
                    val senadorEmail = data.getString(Constants.COLUMNS_INDEX_EMAIL)
                    val senadorImage = data.getBlob(Constants.COLUMNS_INDEX_IMAGE)

                    searchForSenadorOnList(senadorName, senadorEmail, senadorImage)
                }
                data.moveToNext()
            }
            mFinalMainListDeputadosPublisher.onNext(mFilteredDeputadosMainList)
            mFinalPreListDeputadosPublisher.onNext(mFilteredDeputadosPreList)
            mFinalMainListSenadoresPublisher.onNext(mFilteredSenadoresMainList)
            mFinalPreListSenadoresPublisher.onNext(mFilteredSenadoresPreList)
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

            try {
                mDeputadosPreList.
                        first { preListDeputado -> preListDeputado.name == deputadoName }
                        .also { preListDeputado ->
                            preListDeputado.post = Politician.Post.DEPUTADO
                            preListDeputado.email = deputadoEmail
                            preListDeputado.image = deputadoImage
                            mFilteredDeputadosPreList.add(preListDeputado)
                        }

            } catch (noSuchElement: NoSuchElementException) {

            }
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

            try {
                mSenadoresPreList.first { preListSenador -> preListSenador.name == senadorName }
                        .also { preListSenador ->
                            preListSenador.post = Politician.Post.SENADOR
                            preListSenador.email = senadorEmail
                            preListSenador.image = senadorImage
                            mFilteredSenadoresPreList.add(preListSenador)
                        }

            } catch (noSuchElement: NoSuchElementException) {
                Log.d(LOG_TAG, "Deputado $senadorName not present on neither list")
            }
        }
    }

    override fun loadSenadoresMainList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalMainListSenadoresPublisher }
    }

    override fun loadSenadoresPreList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalPreListSenadoresPublisher }
    }

    override fun loadDeputadosMainList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalMainListDeputadosPublisher }
    }

    override fun loadDeputadosPreList(): Observable<ArrayList<Politician>> {
        return Observable.defer { mFinalPreListDeputadosPublisher }
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mFirebaseRepository.onDestroy()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}