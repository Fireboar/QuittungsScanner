package com.example.quittungsscanner.data.receipt

import android.content.Context
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream



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



fun copyTesseractData(context: Context): Boolean {
    val tessDataPath = File(context.filesDir, "tessdata")

    // Erstelle den Ordner "tessdata", falls er noch nicht existiert
    if (!tessDataPath.exists()) {
        tessDataPath.mkdirs()
    }

    // Ziel-Datei im "tessdata"-Ordner
    val trainedDataFile = File(tessDataPath, "deu.traineddata")

    // Überprüfe, ob die Datei bereits existiert
    if (trainedDataFile.exists()) {
        return true  // Die Datei existiert bereits
    }

    // Kopiere die Datei aus den Assets in den internen Speicher
    try {
        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("tessdata/deu.traineddata")
        val outputStream: OutputStream = FileOutputStream(trainedDataFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
