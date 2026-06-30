package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.data.db.AlbumEntity
import com.wisatakita.app.data.db.AppDatabase
import com.wisatakita.app.data.db.PhotoEntity

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
    private val db = AppDatabase.getInstance(context)
    private val albumDao = db.albumDao()

    fun getAlbums(): MutableList<Album> {
        return albumDao.getAllAlbums().map { entity ->
            val photos = albumDao.getPhotos(entity.id).map { it.uri }.toMutableList()
            Album(entity.id, entity.name, entity.createdAt, photos)
        }.toMutableList()
    }

    fun getAlbum(albumId: String): Album? = getAlbums().find { it.id == albumId }

    fun createAlbum(name: String): Album {
        val id = System.currentTimeMillis().toString()
        val createdAt = System.currentTimeMillis()
        albumDao.insertAlbum(AlbumEntity(id, name, createdAt))
        return Album(id, name, createdAt, mutableListOf())
    }

    fun addPhoto(albumId: String, uri: String) {
        albumDao.insertPhoto(PhotoEntity(albumId = albumId, uri = uri))
    }

    fun deletePhoto(albumId: String, uri: String) {
        albumDao.deletePhoto(albumId, uri)
    }

    fun deleteAlbum(albumId: String) {
        albumDao.deleteAllPhotosInAlbum(albumId)
        albumDao.deleteAlbum(albumId)
    }
}
