package com.estoyok.app.features.tracking.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/estoyok/app/features/tracking/data/remote/SubscriptionApiService;", "", "checkout", "Lretrofit2/Response;", "Lcom/estoyok/app/features/tracking/data/model/CheckoutResponse;", "request", "Lcom/estoyok/app/features/tracking/data/model/CheckoutRequest;", "(Lcom/estoyok/app/features/tracking/data/model/CheckoutRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface SubscriptionApiService {
    
    @retrofit2.http.POST(value = "subscriptions/checkout")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object checkout(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.CheckoutRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.CheckoutResponse>> $completion);
}