package com.andrehaueisen.listadejanot.images

import android.content.ContentResolver
import android.content.ContentValues
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Item
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function

/**
 * Created by andre on 9/30/2017.
 */
class ImageFetcherModel(private val mImageFetcherService: ImageFetcherService, private val contentResolver: ContentResolver) {

    fun getPoliticianImages(politician: Politician): Observable<Item> {
        return getUrlFromDatabase(politician)
                .switchIfEmpty(getUrlFromNetwork(politician))
                .flatMap(object : Function<Item, ObservableSource<Item>> {
                    override fun apply(item: Item): ObservableSource<Item> = Observable.just(item)
                })
    }

    private fun getUrlFromDatabase(politician: Politician): Observable<Item> {
        val item = Item()

        return if (politician.imageUrl != null && politician.imageTimestamp != null && isUpToDate(politician.imageTimestamp!!)) {
            item.link = politician.imageUrl
            Observable.just(item)
        } else {
            Observable.empty()
        }
    }

    private fun isUpToDate(imageTimestamp: String): Boolean {
        val twoMonthsInMilliseconds = 5256000000L
        return System.currentTimeMillis() - imageTimestamp.toLong()  < twoMonthsInMilliseconds
    }

    private fun getUrlFromNetwork(politician: Politician): Observable<Item> {

        return mImageFetcherService.catchNews("${politician.name} ${politician.post!!.name}")
                .flatMap({ items ->

                    if (items.items?.size != 0) {
                        updateDatabase(politician, items.items?.get(0)!!)
                    }

                    Observable.fromIterable(items.items)
                })
    }

    private fun updateDatabase(politician: Politician, item: Item) {

        val politicianEntry = PoliticiansContract.Companion.PoliticiansEntry()
        val databaseUri = politicianEntry.buildUriWithId(politician.sqlId!!)
        val timestamp = System.currentTimeMillis().toString()
        val contentValues = ContentValues()
        contentValues.put(politicianEntry.COLUMN_IMAGE_URL, item.link)
        contentValues.put(politicianEntry.COLUMN_IMAGE_TIMESTAMP, timestamp)

        politician.imageUrl = item.link
        politician.imageTimestamp = timestamp

        contentResolver.update(databaseUri, contentValues, null, null)
    }
}