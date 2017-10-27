package com.andrehaueisen.listadejanot.i_main_lists.mvp

import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.SortType
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 10/23/2017.
 */
class MainListsModel(
        private val mDeputadosList: ArrayList<Politician>,
        private val mSenadoresList: ArrayList<Politician>,
        private val mGovernadoresList: ArrayList<Politician>) {

    private val mDeputadosPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mSenadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mGovernadoresPublishSubject: PublishSubject<ArrayList<Politician>> = PublishSubject.create()

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
                //.forEach { politician ->  mSenadoresPublishSubject.onNext(politician) }

        mGovernadoresPublishSubject.onNext(ArrayList(mGovernadoresList
                .asSequence()
                .filter(filter)
                .sortedWith(comparator)
                .take(10).toList()))
                //.forEach { politician ->  mGovernadoresPublishSubject.onNext(politician) }

        mDeputadosPublishSubject.onNext(ArrayList(mDeputadosList
                .asSequence()
                .filter(filter)
                .sortedWith(comparator)
                .take(10).toList()))
                //.forEach { politician ->  mDeputadosPublishSubject.onNext(politician) }
    }

    fun subscribeToDeputados() = mDeputadosPublishSubject
    fun subscribeToSenadores() = mSenadoresPublishSubject
    fun subscribeToGovernadores() = mGovernadoresPublishSubject

}