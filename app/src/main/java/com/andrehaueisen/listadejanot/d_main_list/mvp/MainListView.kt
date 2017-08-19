package com.andrehaueisen.listadejanot.d_main_list.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_main_list.PoliticiansPagesAdapter
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.f_information.mvp.InformationPresenterActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import kotlinx.android.synthetic.main.d_activity_main_list.*


/**
 * Created by andre on 4/20/2017.
 */
class MainListView(val mPresenterActivity: MainListPresenterActivity) : MainListMvpContract.View {

    private var mBundle: Bundle? = null
    private var mMainListSenadores: ArrayList<Politician>? = null
    private var mMainListDeputados: ArrayList<Politician>? = null
    private var mMainListGovernadores: ArrayList<Politician>? = null

    init {
        mPresenterActivity.setContentView(R.layout.d_activity_main_list)
    }

    constructor(presenterActivity: MainListPresenterActivity, bundle: Bundle) : this(presenterActivity) {
        mBundle = bundle
    }

    override fun setViews() {
        setToolbar()
        setPagerAdapter()
        setBottomNavigationView()

        with(mPresenterActivity) {
            if (!isConnectedToInternet()) {
                parent_coordinator_layout.showSnackbar(getString(R.string.no_network))
            }
        }
    }

    private fun setToolbar() {
        val toolbar = mPresenterActivity.toolbar
        mPresenterActivity.setSupportActionBar(toolbar)
        mPresenterActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setPagerAdapter() {
        mPresenterActivity.politicians_pager_adapter.adapter = PoliticiansPagesAdapter(mPresenterActivity.supportFragmentManager)
        mPresenterActivity.politicians_pager_adapter.offscreenPageLimit = 2
        mPresenterActivity.politicians_pager_adapter.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mPresenterActivity.navigation.selectedItemId = R.id.navigation_senadores
                        mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_senadores_title)
                    }
                    1 -> {
                        mPresenterActivity.navigation.selectedItemId = R.id.navigation_deputados
                        mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_deputados_title)
                    }
                    2 -> {
                        mPresenterActivity.navigation.selectedItemId = R.id.navigation_governadores
                        mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_governadores_title)
                    }
                }
            }
        })
        if (mBundle != null) {
            mPresenterActivity.politicians_pager_adapter.onRestoreInstanceState(mBundle?.getParcelable(BUNDLE_PAGER_ADAPTER))
        }
    }

    private fun setBottomNavigationView() = mPresenterActivity.navigation.setOnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {

            R.id.navigation_senadores -> {
                mPresenterActivity.politicians_pager_adapter.setCurrentItem(0, true)
                mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_senadores_title)
                return@setOnNavigationItemSelectedListener true
            }

            R.id.navigation_deputados -> {
                mPresenterActivity.politicians_pager_adapter.setCurrentItem(1, true)
                mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_deputados_title)
                return@setOnNavigationItemSelectedListener true
            }

            R.id.navigation_governadores ->{
                mPresenterActivity.politicians_pager_adapter.setCurrentItem(2, true)
                mPresenterActivity.toolbar_title.text = mPresenterActivity.getString(R.string.banned_governadores_title)
                return@setOnNavigationItemSelectedListener true
            }

            else -> false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?) = mPresenterActivity.menuInflater.inflate(R.menu.menu_main_list, menu)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.action_add_politician_to_main_list -> {
                val intent = Intent(mPresenterActivity, PoliticianSelectorPresenterActivity::class.java)

                mMainListDeputados?.let {
                    val noPicDeputadosList = ArrayList<Politician>()
                    it.forEach {
                        val deputado = Politician(it.post, it.name, it.email)
                        noPicDeputadosList.add(deputado)
                    }
                    intent.putParcelableArrayListExtra(INTENT_DEPUTADOS_MAIN_LIST, noPicDeputadosList)
                }

                mMainListSenadores?.let {
                    val noPicSenadoresList = ArrayList<Politician>()
                    it.forEach {
                        val senador = Politician(it.post, it.name, it.email)
                        noPicSenadoresList.add(senador)
                    }
                    intent.putParcelableArrayListExtra(INTENT_SENADORES_MAIN_LIST, noPicSenadoresList)
                }

                mMainListGovernadores?.let {
                    val noPicGovernadoresList = ArrayList<Politician>()
                     it.forEach {
                         val governador = Politician(it.post, it.name, it.email)
                         noPicGovernadoresList.add(governador)
                     }
                     intent.putParcelableArrayListExtra(INTENT_GOVERNADORES_MAIN_LIST, noPicGovernadoresList)
                }
                mPresenterActivity.startActivity(intent)
            }

            R.id.action_app_info -> {
                mPresenterActivity.startNewActivity(InformationPresenterActivity::class.java)
            }

            R.id.action_logout -> {
                mPresenterActivity.logUserOut()
            }
        }

        return true
    }

    override fun notifyDeputadosNewList(deputados: ArrayList<Politician>) {
        mMainListDeputados = deputados
        val deputadoFragment = (mPresenterActivity.politicians_pager_adapter.adapter as PoliticiansPagesAdapter).getItem(1)
        (deputadoFragment as MainListDeputadosView).notifyDeputadosNewList(deputados)
    }

    override fun notifySenadoresNewList(senadores: ArrayList<Politician>) {
        mMainListSenadores = senadores
        val senadorFragment = (mPresenterActivity.politicians_pager_adapter.adapter as PoliticiansPagesAdapter).getItem(0)
        (senadorFragment as MainListSenadoresView).notifySenadoresNewList(senadores)
    }

    override fun notifyGovernadoresNewList(governadores: ArrayList<Politician>){
        mMainListGovernadores = governadores
        val governadorFragment = (mPresenterActivity.politicians_pager_adapter.adapter as PoliticiansPagesAdapter).getItem(2)
        (governadorFragment as MainListGovernadoresView).notifyGovernadoresNewList(governadores)
    }

    override fun onSaveInstanceState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(BUNDLE_PAGER_ADAPTER, mPresenterActivity.politicians_pager_adapter.onSaveInstanceState())
        return bundle
    }
}