package com.andrehaueisen.listadejanot.images

import com.andrehaueisen.listadejanot.models.Item
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function

/**
 * Created by andre on 9/30/2017.
 */
class ImageFetcherModel(private val mImageFetcherService: ImageFetcherService) {

    fun getPoliticianImages(politicianName: String, politicianPost: Politician.Post): Observable<Item> {
        return getImagesFromNetwork(politicianName, politicianPost).flatMap(object : Function<Item, ObservableSource<Item>> {
            override fun apply(item: Item): ObservableSource<Item> {

                return Observable.just(item)
            }
        })
    }

    private fun getImagesFromNetwork(politicianName: String, politicianPost: Politician.Post): Observable<Item> {

        return mImageFetcherService.catchNews("$politicianName ${politicianPost.name}")
                .flatMap({ items ->
                    Observable.fromIterable(items.items)
                })
    }
}