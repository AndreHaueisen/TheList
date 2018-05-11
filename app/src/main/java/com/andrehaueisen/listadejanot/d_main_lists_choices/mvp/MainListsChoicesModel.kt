package com.andrehaueisen.listadejanot.d_main_lists_choices.mvp

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.util.Log
import android.util.SparseArray
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.LOADER_ID
import com.andrehaueisen.listadejanot.utilities.POLITICIANS_COLUMNS
import com.andrehaueisen.listadejanot.utilities.SortType
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 10/23/2017.
 */
class MainListsChoicesModel(
        private val mDeputadosList: ArrayList<Politician>,
        private val mSenadoresList: ArrayList<Politician>,
        private val mGovernadoresList: ArrayList<Politician>,
        private val mPresidentesList: ArrayList<Politician>,
        private val mMediaHighlights: ArrayList<String>,
        private val mLoaderManager: LoaderManager,
        private val mContext: Context,
        mListReadyPublishSubject: PublishSubject<Boolean>) : LoaderManager.LoaderCallbacks<Cursor> {

    private val COLUMNS_INDEX_ID = 0
    private val COLUMNS_INDEX_POST = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE_URL = 3
    private val COLUMNS_INDEX_IMAGE_TIMESTAMP = 4

    private val mDeputadosPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mSenadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mGovernadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mPresidentesPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

    private val mPoliticiansLoadingStatusPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val mPoliticiansListMapPublishSubject: PublishSubject<SparseArray<ArrayList<Politician>>> = PublishSubject.create()
    private val mListObservable = Observable.zip(
            mSenadoresPublishSubject,
            mGovernadoresPublishSubject,
            mDeputadosPublishSubject,
            mPresidentesPublishSubject,
            Function4<ArrayList<Politician>,
                    ArrayList<Politician>,
                    ArrayList<Politician>,
                    ArrayList<Politician>,
                    SparseArray<ArrayList<Politician>>> { senadores, governadores, deputados, presidentes ->
                val politiciansArray = SparseArray<ArrayList<Politician>>(4)
                politiciansArray.put(0, senadores)
                politiciansArray.put(1, governadores)
                politiciansArray.put(2, deputados)
                politiciansArray.put(4, presidentes)

                politiciansArray
            })

    private val mCompositeDisposable = CompositeDisposable()

    private var mListReadyCounter = 0
    private val mListReadyObserver = object : Observer<Boolean> {
        override fun onNext(isListReady: Boolean) {
            if (isListReady) mListReadyCounter++
            if (mListReadyCounter % 4 == 0)
                initiateDataLoad()
        }

        override fun onSubscribe(d: Disposable) {}
        override fun onComplete() {}
        override fun onError(e: Throwable) {}
    }

    private val mListMapObserver = object : Observer<SparseArray<ArrayList<Politician>>> {
        override fun onNext(politiciansMap: SparseArray<ArrayList<Politician>>) = mPoliticiansListMapPublishSubject.onNext(politiciansMap)

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onComplete() {}
        override fun onError(e: Throwable) {}
    }


    init {
        mListReadyPublishSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListReadyObserver)

        mListObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListMapObserver)
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

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        if (data != null && !data.isClosed && data.count != 0) {
            data.moveToFirst()

            for (i in 0 until data.count) {
                val id = data.getLong(COLUMNS_INDEX_ID)
                val politicianEmail = data.getString(COLUMNS_INDEX_EMAIL)
                val imageUrl: String? = data.getString(COLUMNS_INDEX_IMAGE_URL)
                val imageTimestamp: String? = data.getString(COLUMNS_INDEX_IMAGE_TIMESTAMP)

                val politicianPair: Pair<Politician, Politician.Post> = when (data.getString(COLUMNS_INDEX_POST)) {
                    Politician.Post.DEPUTADO.name ->
                        Pair(mDeputadosList.first { deputado -> deputado.email == politicianEmail }, Politician.Post.DEPUTADO)

                    Politician.Post.DEPUTADA.name ->
                        Pair(mDeputadosList.first { deputada -> deputada.email == politicianEmail }, Politician.Post.DEPUTADA)

                    Politician.Post.SENADOR.name ->
                        Pair(mSenadoresList.first { senador -> senador.email == politicianEmail }, Politician.Post.SENADOR)

                    Politician.Post.SENADORA.name ->
                        Pair(mSenadoresList.first { senadora -> senadora.email == politicianEmail }, Politician.Post.SENADORA)

                    Politician.Post.GOVERNADOR.name ->
                        Pair(mGovernadoresList.first { governador -> governador.email == politicianEmail }, Politician.Post.GOVERNADOR)

                    Politician.Post.GOVERNADORA.name ->
                        Pair(mGovernadoresList.first { governadora -> governadora.email == politicianEmail }, Politician.Post.GOVERNADORA)

                    Politician.Post.PRESIDENTE.name ->
                        Pair(mPresidentesList.first { presidente -> presidente.email == politicianEmail }, Politician.Post.PRESIDENTE)

                    else -> {
                        Log.e("MainListsChoicesModel", "Else block reached. Politician not found!")
                        Pair(mPresidentesList.first { presidente -> presidente.email == politicianEmail }, Politician.Post.PRESIDENTE)
                    }
                }

                val politician = politicianPair.first
                politician.sqlId = id
                politician.post = politicianPair.second
                politician.imageUrl = imageUrl
                politician.imageTimestamp = imageTimestamp

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
            SortType.TOP_OVERALL_GRADE -> Comparator { politician_1, politician_2 -> ((politician_2.overallGrade - politician_1.overallGrade) * 100).toInt() }
            SortType.WORST_OVERALL_GRADE -> Comparator { politician_1, politician_2 -> ((politician_1.overallGrade - politician_2.overallGrade) * 100).toInt() }
            SortType.MEDIA_HIGHLIGHT -> null

        }

        when (sortType) {
            SortType.RECOMMENDATIONS_COUNT ->
                emitPoliticians({ politician -> politician.recommendationsCount >= countFilterThreshold }, comparator)

            SortType.CONDEMNATIONS_COUNT ->
                emitPoliticians({ politician -> politician.condemnationsCount >= countFilterThreshold }, comparator)

            SortType.TOP_OVERALL_GRADE ->
                emitPoliticians({ politician -> politician.overallGrade != gradeFilterThreshold }, comparator)

            SortType.WORST_OVERALL_GRADE ->
                emitPoliticians({ politician -> politician.overallGrade != gradeFilterThreshold }, comparator)

            SortType.MEDIA_HIGHLIGHT ->
                emitPoliticians({ politician -> politician.email in mMediaHighlights }, comparator)
        }
    }

    private fun emitPoliticians(filter: (Politician) -> Boolean, comparator: Comparator<Politician>?) {

        if (comparator != null) {
            mSenadoresPublishSubject.onNext(ArrayList(mSenadoresList
                    .asSequence()
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

            mPresidentesPublishSubject.onNext(ArrayList(mPresidentesList
                    .asSequence()
                    .sortedWith(comparator)
                    .take(4).toList()))

        } else {

            mSenadoresPublishSubject.onNext(ArrayList(mSenadoresList
                    .asSequence()
                    .filter(filter)
                    .toList()))

            mGovernadoresPublishSubject.onNext(ArrayList(mGovernadoresList
                    .asSequence()
                    .filter(filter)
                    .toList()))

            mDeputadosPublishSubject.onNext(ArrayList(mDeputadosList
                    .asSequence()
                    .filter(filter)
                    .toList()))

            mPresidentesPublishSubject.onNext(ArrayList(mPresidentesList
                    .asSequence()
                    .filter(filter)
                    .toList()))
        }

    }

    fun subscribeToPoliticiansLoadingStatus() = mPoliticiansLoadingStatusPublishSubject
    fun subscribeToPoliticiansListsMap() = mPoliticiansListMapPublishSubject

    fun onDestroy() {
        mDeputadosPublishSubject.onComplete()
        mSenadoresPublishSubject.onComplete()
        mGovernadoresPublishSubject.onComplete()
        mPresidentesPublishSubject.onComplete()
        mPoliticiansLoadingStatusPublishSubject.onComplete()
        mCompositeDisposable.dispose()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) = Unit
}