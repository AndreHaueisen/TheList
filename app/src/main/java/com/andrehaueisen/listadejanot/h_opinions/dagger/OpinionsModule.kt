package com.andrehaueisen.listadejanot.h_opinions.dagger

import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by andre on 8/23/2017.
 */
@Module
class OpinionsModule{

    @OpinionsScope
    @Provides
    @Named("opinions")
    fun provideRepository(firebaseRepository: FirebaseRepository): FirebaseRepository = firebaseRepository

}
