package com.andrehaueisen.listadejanot.C_main_list.mvp

import android.os.Bundle
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable

/**
 * Created by andre on 4/20/2017.
 */
interface MainListMvpContract{

    interface View{
        fun setViews()
        fun notifySenadorAddition(senador: Politician)
        fun notifyDeputadoAddition(deputado: Politician)

        fun onSaveInstanceState():Bundle
    }

    interface SenadoresView {
        fun notifySenadorAddition(senador: Politician)
    }

    interface DeputadosView {
        fun notifyDeputadoAddition(deputado: Politician)
    }

    interface Model {
        fun initiateDataLoad()
        fun loadDeputadosData(): Observable<Politician>
        fun loadSenadoresData(): Observable<Politician>
    }

    interface Presenter {

        fun subscribeToModel()

        //ManipulateView

    }
}