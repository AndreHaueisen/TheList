package com.andrehaueisen.listadejanot.C_main_list.mvp

import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable

/**
 * Created by andre on 4/20/2017.
 */
interface MainListMvpContract{

    interface View {
        fun setViews()

        fun notifyDeputadoAddition(deputado: Politician)
        fun notifySenadorAddition(senador: Politician)
    }

    interface Model {
        fun initiateDataLoad()
        fun loadDeputadosData() : Observable<Politician>
        fun loadSenadoresData() : Observable<Politician>
    }

    interface Presenter {

        fun subscribeToModel()

        //ManipulateView



    }
}