package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.internal.observers.FutureSingleObserver

/**
 * Created by andre on 5/16/2017.
 */
class IndividualPoliticianSelectorModel(val mContext: Context, val mLoaderManager: LoaderManager) : PoliticianSelectorMvpContract.IndividualPoliticianModel, LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_CARGO = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE = 3

    private var mSinglePoliticianObservable = FutureSingleObserver<Politician>()

    override fun initiateSinglePoliticianLoad(politicianName: String) {
        val args = Bundle()
        args.putString(Constants.BUNDLE_POLITICIAN_NAME, politicianName)

        if (mLoaderManager.getLoader<Cursor>(Constants.LOADER_ID) == null) {
            mLoaderManager.initLoader(Constants.LOADER_ID, args, this)

        } else {
            mLoaderManager.restartLoader(Constants.LOADER_ID, args, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politicianName = args?.getString(Constants.BUNDLE_POLITICIAN_NAME)
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(mContext,
                politiciansEntry.CONTENT_URI,
                Constants.POLITICIANS_COLUMNS,
                "${politiciansEntry.COLUMN_NAME} = ?",
                arrayOf(politicianName),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            val politician : Politician
            if (data.getString(COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name) {
                val deputadoName = data.getString(COLUMNS_INDEX_NAME)
                val deputadoEmail = data.getString(COLUMNS_INDEX_EMAIL)
                val deputadoImage = data.getBlob(COLUMNS_INDEX_IMAGE)

                politician = Politician(Politician.Post.DEPUTADO, null, deputadoName, deputadoEmail, deputadoImage)

            } else {
                val senadorName = data.getString(COLUMNS_INDEX_NAME)
                val senadorEmail = data.getString(COLUMNS_INDEX_EMAIL)
                val senadorImage = data.getBlob(COLUMNS_INDEX_IMAGE)

                politician = Politician(Politician.Post.SENADOR, null, senadorName, senadorEmail, senadorImage)
            }

            mSinglePoliticianObservable.onSuccess(politician)
        }

        data?.close()
    }

    override fun loadSinglePoliticianObservable(): FutureSingleObserver<Politician> {
        return mSinglePoliticianObservable
    }

    override fun onDestroy() {

    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}