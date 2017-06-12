package com.andrehaueisen.listadejanot.e_search_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.BUNDLE_POLITICIAN_NAME
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS_NAME_IMAGE
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/16/2017.
 */
class SinglePoliticianModel(val mContext: Context,
                            val mLoaderManager: LoaderManager,
                            val mFirebaseRepository: FirebaseRepository,
                            val mFirebaseAuthenticator: FirebaseAuthenticator,
                            val mSelectorModel: PoliticianSelectorModel)

    : PoliticianSelectorMvpContract.IndividualPoliticianModel, LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_NAME = 0
    private val COLUMNS_INDEX_IMAGE = 1

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
                POLITICIANS_COLUMNS_NAME_IMAGE,
                "${politiciansEntry.COLUMN_NAME} = ?",
                arrayOf(politicianName),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        with(data) {
            if (this != null && this.count != 0) {
                moveToFirst()

                val politicianName = getString(COLUMNS_INDEX_NAME)
                val politicianImage = getBlob(COLUMNS_INDEX_IMAGE)

                val politician = mSelectorModel.getSearchablePoliticiansList().find { it.name == politicianName }?.also { it.image = politicianImage }

                if (politician != null) {
                    mSinglePoliticianPublisher.onNext(politician)
                }
            }
            this?.close()
        }

    }

    override fun updatePoliticianVote(politician: Politician, view: PoliticianSelectorMvpContract.View) {
        val userEmail = mFirebaseAuthenticator.getUserEmail()

        userEmail?.let {
            if (politician.post.name == Politician.Post.SENADOR.name) {
                mFirebaseRepository.updateSenadorVoteOnBothLists(politician, it, null, view)
            } else {
                mFirebaseRepository.updateDeputadoVoteOnBothLists(politician, it, null, view)
            }
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