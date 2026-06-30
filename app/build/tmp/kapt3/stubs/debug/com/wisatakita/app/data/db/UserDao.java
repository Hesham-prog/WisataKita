package com.wisatakita.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0012\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0007H\'J\u0010\u0010\u000b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0007H\'\u00a8\u0006\f"}, d2 = {"Lcom/wisatakita/app/data/db/UserDao;", "", "countByEmail", "", "email", "", "findByEmail", "Lcom/wisatakita/app/data/db/UserEntity;", "insert", "", "user", "update", "app_debug"})
@androidx.room.Dao
public abstract interface UserDao {
    
    @androidx.room.Insert(onConflict = 5)
    public abstract void insert(@org.jetbrains.annotations.NotNull
    com.wisatakita.app.data.db.UserEntity user);
    
    @androidx.room.Update
    public abstract void update(@org.jetbrains.annotations.NotNull
    com.wisatakita.app.data.db.UserEntity user);
    
    @androidx.room.Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1")
    @org.jetbrains.annotations.Nullable
    public abstract com.wisatakita.app.data.db.UserEntity findByEmail(@org.jetbrains.annotations.NotNull
    java.lang.String email);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM users WHERE email = :email")
    public abstract int countByEmail(@org.jetbrains.annotations.NotNull
    java.lang.String email);
}