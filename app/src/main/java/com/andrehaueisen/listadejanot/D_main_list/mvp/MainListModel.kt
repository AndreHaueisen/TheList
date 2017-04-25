package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.C_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 4/21/2017.
 */
class MainListModel(val context: Context, val loaderManager: LoaderManager) :
        MainListMvpContract.Model,
        LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var mDeputadoList: ArrayList<Politician>
    private lateinit var mSenadorList : ArrayList<Politician>

    val deputadosObservable: PublishSubject<Politician> = PublishSubject.create()
    val senadoresObservable: PublishSubject<Politician> = PublishSubject.create()

    init {
        initiateDataLoad()
    }

    override fun initiateDataLoad() {
        if(loaderManager.getLoader<Cursor>(Constants.LOADER_ID) == null){
            loaderManager.initLoader(Constants.LOADER_ID, null, this)

        }else{
            loaderManager.restartLoader(Constants.LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(context, politiciansEntry.CONTENT_URI, Constants.POLITICIANS_COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?){
        mDeputadoList = ArrayList()
        mSenadorList = ArrayList()

        if(data != null && data.count != 0){
            data.moveToFirst()

            for( i in 0..data.count - 1){
                if(data.getString(Constants.COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name){

                    val deputadoName = data.getString(Constants.COLUMNS_INDEX_NAME)
                    val deputadoImage = data.getBlob(Constants.COLUMNS_INDEX_IMAGE)

                    val deputado = Politician(Politician.Post.DEPUTADO, null, deputadoName, null, deputadoImage)
                    mDeputadoList.add(deputado)
                    deputadosObservable.onNext(deputado)

                }else{
                    val senadorName = data.getString(Constants.COLUMNS_INDEX_NAME)
                    val senadorEmail = data.getString(Constants.COLUMNS_INDEX_EMAIL)
                    val senadorImage = data.getBlob(Constants.COLUMNS_INDEX_IMAGE)

                    val senador = Politician(Politician.Post.SENADOR, null, senadorName, senadorEmail, senadorImage)
                    mSenadorList.add(senador)
                    senadoresObservable.onNext(senador)
                }

                data.moveToNext()
            }
            data.close()
        }
    }

    override fun loadDeputadosData(): Observable<Politician> {
        return Observable.defer { deputadosObservable }
    }

    override fun loadSenadoresData(): Observable<Politician> {
        return Observable.defer { senadoresObservable }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }
}