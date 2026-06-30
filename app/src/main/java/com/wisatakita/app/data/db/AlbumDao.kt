package com.wisatakita.app.data.db

import androidx.room.*

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlbum(album: AlbumEntity)

    @Query("SELECT * FROM albums ORDER BY createdAt DESC")
    fun getAllAlbums(): List<AlbumEntity>

    @Query("DELETE FROM albums WHERE id = :albumId")
    fun deleteAlbum(albumId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE albumId = :albumId")
    fun getPhotos(albumId: String): List<PhotoEntity>

    @Query("DELETE FROM photos WHERE albumId = :albumId AND uri = :uri")
    fun deletePhoto(albumId: String, uri: String)

    @Query("DELETE FROM photos WHERE albumId = :albumId")
    fun deleteAllPhotosInAlbum(albumId: String)
}
