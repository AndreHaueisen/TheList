package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/16/2017.
 */
class IndividualPoliticianSelectorModel(val mContext: Context, val mLoaderManager: LoaderManager) : PoliticianSelectorMvpContract.IndividualPoliticianModel, LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_NAME = 0
    private val COLUMNS_INDEX_IMAGE = 1

    private var mSinglePoliticianPublisher : PublishSubject<Pair<String, ByteArray>> = PublishSubject.create()

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
                Constants.POLITICIANS_COLUMNS_IMAGE,
                "${politiciansEntry.COLUMN_NAME} = ?",
                arrayOf(politicianName),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            val politicianName = data.getString(COLUMNS_INDEX_NAME)
            val politicianImage = data.getBlob(COLUMNS_INDEX_IMAGE)

            val pairPoliticianNameImage = Pair(politicianName, politicianImage)

            mSinglePoliticianPublisher.onNext(pairPoliticianNameImage)
                    //.onSuccess(pairPoliticianNameImage)


        }

        data?.close()
    }

    override fun loadSinglePoliticianObservable(): Observable<Pair<String, ByteArray>> {
        return Observable.defer { mSinglePoliticianPublisher }
    }

    override fun onDestroy() {

    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}