package com.andrehaueisen.listadejanot.D_main_list.mvp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable

/**
 * Created by andre on 4/20/2017.
 */
interface MainListMvpContract{

    interface View{
        fun setViews()
        fun notifySenadoresNewList(senadores: ArrayList<Politician>)
        fun notifyDeputadosNewList(deputados: ArrayList<Politician>)

        fun onCreateOptionsMenu(menu: Menu?)
        fun onOptionsItemSelected(item: MenuItem?): Boolean

        fun onSaveInstanceState():Bundle
    }

    interface SenadoresView {
        fun notifySenadoresNewList(senadores: ArrayList<Politician>)
    }

    interface DeputadosView {
        fun notifyDeputadosNewList(deputados: ArrayList<Politician>)
    }

    interface Presenter {

        fun subscribeToModel()
    }

    interface Model {
        fun initiateDataLoad()
        fun loadSenadoresMainList(): Observable<ArrayList<Politician>>
        fun loadSenadoresPreList(): Observable<ArrayList<Politician>>
        fun loadDeputadosMainList(): Observable<ArrayList<Politician>>
        fun loadDeputadosPreList(): Observable<ArrayList<Politician>>

        fun onDestroy()
    }

    interface Repository{

    }


}