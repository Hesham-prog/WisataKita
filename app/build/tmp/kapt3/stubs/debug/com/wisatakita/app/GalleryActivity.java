package com.wisatakita.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0014J\b\u0010\r\u001a\u00020\nH\u0014J\b\u0010\u000e\u001a\u00020\nH\u0002J\b\u0010\u000f\u001a\u00020\nH\u0002J\u0018\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/wisatakita/app/GalleryActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "adapter", "Lcom/wisatakita/app/AlbumAdapter;", "binding", "Lcom/wisatakita/app/databinding/ActivityGalleryBinding;", "galleryData", "Lcom/wisatakita/app/data/GalleryData;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "refreshAlbums", "showCreateAlbumDialog", "showDeleteAlbumDialog", "albumId", "", "albumName", "app_debug"})
public final class GalleryActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.wisatakita.app.databinding.ActivityGalleryBinding binding;
    private com.wisatakita.app.data.GalleryData galleryData;
    private com.wisatakita.app.AlbumAdapter adapter;
    
    public GalleryActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override
    protected void onResume() {
    }
    
    private final void refreshAlbums() {
    }
    
    private final void showCreateAlbumDialog() {
    }
    
    private final void showDeleteAlbumDialog(java.lang.String albumId, java.lang.String albumName) {
    }
}