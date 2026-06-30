package com.wisatakita.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0018\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\u0005H\'J\u000e\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\'J\u0016\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\n2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u000bH\'J\u0010\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\rH\'\u00a8\u0006\u0012"}, d2 = {"Lcom/wisatakita/app/data/db/AlbumDao;", "", "deleteAlbum", "", "albumId", "", "deleteAllPhotosInAlbum", "deletePhoto", "uri", "getAllAlbums", "", "Lcom/wisatakita/app/data/db/AlbumEntity;", "getPhotos", "Lcom/wisatakita/app/data/db/PhotoEntity;", "insertAlbum", "album", "insertPhoto", "photo", "app_debug"})
@androidx.room.Dao
public abstract interface AlbumDao {
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void insertAlbum(@org.jetbrains.annotations.NotNull
    com.wisatakita.app.data.db.AlbumEntity album);
    
    @androidx.room.Query(value = "SELECT * FROM albums ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull
    public abstract java.util.List<com.wisatakita.app.data.db.AlbumEntity> getAllAlbums();
    
    @androidx.room.Query(value = "DELETE FROM albums WHERE id = :albumId")
    public abstract void deleteAlbum(@org.jetbrains.annotations.NotNull
    java.lang.String albumId);
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void insertPhoto(@org.jetbrains.annotations.NotNull
    com.wisatakita.app.data.db.PhotoEntity photo);
    
    @androidx.room.Query(value = "SELECT * FROM photos WHERE albumId = :albumId")
    @org.jetbrains.annotations.NotNull
    public abstract java.util.List<com.wisatakita.app.data.db.PhotoEntity> getPhotos(@org.jetbrains.annotations.NotNull
    java.lang.String albumId);
    
    @androidx.room.Query(value = "DELETE FROM photos WHERE albumId = :albumId AND uri = :uri")
    public abstract void deletePhoto(@org.jetbrains.annotations.NotNull
    java.lang.String albumId, @org.jetbrains.annotations.NotNull
    java.lang.String uri);
    
    @androidx.room.Query(value = "DELETE FROM photos WHERE albumId = :albumId")
    public abstract void deleteAllPhotosInAlbum(@org.jetbrains.annotations.NotNull
    java.lang.String albumId);
}