package com.estoyok.app.features.wellbeing.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001c\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001aJ\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0007\u001a#\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0011\u0010\u0013\u001a\r\u0012\u0004\u0012\u00020\u00010\r\u00a2\u0006\u0002\b\u0014H\u0007\u00a8\u0006\u0015"}, d2 = {"AjustesScreen", "", "viewModel", "Lcom/estoyok/app/features/wellbeing/presentation/AjustesViewModel;", "authViewModel", "Lcom/estoyok/app/features/auth/presentation/AuthViewModel;", "ContactRow", "contact", "Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;", "index", "", "totalSize", "onMoveUp", "Lkotlin/Function0;", "onMoveDown", "onDelete", "SettingsCard", "title", "", "content", "Landroidx/compose/runtime/Composable;", "app_debug"})
public final class AjustesScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AjustesScreen(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.presentation.AjustesViewModel viewModel, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.presentation.AuthViewModel authViewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SettingsCard(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ContactRow(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact, int index, int totalSize, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onMoveUp, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onMoveDown, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
}