package com.andrehaueisen.listadejanot.i_main_lists.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS
import com.andrehaueisen.listadejanot.utilities.SortType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 10/23/2017.
 */
class MainListsModel(
        private val mDeputadosList: ArrayList<Politician>,
        private val mSenadoresList: ArrayList<Politician>,
        private val mGovernadoresList: ArrayList<Politician>,
        private val mLoaderManager: LoaderManager,
        private val mContext: Context,
        mListReadyPublishSubject: PublishSubject<Boolean>):  LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_POST = 0
    private val COLUMNS_INDEX_EMAIL = 1

    private val mDeputadosPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mSenadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mGovernadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

    private val mPoliticiansLoadingStatusPublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var mListReadyCounter = 0

    init {
        mListReadyPublishSubject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ isListReady ->
                    if (isListReady) mListReadyCounter++
                    if(mListReadyCounter % 3 == 0)
                        initiateDataLoad()
                })
    }

    private fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        return CursorLoader(mContext, politiciansEntry.CONTENT_URI, POLITICIANS_COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            for (i in 0 until data.count) {
                val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)

                when (data.getString(COLUMNS_INDEX_POST)) {
                    Politician.Post.DEPUTADO.name ->
                        mDeputadosList.first { deputado -> deputado.email == politicianEmail }.post = Politician.Post.DEPUTADO

                    Politician.Post.DEPUTADA.name ->
                        mDeputadosList.first { deputada -> deputada.email == politicianEmail }.post = Politician.Post.DEPUTADA

                    Politician.Post.SENADOR.name ->
                        mSenadoresList.first { senador -> senador.email == politicianEmail }.post = Politician.Post.SENADOR

                    Politician.Post.SENADORA.name ->
                        mSenadoresList.first { senadora -> senadora.email == politicianEmail }.post = Politician.Post.SENADORA

                    Politician.Post.GOVERNADOR.name ->
                        mGovernadoresList.first { governador -> governador.email == politicianEmail }.post = Politician.Post.GOVERNADOR

                    Politician.Post.GOVERNADORA.name ->
                        mGovernadoresList.first { governadora -> governadora.email == politicianEmail }.post = Politician.Post.GOVERNADORA
                }

                data.moveToNext()
            }
            mPoliticiansLoadingStatusPublishSubject.onNext(true)
            data.close()
        }
    }

    fun sortPoliticiansList(sortType: SortType, countFilterThreshold: Int = 1) {

        val gradeFilterThreshold = -1F

        val comparator = when (sortType) {

            SortType.RECOMMENDATIONS_COUNT -> Comparator<Politician> { politician_1, politician_2 -> (politician_2.recommendationsCount - politician_1.recommendationsCount) }
            SortType.CONDEMNATIONS_COUNT -> Comparator { politician_1, politician_2 -> (politician_2.condemnationsCount - politician_1.condemnationsCount) }
            SortType.OVERALL_GRADE -> Comparator { politician_1, politician_2 -> (politician_2.overallGrade - politician_1.overallGrade).toInt() }

        }

        when (sortType){
            SortType.RECOMMENDATIONS_COUNT ->
                emitPoliticians( {politician -> politician.recommendationsCount >= countFilterThreshold}, comparator)

            SortType.CONDEMNATIONS_COUNT ->
                emitPoliticians( {politician -> politician.condemnationsCount >= countFilterThreshold}, comparator)

            SortType.OVERALL_GRADE ->
                emitPoliticians( {politician -> politician.overallGrade != gradeFilterThreshold}, comparator)
        }
    }

    private fun emitPoliticians(filter: (Politician) -> Boolean, comparator: Comparator<Politician>){

        mSenadoresPublishSubject.onNext( ArrayList(
                mSenadoresList.asSequence()
                .filter(filter)
                .sortedWith(comparator)
                .take(10).toList()))

        mGovernadoresPublishSubject.onNext(ArrayList(mGovernadoresList
                .asSequence()
                .filter(filter)
                .sortedWith(comparator)
                .take(10).toList()))

        mDeputadosPublishSubject.onNext(ArrayList(mDeputadosList
                .asSequence()
                .filter(filter)
                .sortedWith(comparator)
                .take(10).toList()))
    }

    fun subscribeToPoliticiansLoadingStatus() = mPoliticiansLoadingStatusPublishSubject
    fun subscribeToDeputados() = mDeputadosPublishSubject
    fun subscribeToSenadores() = mSenadoresPublishSubject
    fun subscribeToGovernadores() = mGovernadoresPublishSubject

    fun onDestroy(){
        mDeputadosPublishSubject.onComplete()
        mSenadoresPublishSubject.onComplete()
        mGovernadoresPublishSubject.onComplete()
        mPoliticiansLoadingStatusPublishSubject.onComplete()
    }
    override fun onLoaderReset(loader: Loader<Cursor>?) = Unit
}