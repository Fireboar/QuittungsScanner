package com.example.quittungsscanner.data.receipt

import android.content.Context
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TessModule {

    @Provides
    @Singleton
    fun provideTessBaseAPI(@ApplicationContext context: Context): TessBaseAPI {
        val tessBaseAPI = TessBaseAPI()

        val path = context.filesDir.toString()

        tessBaseAPI.init(path, "deu")
        return tessBaseAPI
    }

}