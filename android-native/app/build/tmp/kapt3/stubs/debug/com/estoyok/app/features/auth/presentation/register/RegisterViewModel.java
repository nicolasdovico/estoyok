package com.estoyok.app.features.auth.presentation.register;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010.\u001a\u00020/2\u0006\u00100\u001a\u00020\u0007J\u000e\u00101\u001a\u00020/2\u0006\u00100\u001a\u00020\u0007J\u000e\u00102\u001a\u00020/2\u0006\u00100\u001a\u00020\u0007J\u000e\u00103\u001a\u00020/2\u0006\u00100\u001a\u00020\u0007J\u000e\u00104\u001a\u00020/2\u0006\u00100\u001a\u00020\u0007J\u0006\u00105\u001a\u00020/R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\t\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR+\u0010\u0010\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0013\u0010\u000f\u001a\u0004\b\u0011\u0010\u000b\"\u0004\b\u0012\u0010\rR/\u0010\u0014\u001a\u0004\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0017\u0010\u000f\u001a\u0004\b\u0015\u0010\u000b\"\u0004\b\u0016\u0010\rR+\u0010\u0019\u001a\u00020\u00182\u0006\u0010\b\u001a\u00020\u00188F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001d\u0010\u000f\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR+\u0010\u001e\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b!\u0010\u000f\u001a\u0004\b\u001f\u0010\u000b\"\u0004\b \u0010\rR+\u0010\"\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b%\u0010\u000f\u001a\u0004\b#\u0010\u000b\"\u0004\b$\u0010\rR+\u0010&\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00078F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b)\u0010\u000f\u001a\u0004\b\'\u0010\u000b\"\u0004\b(\u0010\rR\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00070+\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-\u00a8\u00066"}, d2 = {"Lcom/estoyok/app/features/auth/presentation/register/RegisterViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "(Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;)V", "_registerSuccess", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "<set-?>", "confirmPassword", "getConfirmPassword", "()Ljava/lang/String;", "setConfirmPassword", "(Ljava/lang/String;)V", "confirmPassword$delegate", "Landroidx/compose/runtime/MutableState;", "email", "getEmail", "setEmail", "email$delegate", "errorMessage", "getErrorMessage", "setErrorMessage", "errorMessage$delegate", "", "isLoading", "()Z", "setLoading", "(Z)V", "isLoading$delegate", "name", "getName", "setName", "name$delegate", "password", "getPassword", "setPassword", "password$delegate", "phone", "getPhone", "setPhone", "phone$delegate", "registerSuccess", "Lkotlinx/coroutines/flow/SharedFlow;", "getRegisterSuccess", "()Lkotlinx/coroutines/flow/SharedFlow;", "onConfirmPasswordChange", "", "newValue", "onEmailChange", "onNameChange", "onPasswordChange", "onPhoneChange", "register", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class RegisterViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState name$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState email$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState phone$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState password$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState confirmPassword$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<java.lang.String> _registerSuccess = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<java.lang.String> registerSuccess = null;
    
    @javax.inject.Inject()
    public RegisterViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    private final void setName(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    private final void setEmail(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPhone() {
        return null;
    }
    
    private final void setPhone(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPassword() {
        return null;
    }
    
    private final void setPassword(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConfirmPassword() {
        return null;
    }
    
    private final void setConfirmPassword(java.lang.String p0) {
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    private final void setLoading(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<java.lang.String> getRegisterSuccess() {
        return null;
    }
    
    public final void onNameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void onEmailChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void onPhoneChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void onConfirmPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void register() {
    }
}