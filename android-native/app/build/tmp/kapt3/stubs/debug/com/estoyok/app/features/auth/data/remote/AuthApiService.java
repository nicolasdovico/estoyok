package com.estoyok.app.features.auth.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/estoyok/app/features/auth/data/remote/AuthApiService;", "", "login", "Lretrofit2/Response;", "Lcom/estoyok/app/features/auth/data/model/AuthResponse;", "request", "Lcom/estoyok/app/features/auth/data/model/LoginRequest;", "(Lcom/estoyok/app/features/auth/data/model/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/estoyok/app/features/auth/data/model/RegisterRequest;", "(Lcom/estoyok/app/features/auth/data/model/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resendOtp", "Lcom/estoyok/app/features/auth/data/model/ResendOtpRequest;", "(Lcom/estoyok/app/features/auth/data/model/ResendOtpRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyEmail", "Lcom/estoyok/app/features/auth/data/model/VerifyEmailRequest;", "(Lcom/estoyok/app/features/auth/data/model/VerifyEmailRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface AuthApiService {
    
    @retrofit2.http.POST(value = "register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.POST(value = "login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "verify-email")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyEmail(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.VerifyEmailRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "resend-otp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resendOtp(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.model.ResendOtpRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.POST(value = "logout")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logout(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
}