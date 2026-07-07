package com.estoyok.app.features.tracking.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nH\u0016J\u0014\u0010\u000b\u001a\u00020\n2\n\u0010\f\u001a\u0006\u0012\u0002\b\u00030\rH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/estoyok/app/features/tracking/data/repository/SubscriptionRepositoryImpl;", "Lcom/estoyok/app/features/tracking/domain/repository/SubscriptionRepository;", "apiService", "Lcom/estoyok/app/features/tracking/data/remote/SubscriptionApiService;", "(Lcom/estoyok/app/features/tracking/data/remote/SubscriptionApiService;)V", "checkout", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/tracking/data/model/CheckoutResponse;", "provider", "", "parseErrorMessage", "response", "Lretrofit2/Response;", "app_debug"})
public final class SubscriptionRepositoryImpl implements com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.data.remote.SubscriptionApiService apiService = null;
    
    @javax.inject.Inject()
    public SubscriptionRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.remote.SubscriptionApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.CheckoutResponse>> checkout(@org.jetbrains.annotations.NotNull()
    java.lang.String provider) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}