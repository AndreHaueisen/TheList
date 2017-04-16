package com.andrehaueisen.listadejanot.httpDataFetcher


import android.net.Uri
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable
import org.jsoup.Connection
import java.util.concurrent.Callable

/**
 * Created by andre on 4/15/2017.
 */
class DataService(val mJsoupConnection: Connection) {

    lateinit var mPoliticiansURL: ArrayList<Uri>

    var senadorCounter = 0
    var deputadoCounter = 0

    fun getResponse(): Observable<Connection.Response> {
        return Observable.fromCallable(object : Callable<Connection.Response> {
            override fun call(): Connection.Response {
                return mJsoupConnection.execute()
            }
        })
    }

    fun getPoliticiansUrl(): Observable<Uri> {
        return getResponse().flatMap { it ->
            var document = it.parse()
            val tables = document.getElementById("parlamentarNome").getElementsByTag("tbody")
            document = null

            mPoliticiansURL = ArrayList<Uri>()
            for (table in tables) {
                mPoliticiansURL.addAll(table.select("a").map { Uri.parse(it.attr("href").toString()) })
            }

            Observable.fromIterable(mPoliticiansURL)
        }
    }

    fun getIndividualPoliticianData(): Observable<Politician> {
        return getPoliticiansUrl().flatMap { politicianUri ->

            if (politicianUri.authority == "www25.senado.leg.br") {

                Observable.fromCallable { mJsoupConnection.url(politicianUri.toString()).execute() }
                        .flatMap { response ->
                            val document = response.parse()
                            val personalInfoBox = document.getElementsByClass("body")[0]
                            val imageUrl = personalInfoBox.getElementsByTag("img")[0].attr("src")
                            val descriptionList = personalInfoBox.getElementsByTag("dl")[0]
                            val name = descriptionList.getElementsByTag("dd")[0].text()
                            val email = descriptionList.getElementsByTag("dd")[6].text()

                            Observable.just(Politician(Politician.Post.DEPUTADO, imageUrl, name, email))
                        }


                /*senadorCounter++
                Log.i("DataService", "Senador: $senadorCounter")*/

            } else {

                Observable.fromCallable { mJsoupConnection.url(politicianUri.toString()).execute() }
                        .flatMap { response ->
                            val document = response.parse()
                            val personalInfoBox = document.getElementById("content")
                            val imageUrl = personalInfoBox.getElementsByTag("img")[0].attr("src")
                            val name = personalInfoBox.getElementsByTag("ul")[0].getElementsByTag("li")[0].text().substringAfter(':')

                            Observable.just(Politician(Politician.Post.DEPUTADO, imageUrl, name))
                        }


                /*deputadoCounter++
                Log.i("DataService", "Deputado: $deputadoCounter")*/

            }
        }
    }
}