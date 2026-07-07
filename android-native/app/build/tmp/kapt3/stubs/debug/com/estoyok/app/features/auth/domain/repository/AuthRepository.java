package com.estoyok.app.features.auth.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006H&J\u001c\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00062\u0006\u0010\u000b\u001a\u00020\fH&J\u0014\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\t0\u0006H&J\u001c\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\t0\u00062\u0006\u0010\u000b\u001a\u00020\u0010H&J\u001c\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\t0\u00062\u0006\u0010\u000b\u001a\u00020\u0012H&J0\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0014\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\u00072\u0006\u0010\u0016\u001a\u00020\u00072\b\u0010\u0017\u001a\u0004\u0018\u00010\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00062\u0006\u0010\u000b\u001a\u00020\u001aH&\u00a8\u0006\u001b"}, d2 = {"Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "", "clearSession", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAuthToken", "Lkotlinx/coroutines/flow/Flow;", "", "login", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/auth/data/model/AuthResponse;", "request", "Lcom/estoyok/app/features/auth/data/model/LoginRequest;", "logout", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "register", "Lcom/estoyok/app/features/auth/data/model/RegisterRequest;", "resendOtp", "Lcom/estoyok/app/features/auth/data/model/ResendOtpRequest;", "saveSession", "token", "name", "email", "phone", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyEmail", "Lcom/estoyok/app/features/auth/data/model/VerifyEmailRequest;", "app_debug"})
public abstract interface AuthRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> register(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.RegisterRequest request);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.AuthResponse>> login(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.LoginRequest request);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.AuthResponse>> verifyEmail(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.VerifyEmailRequest request);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> resendOtp(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.ResendOtpRequest request);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> logout();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.String> getAuthToken();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object saveSession(@org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearSession(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}