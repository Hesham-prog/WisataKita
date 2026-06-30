package com.wisatakita.app.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\rJ\u000e\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\rJ\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00020\rJ\u0006\u0010\u0013\u001a\u00020\u0014J\u001e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\rJ.\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0019\u001a\u00020\r2\u0006\u0010\u001a\u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\r2\u0006\u0010\u001c\u001a\u00020\rJ\u000e\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u0010\u001a\u00020\rJ\u0016\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\rR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n \t*\u0004\u0018\u00010\b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/wisatakita/app/data/UserPrefs;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "db", "Lcom/wisatakita/app/data/db/AppDatabase;", "session", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "userDao", "Lcom/wisatakita/app/data/db/UserDao;", "getCurrentEmail", "", "getCurrentName", "getName", "email", "isEmailTaken", "", "logout", "", "register", "name", "password", "saveProfile", "age", "gender", "phone", "hometown", "setCurrentUser", "validateLogin", "app_debug"})
public final class UserPrefs {
    @org.jetbrains.annotations.NotNull
    private final com.wisatakita.app.data.db.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull
    private final com.wisatakita.app.data.db.UserDao userDao = null;
    private final android.content.SharedPreferences session = null;
    
    public UserPrefs(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final void register(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password) {
    }
    
    public final void saveProfile(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String age, @org.jetbrains.annotations.NotNull
    java.lang.String gender, @org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    java.lang.String hometown) {
    }
    
    public final boolean isEmailTaken(@org.jetbrains.annotations.NotNull
    java.lang.String email) {
        return false;
    }
    
    public final boolean validateLogin(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getName(@org.jetbrains.annotations.NotNull
    java.lang.String email) {
        return null;
    }
    
    public final void setCurrentUser(@org.jetbrains.annotations.NotNull
    java.lang.String email) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrentEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrentName() {
        return null;
    }
    
    public final void logout() {
    }
}