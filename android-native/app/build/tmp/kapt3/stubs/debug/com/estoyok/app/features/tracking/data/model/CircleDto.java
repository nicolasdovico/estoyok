package com.estoyok.app.features.tracking.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\u000f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00c6\u0003JR\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00c6\u0001\u00a2\u0006\u0002\u0010\u001fJ\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020\u0003H\u00d6\u0001J\t\u0010$\u001a\u00020\u0005H\u00d6\u0001R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001c\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR\u001a\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006%"}, d2 = {"Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "", "id", "", "name", "", "inviteCode", "ownerId", "speedLimit", "members", "", "Lcom/estoyok/app/features/tracking/data/model/CircleMemberDto;", "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/Integer;Ljava/util/List;)V", "getId", "()I", "getInviteCode", "()Ljava/lang/String;", "getMembers", "()Ljava/util/List;", "getName", "getOwnerId", "getSpeedLimit", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/Integer;Ljava/util/List;)Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class CircleDto {
    @com.google.gson.annotations.SerializedName(value = "id")
    private final int id = 0;
    @com.google.gson.annotations.SerializedName(value = "name")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @com.google.gson.annotations.SerializedName(value = "invite_code")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String inviteCode = null;
    @com.google.gson.annotations.SerializedName(value = "owner_id")
    private final int ownerId = 0;
    @com.google.gson.annotations.SerializedName(value = "speed_limit")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer speedLimit = null;
    @com.google.gson.annotations.SerializedName(value = "users")
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> members = null;
    
    public CircleDto(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String inviteCode, int ownerId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer speedLimit, @org.jetbrains.annotations.NotNull()
    java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> members) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInviteCode() {
        return null;
    }
    
    public final int getOwnerId() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getSpeedLimit() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> getMembers() {
        return null;
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.tracking.data.model.CircleDto copy(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String inviteCode, int ownerId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer speedLimit, @org.jetbrains.annotations.NotNull()
    java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> members) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}