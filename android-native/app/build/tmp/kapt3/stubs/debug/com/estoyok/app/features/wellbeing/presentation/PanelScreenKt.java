package com.estoyok.app.features.wellbeing.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001af\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\b0\u000f2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00032\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00010\u0014H\u0007\u001a\u0012\u0010\u0016\u001a\u00020\u00012\b\b\u0002\u0010\u0017\u001a\u00020\u0018H\u0007\u001a\b\u0010\u0019\u001a\u00020\u0001H\u0007\u001a\u0010\u0010\u001a\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\rH\u0007\u00a8\u0006\u001b"}, d2 = {"CheckInButton", "", "isCheckingIn", "", "onClick", "Lkotlin/Function0;", "CheckInItemRow", "checkIn", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInDto;", "PanelContent", "userName", "", "status", "Lcom/estoyok/app/features/wellbeing/presentation/WellbeingStatus;", "checkInHistory", "", "isSosTriggered", "onRefresh", "onCheckIn", "onSos", "Lkotlin/Function1;", "Landroid/content/Context;", "PanelScreen", "viewModel", "Lcom/estoyok/app/features/wellbeing/presentation/PanelViewModel;", "PanelScreenPreview", "StatusBanner", "app_debug"})
public final class PanelScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void PanelScreen(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.presentation.PanelViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PanelContent(@org.jetbrains.annotations.NotNull()
    java.lang.String userName, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.presentation.WellbeingStatus status, @org.jetbrains.annotations.NotNull()
    java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto> checkInHistory, boolean isCheckingIn, boolean isSosTriggered, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRefresh, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCheckIn, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super android.content.Context, kotlin.Unit> onSos) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StatusBanner(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.presentation.WellbeingStatus status) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CheckInButton(boolean isCheckingIn, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CheckInItemRow(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.CheckInDto checkIn) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void PanelScreenPreview() {
    }
}