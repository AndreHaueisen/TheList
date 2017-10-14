package com.andrehaueisen.listadejanot.utilities

import com.andrehaueisen.listadejanot.models.Items
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by andre on 9/30/2017.
 */
interface ImageFetcherService {

    @GET("customsearch/v1")
    fun catchNews(@Query("q") query: String): Observable<Items>
}