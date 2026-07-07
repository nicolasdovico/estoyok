package com.estoyok.app.features.tracking.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010,\u001a\u00020-H\u0014J\u0006\u0010.\u001a\u00020-J\u000e\u0010/\u001a\u00020-2\u0006\u00100\u001a\u00020\u0007J\b\u00101\u001a\u00020-H\u0002J\u000e\u00102\u001a\u00020-2\u0006\u00103\u001a\u000204R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R7\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\r\u0010\u000e\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR/\u0010\u0010\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0005\u001a\u0004\u0018\u00010\u000f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0015\u0010\u000e\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R+\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0005\u001a\u00020\u00168F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001b\u0010\u000e\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR+\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u0005\u001a\u00020\u00168F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001e\u0010\u000e\u001a\u0004\b\u001c\u0010\u0018\"\u0004\b\u001d\u0010\u001aR\u0010\u0010\u001f\u001a\u0004\u0018\u00010 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R/\u0010!\u001a\u0004\u0018\u00010\u00072\b\u0010\u0005\u001a\u0004\u0018\u00010\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b&\u0010\u000e\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R7\u0010(\u001a\b\u0012\u0004\u0012\u00020\'0\u00062\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\'0\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b+\u0010\u000e\u001a\u0004\b)\u0010\n\"\u0004\b*\u0010\f\u00a8\u00065"}, d2 = {"Lcom/estoyok/app/features/tracking/presentation/MapaViewModel;", "Landroidx/lifecycle/ViewModel;", "circleRepository", "Lcom/estoyok/app/features/tracking/domain/repository/CircleRepository;", "(Lcom/estoyok/app/features/tracking/domain/repository/CircleRepository;)V", "<set-?>", "", "Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "circles", "getCircles", "()Ljava/util/List;", "setCircles", "(Ljava/util/List;)V", "circles$delegate", "Landroidx/compose/runtime/MutableState;", "", "errorMessage", "getErrorMessage", "()Ljava/lang/String;", "setErrorMessage", "(Ljava/lang/String;)V", "errorMessage$delegate", "", "isRefreshing", "()Z", "setRefreshing", "(Z)V", "isRefreshing$delegate", "isServiceRunning", "setServiceRunning", "isServiceRunning$delegate", "pollingJob", "Lkotlinx/coroutines/Job;", "selectedCircle", "getSelectedCircle", "()Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "setSelectedCircle", "(Lcom/estoyok/app/features/tracking/data/model/CircleDto;)V", "selectedCircle$delegate", "Lcom/estoyok/app/features/tracking/data/model/CircleMemberDto;", "selectedCircleMembers", "getSelectedCircleMembers", "setSelectedCircleMembers", "selectedCircleMembers$delegate", "onCleared", "", "refreshCircles", "selectCircle", "circle", "startPolling", "toggleTrackingService", "context", "Landroid/content/Context;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MapaViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.domain.repository.CircleRepository circleRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState circles$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState selectedCircle$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState selectedCircleMembers$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isServiceRunning$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isRefreshing$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job pollingJob;
    
    @javax.inject.Inject()
    public MapaViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.CircleRepository circleRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.tracking.data.model.CircleDto> getCircles() {
        return null;
    }
    
    private final void setCircles(java.util.List<com.estoyok.app.features.tracking.data.model.CircleDto> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.estoyok.app.features.tracking.data.model.CircleDto getSelectedCircle() {
        return null;
    }
    
    private final void setSelectedCircle(com.estoyok.app.features.tracking.data.model.CircleDto p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> getSelectedCircleMembers() {
        return null;
    }
    
    private final void setSelectedCircleMembers(java.util.List<com.estoyok.app.features.tracking.data.model.CircleMemberDto> p0) {
    }
    
    public final boolean isServiceRunning() {
        return false;
    }
    
    private final void setServiceRunning(boolean p0) {
    }
    
    public final boolean isRefreshing() {
        return false;
    }
    
    private final void setRefreshing(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    public final void refreshCircles() {
    }
    
    public final void selectCircle(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.CircleDto circle) {
    }
    
    private final void startPolling() {
    }
    
    public final void toggleTrackingService(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}