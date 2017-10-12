package com.andrehaueisen.listadejanot.d_search_politician.mvp

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
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/16/2017.
 */
class SinglePoliticianModel(private val mContext: Context,
                            private val mLoaderManager: LoaderManager,
                            val mFirebaseRepository: FirebaseRepository,
                            val mFirebaseAuthenticator: FirebaseAuthenticator,
                            private val mSelectorModel: PoliticianSelectorModel)

    : PoliticianSelectorMvpContract.IndividualPoliticianModel, LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_NAME = 1

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
                POLITICIANS_COLUMNS_NAME_NO_EMAIL,
                "${politiciansEntry.COLUMN_NAME} = ?",
                arrayOf(politicianName),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        with(data) {
            if (this != null && this.count != 0) {
                moveToFirst()

                val politicianPost = getString(COLUMNS_INDEX_POST)
                val politicianName = getString(COLUMNS_INDEX_NAME)

                val politician = mSelectorModel.getSearchablePoliticiansList().find { it.name == politicianName }
                        ?.also { politician ->

                            when (politicianPost) {
                                Politician.Post.DEPUTADO.name ->
                                    politician.post = Politician.Post.DEPUTADO

                                Politician.Post.DEPUTADA.name ->
                                    politician.post = Politician.Post.DEPUTADA

                                Politician.Post.SENADOR.name ->
                                    politician.post = Politician.Post.SENADOR

                                Politician.Post.SENADORA.name ->
                                    politician.post = Politician.Post.SENADORA

                                Politician.Post.GOVERNADOR.name ->
                                    politician.post = Politician.Post.GOVERNADOR

                                Politician.Post.GOVERNADORA.name ->
                                    politician.post = Politician.Post.GOVERNADORA
                            }
                        }

                if (politician != null) {
                    mSinglePoliticianPublisher.onNext(politician)
                }
            }
            this?.close()
        }
    }

    fun updateLists(listAction: ListAction, politician: Politician){
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleListChangeOnDatabase(listAction, politician, userEmail)
    }

    fun updateGrade(voteType: RatingBarType, outdatedGrade:Float, newGrade: Float, politician: Politician, user: User){
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleGradeChange(voteType, outdatedGrade, newGrade, politician, userEmail, user)
    }

    override fun loadSinglePoliticianPublisher(): PublishSubject<Politician> =
            mSinglePoliticianPublisher

    override fun onDestroy() {
        if (!mCompositeDisposable.isDisposed) mCompositeDisposable.dispose()
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) = Unit
}