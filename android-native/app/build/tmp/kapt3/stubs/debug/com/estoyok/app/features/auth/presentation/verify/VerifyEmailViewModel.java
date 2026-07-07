package com.estoyok.app.features.auth.presentation.verify;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010*\u001a\u00020\t2\u0006\u0010+\u001a\u00020\u000bJ\u0006\u0010,\u001a\u00020\tJ\u0006\u0010-\u001a\u00020\tR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\f\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0011\u0010\u0012\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0013\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR/\u0010\u0015\u001a\u0004\u0018\u00010\u000b2\b\u0010\n\u001a\u0004\u0018\u00010\u000b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0018\u0010\u0012\u001a\u0004\b\u0016\u0010\u000e\"\u0004\b\u0017\u0010\u0010R+\u0010\u001a\u001a\u00020\u00192\u0006\u0010\n\u001a\u00020\u00198F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001e\u0010\u0012\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR+\u0010\u001f\u001a\u00020\u00192\u0006\u0010\n\u001a\u00020\u00198F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b!\u0010\u0012\u001a\u0004\b\u001f\u0010\u001b\"\u0004\b \u0010\u001dR/\u0010\"\u001a\u0004\u0018\u00010\u000b2\b\u0010\n\u001a\u0004\u0018\u00010\u000b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b%\u0010\u0012\u001a\u0004\b#\u0010\u000e\"\u0004\b$\u0010\u0010R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\t0\'\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)\u00a8\u0006."}, d2 = {"Lcom/estoyok/app/features/auth/presentation/verify/VerifyEmailViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "(Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;Landroidx/lifecycle/SavedStateHandle;)V", "_verificationSuccess", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "<set-?>", "", "code", "getCode", "()Ljava/lang/String;", "setCode", "(Ljava/lang/String;)V", "code$delegate", "Landroidx/compose/runtime/MutableState;", "email", "getEmail", "errorMessage", "getErrorMessage", "setErrorMessage", "errorMessage$delegate", "", "isLoading", "()Z", "setLoading", "(Z)V", "isLoading$delegate", "isResending", "setResending", "isResending$delegate", "successMessage", "getSuccessMessage", "setSuccessMessage", "successMessage$delegate", "verificationSuccess", "Lkotlinx/coroutines/flow/SharedFlow;", "getVerificationSuccess", "()Lkotlinx/coroutines/flow/SharedFlow;", "onCodeChange", "newValue", "resendOtp", "verify", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class VerifyEmailViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState code$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isResending$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState successMessage$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Unit> _verificationSuccess = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> verificationSuccess = null;
    
    @javax.inject.Inject()
    public VerifyEmailViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.SavedStateHandle savedStateHandle) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCode() {
        return null;
    }
    
    private final void setCode(java.lang.String p0) {
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    private final void setLoading(boolean p0) {
    }
    
    public final boolean isResending() {
        return false;
    }
    
    private final void setResending(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSuccessMessage() {
        return null;
    }
    
    private final void setSuccessMessage(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> getVerificationSuccess() {
        return null;
    }
    
    public final void onCodeChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void verify() {
    }
    
    public final void resendOtp() {
    }
}