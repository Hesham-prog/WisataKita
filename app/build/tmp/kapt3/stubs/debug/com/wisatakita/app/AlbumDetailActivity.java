package com.wisatakita.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002J\b\u0010\u0015\u001a\u00020\u0014H\u0002J\b\u0010\u0016\u001a\u00020\u0014H\u0002J\u0012\u0010\u0017\u001a\u00020\u00142\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0014J\b\u0010\u001a\u001a\u00020\u0014H\u0014J\b\u0010\u001b\u001a\u00020\u0014H\u0002J\b\u0010\u001c\u001a\u00020\u0014H\u0002J\u0010\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u001e\u001a\u00020\u0006H\u0002J\u0010\u0010\u001f\u001a\u00020\u00142\u0006\u0010 \u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\r\u001a\u0010\u0012\f\u0012\n \u000f*\u0004\u0018\u00010\u00060\u00060\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0010\u001a\u0010\u0012\f\u0012\n \u000f*\u0004\u0018\u00010\u00060\u00060\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0011\u001a\u0010\u0012\f\u0012\n \u000f*\u0004\u0018\u00010\u00060\u00060\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0012\u001a\u0010\u0012\f\u0012\n \u000f*\u0004\u0018\u00010\f0\f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/wisatakita/app/AlbumDetailActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "adapter", "Lcom/wisatakita/app/PhotoAdapter;", "albumId", "", "binding", "Lcom/wisatakita/app/databinding/ActivityAlbumDetailBinding;", "galleryData", "Lcom/wisatakita/app/data/GalleryData;", "pendingCameraUri", "Landroid/net/Uri;", "pickImage", "Landroidx/activity/result/ActivityResultLauncher;", "kotlin.jvm.PlatformType", "requestCameraPermission", "requestGalleryPermission", "takePicture", "checkCameraAndLaunch", "", "checkGalleryAndLaunch", "launchCamera", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "refreshPhotos", "showAddPhotoDialog", "showDeleteAlbumDialog", "albumName", "showDeletePhotoDialog", "uri", "app_debug"})
public final class AlbumDetailActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.wisatakita.app.databinding.ActivityAlbumDetailBinding binding;
    private com.wisatakita.app.data.GalleryData galleryData;
    private com.wisatakita.app.PhotoAdapter adapter;
    @org.jetbrains.annotations.NotNull
    private java.lang.String albumId = "";
    @org.jetbrains.annotations.Nullable
    private android.net.Uri pendingCameraUri;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> pickImage = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePicture = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestCameraPermission = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestGalleryPermission = null;
    
    public AlbumDetailActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override
    protected void onResume() {
    }
    
    private final void refreshPhotos() {
    }
    
    private final void showAddPhotoDialog() {
    }
    
    private final void checkCameraAndLaunch() {
    }
    
    private final void checkGalleryAndLaunch() {
    }
    
    private final void launchCamera() {
    }
    
    private final void showDeletePhotoDialog(java.lang.String uri) {
    }
    
    private final void showDeleteAlbumDialog(java.lang.String albumName) {
    }
}