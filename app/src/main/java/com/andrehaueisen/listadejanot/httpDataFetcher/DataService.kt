package com.andrehaueisen.listadejanot.httpDataFetcher


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Patterns
import com.andrehaueisen.listadejanot.database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.Observable
import io.reactivex.Single
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.MalformedURLException
import java.util.concurrent.Callable


/**
 * Created by andre on 4/15/2017.
 */
class DataService(val mJsoupConnection: Connection, val context: Context) {

    val LOG_TAG : String = DataService::class.java.simpleName

    lateinit var mPoliticiansURL: ArrayList<Uri>
    var senadorCounter = 0
    var deputadoCounter = 0

    private fun getResponse(): Observable<Connection.Response> {
        return Observable.fromCallable(object : Callable<Connection.Response> {
            override fun call(): Connection.Response {
                return mJsoupConnection.execute()
            }
        })
    }

    private fun getPoliticiansUrl(): Observable<Uri> {
        return getResponse().flatMap { it ->
            var document = it.parse()
            val tables = document.getElementById("parlamentarNome").getElementsByTag("tbody")
            document = null

            mPoliticiansURL = ArrayList<Uri>()
            for (table in tables) {
                mPoliticiansURL.addAll(table.select("a").map { Uri.parse(it.attr("href").toString()) })
            }

            Log.i(LOG_TAG, "Requesting politician pages number: ${mPoliticiansURL.size}")
            Observable.fromIterable(mPoliticiansURL)
        }
    }

    private fun getIndividualPoliticianData(): Observable<Politician> {
        return getPoliticiansUrl().flatMap { politicianUri ->

            if (politicianUri.authority == "www25.senado.leg.br") {

                try {
                    Observable.fromCallable { mJsoupConnection.url(politicianUri.toString()).execute() }
                            .flatMap { response ->
                                val document = response.parse()
                                val personalInfoBox = document.getElementsByClass("body")[0]
                                val imageUrl = personalInfoBox.getElementsByTag("img")[0].attr("src")
                                val descriptionList = personalInfoBox.getElementsByTag("dl")[0]
                                val name = descriptionList.getElementsByTag("dd")[0].text()
                                var email: String? = null
                                val searchForEmailElement = descriptionList.getElementsByAttributeValueMatching("href", Patterns.EMAIL_ADDRESS)
                                if (searchForEmailElement.isNotEmpty()) {
                                    email = descriptionList.getElementsByAttributeValueMatching("href", Patterns.EMAIL_ADDRESS)[0].text()
                                }

                                val politician = Politician(Politician.Post.SENADOR, imageUrl, name, email)
                                senadorCounter++
                                Log.i(LOG_TAG, "Adding $politician to buffer. Senador N: $senadorCounter")
                                Observable.just(politician)
                            }
                }catch (eofe: EOFException){
                    Log.e(LOG_TAG, "End of stream on Senador: $eofe")
                    Observable.empty<Politician>()
                }catch (hse: HttpStatusException){
                    Log.e(LOG_TAG, "Status exception Senador: $hse")
                    Observable.empty<Politician>()
                }

            } else {

                try {
                    Observable.fromCallable { mJsoupConnection.url(politicianUri.toString()).execute() }
                            .flatMap { response ->
                                val document = response.parse()
                                val personalInfoBox = document.getElementById("content")
                                val imageUrl = personalInfoBox.getElementsByTag("img")[0].attr("src")
                                val name = personalInfoBox.getElementsByTag("ul")[0].getElementsByTag("li")[0].text().substringAfter(':')

                                val politician = Politician(Politician.Post.DEPUTADO, imageUrl, name)
                                deputadoCounter++
                                Log.i(LOG_TAG, "Adding $politician to buffer. Deputado N: $deputadoCounter")

                                Observable.just(politician)
                            }
                }catch (eofe: EOFException){
                    Log.e(LOG_TAG, "End of stream on Deputado: $eofe")
                    Observable.empty<Politician>()
                }catch (hse: HttpStatusException){
                    Log.e(LOG_TAG, "Status exception Deputado: $hse")
                    Observable.empty<Politician>()
                }

            }
        }
    }

    fun savePoliticiansToDatabase() : Single<Boolean> {
        return getIndividualPoliticianData()
                .buffer(594)
                .any { politiciansList ->
                    val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
                    val contentValuesList = ArrayList<ContentValues>()

                    Log.i(LOG_TAG, "Politicians list size after buffer: ${politiciansList.size}")

                    politiciansList.map {
                        val imageBytes = getImageBytes(it.imageUrl, it.cargo)
                        val contentValues = ContentValues()
                        try {
                            contentValues.put(politiciansEntry.COLUMN_CARGO, it.cargo.name)
                            contentValues.put(politiciansEntry.COLUMN_IMAGE_URL, it.imageUrl)
                            contentValues.put(politiciansEntry.COLUMN_NAME, it.name)
                            contentValues.put(politiciansEntry.COLUMN_EMAIL, it.email)
                            contentValues.put(politiciansEntry.COLUMN_IMAGE, imageBytes)

                        }catch (mURLe : MalformedURLException) {
                            Log.e(LOG_TAG, mURLe.message)
                        } catch (ioe: IOException) {
                            Log.e(LOG_TAG, ioe.message)
                        }
                        Log.i(LOG_TAG, "CV added to array: $contentValues")
                        contentValuesList.add(contentValues)
                    }

                    val contentResolver = context.contentResolver
                    contentResolver.delete(politiciansEntry.CONTENT_URI, null, null)

                    val newRows = contentResolver.bulkInsert(politiciansEntry.CONTENT_URI, contentValuesList.toTypedArray())

                    (newRows == 594)
                }
    }

    @Throws(IOException::class)
    private fun getImageBytes(imageUrl: String, post: Politician.Post) : ByteArray{

        val bitmap : Bitmap
                                                                                        //skip cache on disk since we are persisting it to the database
        val glide = Glide.with(context).load(imageUrl).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)

        if(post == Politician.Post.DEPUTADO) {
            bitmap = glide.into(150, 195).get()

        }else{
            bitmap = glide.into(300, 360).get()
        }

        val byteArrayOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOS)

        return byteArrayOS.toByteArray()
    }
}