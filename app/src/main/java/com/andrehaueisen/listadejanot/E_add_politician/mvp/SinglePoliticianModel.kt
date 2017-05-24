package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_POLITICIAN_NAME
import com.andrehaueisen.listadejanot.utilities.FAKE_USER_EMAIL
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/16/2017.
 */
class SinglePoliticianModel(val mContext: Context, val mLoaderManager: LoaderManager, val mFirebaseRepository: FirebaseRepository)
    : PoliticianSelectorMvpContract.IndividualPoliticianModel, LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE = 3

    private var mSinglePoliticianPublisher: PublishSubject<Politician> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()

    override fun initiateSinglePoliticianLoad(politicianName: String) {

        val args = Bundle()
        args.putString(BUNDLE_POLITICIAN_NAME, politicianName)

        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, args, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, args, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politicianName = args?.getString(BUNDLE_POLITICIAN_NAME)
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(mContext,
                politiciansEntry.CONTENT_URI,
                POLITICIANS_COLUMNS,
                "${politiciansEntry.COLUMN_NAME} = ?",
                arrayOf(politicianName),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            val politicianPost = data.getString(COLUMNS_INDEX_POST)
            val politicianName = data.getString(COLUMNS_INDEX_NAME)
            val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)
            val politicianImage = data.getBlob(COLUMNS_INDEX_IMAGE)

            val politician: Politician
            if (politicianPost == Politician.Post.DEPUTADO.name) {
                politician = Politician(Politician.Post.DEPUTADO, null, politicianName, politicianEmail, politicianImage)
                deputadoMergeDatabaseWithFirebase(politician)

            } else {
                politician = Politician(Politician.Post.SENADOR, null, politicianName, politicianEmail, politicianImage)
                senadorMergeDatabaseWithFirebase(politician)
            }
        }
        data?.close()
    }

    private fun deputadoMergeDatabaseWithFirebase(deputado: Politician) {

        mFirebaseRepository.getDeputadosPreList()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {

                    override fun onSuccess(deputadosList: ArrayList<Politician>?) {

                        deputadosList?.firstOrNull { it.name == deputado.name }
                                .also {
                                    if(it != null) deputado.condemnedBy = it.condemnedBy
                                    deputado.votesNumber = it?.votesNumber ?: 0
                                }

                        mSinglePoliticianPublisher.onNext(deputado)

                    }

                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {

                    }
                })

    }

    private fun senadorMergeDatabaseWithFirebase(senador: Politician) {
        mFirebaseRepository.getSenadoresPreList()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {

                    override fun onSuccess(senadoresList: ArrayList<Politician>) {

                        senadoresList.firstOrNull { it.name == senador.name }
                                .also {
                                    if(it != null) senador.condemnedBy = it.condemnedBy
                                    senador.votesNumber = it?.votesNumber ?: 0
                                }

                        mSinglePoliticianPublisher.onNext(senador)
                    }

                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onError(e: Throwable?) {

                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun updatePoliticianVote(politician: Politician, view: PoliticianSelectorMvpContract.View) {
        if (politician.post.name == Politician.Post.SENADOR.name) {
            mFirebaseRepository.updateSenadorVoteOnPreList(politician, FAKE_USER_EMAIL, view)
        } else {
            mFirebaseRepository.updateDeputadoVoteOnPreList(politician, FAKE_USER_EMAIL, view)
        }
    }

    override fun loadSinglePoliticianPublisher(): PublishSubject<Politician> {
        return mSinglePoliticianPublisher
    }

    override fun onDestroy() {
        if (!mCompositeDisposable.isDisposed) mCompositeDisposable.dispose()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}