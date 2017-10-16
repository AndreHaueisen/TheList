package com.andrehaueisen.listadejanot.d_search_politician.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.BuildConfig
import com.andrehaueisen.listadejanot.images.ImageFetcherService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * Created by andre on 9/30/2017.
 */
@Module
class ImageFetcherModule{

    /*
    "https://www.googleapis.com/customsearch/v1
 -- ?q={searchTerms}&
 -- num=5&
    start={startIndex?}&
    lr={language?}&
    safe={safe?}&
 -- cx="000940944128054755129:fptjb5vejxk"&
    sort={sort?}&
    filter={filter?}&
    gl={gl?}&
    cr={cr?}&
    googlehost={googleHost?}&
    c2coff={disableCnTwTranslation?}&
    hq={hq?}&
 -- hl="pt-BR"&
    siteSearch={siteSearch?}&
    siteSearchFilter={siteSearchFilter?}&
    exactTerms={exactTerms?}&
    excludeTerms={excludeTerms?}&
    linkSite={linkSite?}&
    orTerms={orTerms?}&
    relatedSite={relatedSite?}&
    dateRestrict={dateRestrict?}&
    lowRange={lowRange?}&
    highRange={highRange?}&
 -- searchType="image"&
    fileType={fileType?}&
    rights={rights?}&
 -- imgSize="medium"&
 -- imgType="face"&
    imgColorType={imgColorType?}&
    imgDominantColor={imgDominantColor?}&
    alt=json"
 -- key=BuildConfig...
    */

    private val BASE_NEWS_URL = "https://www.googleapis.com/"

    private val PARAMETER_RESULTS_NUMBER = "num"
    private val PARAMETER_SEARCH_ID = "cx"
    private val PARAMETER_LANGUAGE = "hl"
    private val PARAMETER_SEARCH_TYPE = "searchType"
    private val PARAMETER_IMAGE_SIZE = "imgSize"
    private val PARAMETER_IMAGE_TYPE = "imgType"
    private val PARAMETER_SEARCH_KEY = "key"

    @Provides
    @PoliticianSelectorScope
    fun provideCache(context: Context) = Cache(File(context.externalCacheDir, "new_cache_dir"), (5 * 1024 * 1024).toLong()) //5MB cache

    @Provides
    @PoliticianSelectorScope
    fun provideConverterFactory(): GsonConverterFactory {
        val customGson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        return GsonConverterFactory.create(customGson)
    }

    @Provides
    @PoliticianSelectorScope
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @PoliticianSelectorScope
    fun provideInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            val url = request
                    .url()
                    .newBuilder()
                    .addQueryParameter(PARAMETER_RESULTS_NUMBER, "1")
                    .addQueryParameter(PARAMETER_SEARCH_ID, BuildConfig.SEARCH_ID)
                    .addQueryParameter(PARAMETER_LANGUAGE, "pt-BR")
                    .addQueryParameter(PARAMETER_SEARCH_TYPE, "image")
                    .addQueryParameter(PARAMETER_IMAGE_SIZE, "medium")
                    .addQueryParameter(PARAMETER_IMAGE_TYPE, "photo")
                    .addQueryParameter(PARAMETER_SEARCH_KEY, BuildConfig.SEARCH_API_KEY)
                    .build()
            request = request.newBuilder().url(url).build()
            chain.proceed(request)
        }
    }

    @Provides
    @PoliticianSelectorScope
    fun provideRxJavaFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Provides
    @PoliticianSelectorScope
    fun provideClient(loggingInterceptor: HttpLoggingInterceptor, interceptor: Interceptor, cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(interceptor)
                .cache(cache)
                .build()
    }

    @Provides
    @PoliticianSelectorScope
    fun provideImageService(client: OkHttpClient, converterFactory: GsonConverterFactory, adapterFactory: RxJava2CallAdapterFactory): ImageFetcherService {
        return Retrofit.Builder()
                .baseUrl(BASE_NEWS_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(adapterFactory)
                .build()
                .create<ImageFetcherService>(ImageFetcherService::class.java)
    }
}
