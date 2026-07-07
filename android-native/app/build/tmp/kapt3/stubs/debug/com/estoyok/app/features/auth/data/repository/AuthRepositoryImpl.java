package com.estoyok.app.features.auth.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0010\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u000bH\u0016J\u001c\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u000b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\u0014\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u000e0\u000bH\u0016J\u0014\u0010\u0014\u001a\u00020\f2\n\u0010\u0015\u001a\u0006\u0012\u0002\b\u00030\u0016H\u0002J\u001c\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u000e0\u000b2\u0006\u0010\u0010\u001a\u00020\u0018H\u0016J\u001c\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u000e0\u000b2\u0006\u0010\u0010\u001a\u00020\u001aH\u0016JC\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u001c0\u000e0\u000b\"\u0004\b\u0000\u0010\u001c2\"\u0010\u001d\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u001c0\u00160\u001f\u0012\u0006\u0012\u0004\u0018\u00010 0\u001eH\u0002\u00a2\u0006\u0002\u0010!J0\u0010\"\u001a\u00020\b2\u0006\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020\f2\u0006\u0010%\u001a\u00020\f2\b\u0010&\u001a\u0004\u0018\u00010\fH\u0096@\u00a2\u0006\u0002\u0010\'J\u001c\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u000b2\u0006\u0010\u0010\u001a\u00020)H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/estoyok/app/features/auth/data/repository/AuthRepositoryImpl;", "Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "apiService", "Lcom/estoyok/app/features/auth/data/remote/AuthApiService;", "sessionManager", "Lcom/estoyok/app/core/data/local/SessionManager;", "(Lcom/estoyok/app/features/auth/data/remote/AuthApiService;Lcom/estoyok/app/core/data/local/SessionManager;)V", "clearSession", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAuthToken", "Lkotlinx/coroutines/flow/Flow;", "", "login", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/auth/data/model/AuthResponse;", "request", "Lcom/estoyok/app/features/auth/data/model/LoginRequest;", "logout", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "parseErrorMessage", "response", "Lretrofit2/Response;", "register", "Lcom/estoyok/app/features/auth/data/model/RegisterRequest;", "resendOtp", "Lcom/estoyok/app/features/auth/data/model/ResendOtpRequest;", "safeApiCall", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "saveSession", "token", "name", "email", "phone", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyEmail", "Lcom/estoyok/app/features/auth/data/model/VerifyEmailRequest;", "app_debug"})
public final class AuthRepositoryImpl implements com.estoyok.app.features.auth.domain.repository.AuthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.auth.data.remote.AuthApiService apiService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.core.data.local.SessionManager sessionManager = null;
    
    @javax.inject.Inject()
    public AuthRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.remote.AuthApiService apiService, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.core.data.local.SessionManager sessionManager) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> register(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.RegisterRequest request) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.AuthResponse>> login(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.LoginRequest request) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.AuthResponse>> verifyEmail(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.VerifyEmailRequest request) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> resendOtp(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.ResendOtpRequest request) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> logout() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.String> getAuthToken() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object saveSession(@org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object clearSession(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}