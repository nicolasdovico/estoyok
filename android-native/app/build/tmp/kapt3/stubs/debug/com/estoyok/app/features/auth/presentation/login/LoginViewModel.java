package com.estoyok.app.features.auth.presentation.login;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010#\u001a\u00020\u0007J\u000e\u0010$\u001a\u00020\u00072\u0006\u0010%\u001a\u00020\tJ\u000e\u0010&\u001a\u00020\u00072\u0006\u0010%\u001a\u00020\tR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000f\u0010\u0010\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR/\u0010\u0011\u001a\u0004\u0018\u00010\t2\b\u0010\b\u001a\u0004\u0018\u00010\t8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0014\u0010\u0010\u001a\u0004\b\u0012\u0010\f\"\u0004\b\u0013\u0010\u000eR+\u0010\u0016\u001a\u00020\u00152\u0006\u0010\b\u001a\u00020\u00158F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001a\u0010\u0010\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00070\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR+\u0010\u001f\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\"\u0010\u0010\u001a\u0004\b \u0010\f\"\u0004\b!\u0010\u000e\u00a8\u0006\'"}, d2 = {"Lcom/estoyok/app/features/auth/presentation/login/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "(Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;)V", "_loginSuccess", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "<set-?>", "", "email", "getEmail", "()Ljava/lang/String;", "setEmail", "(Ljava/lang/String;)V", "email$delegate", "Landroidx/compose/runtime/MutableState;", "errorMessage", "getErrorMessage", "setErrorMessage", "errorMessage$delegate", "", "isLoading", "()Z", "setLoading", "(Z)V", "isLoading$delegate", "loginSuccess", "Lkotlinx/coroutines/flow/SharedFlow;", "getLoginSuccess", "()Lkotlinx/coroutines/flow/SharedFlow;", "password", "getPassword", "setPassword", "password$delegate", "login", "onEmailChange", "newValue", "onPasswordChange", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState email$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState password$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Unit> _loginSuccess = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> loginSuccess = null;
    
    @javax.inject.Inject()
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.domain.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    private final void setEmail(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPassword() {
        return null;
    }
    
    private final void setPassword(java.lang.String p0) {
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
    public final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> getLoginSuccess() {
        return null;
    }
    
    public final void onEmailChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String newValue) {
    }
    
    public final void login() {
    }
}