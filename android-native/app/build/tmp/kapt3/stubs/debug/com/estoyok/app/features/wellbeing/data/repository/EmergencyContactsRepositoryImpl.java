package com.estoyok.app.features.wellbeing.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\bH\u0016J\u001c\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00070\u00062\u0006\u0010\f\u001a\u00020\rH\u0016J\u001a\u0010\u000e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u000f0\u00070\u0006H\u0016J\u0014\u0010\u0010\u001a\u00020\u00112\n\u0010\u0012\u001a\u0006\u0012\u0002\b\u00030\u0013H\u0002J\"\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00070\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\r0\u000fH\u0016JC\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00170\u00070\u0006\"\u0004\b\u0000\u0010\u00172\"\u0010\u0018\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00170\u00130\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\u0019H\u0002\u00a2\u0006\u0002\u0010\u001cJ$\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\bH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/repository/EmergencyContactsRepositoryImpl;", "Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "apiService", "Lcom/estoyok/app/features/wellbeing/data/remote/EmergencyContactsApiService;", "(Lcom/estoyok/app/features/wellbeing/data/remote/EmergencyContactsApiService;)V", "createContact", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;", "contact", "deleteContact", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "id", "", "getContacts", "", "parseErrorMessage", "", "response", "Lretrofit2/Response;", "reorderContacts", "ids", "safeApiCall", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "updateContact", "app_debug"})
public final class EmergencyContactsRepositoryImpl implements com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.data.remote.EmergencyContactsApiService apiService = null;
    
    @javax.inject.Inject()
    public EmergencyContactsRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.remote.EmergencyContactsApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>>> getContacts() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> createContact(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> updateContact(int id, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> deleteContact(int id) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> reorderContacts(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> ids) {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}