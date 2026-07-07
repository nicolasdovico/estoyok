package com.estoyok.app.features.tracking.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\"\u00104\u001a\u0002052\u0006\u00106\u001a\u00020\u001a2\u0012\u00107\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020508J\u000e\u00109\u001a\u0002052\u0006\u0010:\u001a\u00020\u001aJ\u000e\u0010;\u001a\u0002052\u0006\u0010<\u001a\u00020=J\u000e\u0010>\u001a\u000205H\u0082@\u00a2\u0006\u0002\u0010?J\u000e\u0010@\u001a\u000205H\u0082@\u00a2\u0006\u0002\u0010?J\u000e\u0010A\u001a\u0002052\u0006\u0010B\u001a\u00020\u001aJ\u0006\u0010C\u001a\u000205J\u0016\u0010D\u001a\u0002052\u0006\u0010<\u001a\u00020=2\u0006\u0010E\u001a\u00020=J\u000e\u0010F\u001a\u0002052\u0006\u0010G\u001a\u00020\u0013R+\u0010\u000b\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R7\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00130\u00128F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0019\u0010\u0011\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R/\u0010\u001b\u001a\u0004\u0018\u00010\u001a2\b\u0010\t\u001a\u0004\u0018\u00010\u001a8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b \u0010\u0011\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR+\u0010!\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b#\u0010\u0011\u001a\u0004\b!\u0010\r\"\u0004\b\"\u0010\u000fR+\u0010$\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b&\u0010\u0011\u001a\u0004\b$\u0010\r\"\u0004\b%\u0010\u000fR/\u0010\'\u001a\u0004\u0018\u00010\u00132\b\u0010\t\u001a\u0004\u0018\u00010\u00138F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b,\u0010\u0011\u001a\u0004\b(\u0010)\"\u0004\b*\u0010+R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010.\u001a\u0004\u0018\u00010-2\b\u0010\t\u001a\u0004\u0018\u00010-8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b3\u0010\u0011\u001a\u0004\b/\u00100\"\u0004\b1\u00102\u00a8\u0006H"}, d2 = {"Lcom/estoyok/app/features/tracking/presentation/FamiliaViewModel;", "Landroidx/lifecycle/ViewModel;", "circleRepository", "Lcom/estoyok/app/features/tracking/domain/repository/CircleRepository;", "subscriptionRepository", "Lcom/estoyok/app/features/tracking/domain/repository/SubscriptionRepository;", "settingsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;", "(Lcom/estoyok/app/features/tracking/domain/repository/CircleRepository;Lcom/estoyok/app/features/tracking/domain/repository/SubscriptionRepository;Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;)V", "<set-?>", "", "checkoutLoading", "getCheckoutLoading", "()Z", "setCheckoutLoading", "(Z)V", "checkoutLoading$delegate", "Landroidx/compose/runtime/MutableState;", "", "Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "circles", "getCircles", "()Ljava/util/List;", "setCircles", "(Ljava/util/List;)V", "circles$delegate", "", "errorMessage", "getErrorMessage", "()Ljava/lang/String;", "setErrorMessage", "(Ljava/lang/String;)V", "errorMessage$delegate", "isActionInProgress", "setActionInProgress", "isActionInProgress$delegate", "isRefreshing", "setRefreshing", "isRefreshing$delegate", "selectedCircle", "getSelectedCircle", "()Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "setSelectedCircle", "(Lcom/estoyok/app/features/tracking/data/model/CircleDto;)V", "selectedCircle$delegate", "Lcom/estoyok/app/features/auth/data/model/UserDto;", "user", "getUser", "()Lcom/estoyok/app/features/auth/data/model/UserDto;", "setUser", "(Lcom/estoyok/app/features/auth/data/model/UserDto;)V", "user$delegate", "checkoutSubscription", "", "provider", "onUrlReceived", "Lkotlin/Function1;", "createCircle", "name", "deleteCircle", "circleId", "", "fetchCircles", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchUserProfile", "joinCircle", "inviteCode", "refreshData", "removeMember", "memberId", "selectCircle", "circle", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class FamiliaViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.domain.repository.CircleRepository circleRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository subscriptionRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState user$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState circles$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState selectedCircle$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isRefreshing$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isActionInProgress$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState checkoutLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    
    @javax.inject.Inject()
    public FamiliaViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.CircleRepository circleRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository subscriptionRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.estoyok.app.features.auth.data.model.UserDto getUser() {
        return null;
    }
    
    private final void setUser(com.estoyok.app.features.auth.data.model.UserDto p0) {
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
    
    public final boolean isRefreshing() {
        return false;
    }
    
    private final void setRefreshing(boolean p0) {
    }
    
    public final boolean isActionInProgress() {
        return false;
    }
    
    private final void setActionInProgress(boolean p0) {
    }
    
    public final boolean getCheckoutLoading() {
        return false;
    }
    
    private final void setCheckoutLoading(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    public final void refreshData() {
    }
    
    private final java.lang.Object fetchUserProfile(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchCircles(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void selectCircle(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.CircleDto circle) {
    }
    
    public final void createCircle(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void joinCircle(@org.jetbrains.annotations.NotNull()
    java.lang.String inviteCode) {
    }
    
    public final void removeMember(int circleId, int memberId) {
    }
    
    public final void deleteCircle(int circleId) {
    }
    
    public final void checkoutSubscription(@org.jetbrains.annotations.NotNull()
    java.lang.String provider, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onUrlReceived) {
    }
}