package com.wisatakita.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0011B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0007\u001a\u00020\bH\u0016J\u001c\u0010\t\u001a\u00020\n2\n\u0010\u000b\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\f\u001a\u00020\bH\u0016J\u001c\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/wisatakita/app/TeamAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/wisatakita/app/TeamAdapter$ViewHolder;", "members", "", "Lcom/wisatakita/app/data/TeamMember;", "(Ljava/util/List;)V", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ViewHolder", "app_debug"})
public final class TeamAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.wisatakita.app.TeamAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.wisatakita.app.data.TeamMember> members = null;
    
    public TeamAdapter(@org.jetbrains.annotations.NotNull
    java.util.List<com.wisatakita.app.data.TeamMember> members) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.wisatakita.app.TeamAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    com.wisatakita.app.TeamAdapter.ViewHolder holder, int position) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/wisatakita/app/TeamAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/wisatakita/app/databinding/ItemTeamMemberBinding;", "(Lcom/wisatakita/app/TeamAdapter;Lcom/wisatakita/app/databinding/ItemTeamMemberBinding;)V", "getBinding", "()Lcom/wisatakita/app/databinding/ItemTeamMemberBinding;", "bind", "", "member", "Lcom/wisatakita/app/data/TeamMember;", "app_debug"})
    public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final com.wisatakita.app.databinding.ItemTeamMemberBinding binding = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull
        com.wisatakita.app.databinding.ItemTeamMemberBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.wisatakita.app.databinding.ItemTeamMemberBinding getBinding() {
            return null;
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull
        com.wisatakita.app.data.TeamMember member) {
        }
    }
}