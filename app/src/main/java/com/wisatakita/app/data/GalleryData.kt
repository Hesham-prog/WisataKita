package com.wisatakita.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Album(
    val id: String,
    val name: String,
    val createdAt: Long,
    val photoUris: MutableList<String>
) {
    val coverUri: String? get() = photoUris.lastOrNull()
    val photoCount: Int get() = photoUris.size
}

class GalleryData(context: Context) {
    private val prefs = context.getSharedPreferences("wk_gallery", Context.MODE_PRIVATE)

    fun getAlbums(): MutableList<Album> {
        val json = prefs.getString("albums", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val photosArr = obj.getJSONArray("photos")
                val photos = (0 until photosArr.length()).map { photosArr.getString(it) }.toMutableList()
                Album(obj.getString("id"), obj.getString("name"), obj.getLong("createdAt"), photos)
            }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveAlbums(albums: List<Album>) {
        val arr = JSONArray()
        albums.forEach { album ->
            val obj = JSONObject().apply {
                put("id", album.id)
                put("name", album.name)
                put("createdAt", album.createdAt)
                val photosArr = JSONArray()
                album.photoUris.forEach { photosArr.put(it) }
                put("photos", photosArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString("albums", arr.toString()).apply()
    }

    fun createAlbum(name: String): Album {
        val albums = getAlbums()
        val album = Album(System.currentTimeMillis().toString(), name, System.currentTimeMillis(), mutableListOf())
        albums.add(album)
        saveAlbums(albums)
        return album
    }

    fun addPhoto(albumId: String, uri: String) {
        val albums = getAlbums()
        albums.find { it.id == albumId }?.photoUris?.add(uri)
        saveAlbums(albums)
    }

    fun deletePhoto(albumId: String, uri: String) {
        val albums = getAlbums()
        albums.find { it.id == albumId }?.photoUris?.remove(uri)
        saveAlbums(albums)
    }

    fun deleteAlbum(albumId: String) {
        saveAlbums(getAlbums().filter { it.id != albumId })
    }

    fun getAlbum(albumId: String) = getAlbums().find { it.id == albumId }
}
