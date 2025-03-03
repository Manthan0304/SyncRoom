package com.codewithfk.chatter

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.util.UUID

class SupabaseStorageUtils(val context: Context) {

    val supabase = createSupabaseClient(
        "https://sazoxlpkpfdhvuomaxfn.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNhem94bHBrcGZkaHZ1b21heGZuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzk0MDQ4NTgsImV4cCI6MjA1NDk4MDg1OH0.Y_-wZkXMGpAmF26MPuisQ8oasiKeWQVpDDz81yAn0kw"
    ) {
        install(Storage)
    }

    suspend fun uploadImage(uri: Uri): String? {
        try {
            val extension = uri.path?.substringAfterLast(".") ?: "jpg"
            val fileName = "${UUID.randomUUID()}.$extension"
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            supabase.storage.from(BUCKET_NAME).upload(fileName, inputStream.readBytes())
            val publicUrl = supabase.storage.from(BUCKET_NAME).publicUrl(fileName)
            return publicUrl
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    companion object {
        const val BUCKET_NAME = "chat_images"
    }
}