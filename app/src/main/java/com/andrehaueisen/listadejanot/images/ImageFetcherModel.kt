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

        val queryComplement = when(politicianPost){
            Politician.Post.SENADOR, Politician.Post.SENADORA -> "congresso nacional"
            Politician.Post.PRESIDENTE -> "posse"
            Politician.Post.GOVERNADOR, Politician.Post.GOVERNADORA -> "sede do governo"
            Politician.Post.DEPUTADO, Politician.Post.DEPUTADA -> "fala"
        }

        return mImageFetcherService.catchNews("$politicianName ${politicianPost.name} $queryComplement")
                .flatMap({ items ->
                    Observable.fromIterable(items.items)
                })
    }
}