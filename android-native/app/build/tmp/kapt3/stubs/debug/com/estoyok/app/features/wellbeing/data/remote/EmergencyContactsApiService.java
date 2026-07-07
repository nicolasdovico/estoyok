package com.estoyok.app.features.wellbeing.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\t\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\r0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J(\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\t\u001a\u00020\n2\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0014\u00a8\u0006\u0015"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/remote/EmergencyContactsApiService;", "", "createContact", "Lretrofit2/Response;", "Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;", "contact", "(Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteContact", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContacts", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reorderContacts", "request", "Lcom/estoyok/app/features/wellbeing/data/model/ReorderContactsRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/ReorderContactsRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateContact", "(ILcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface EmergencyContactsApiService {
    
    @retrofit2.http.GET(value = "emergency-contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getContacts(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>>> $completion);
    
    @retrofit2.http.POST(value = "emergency-contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createContact(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> $completion);
    
    @retrofit2.http.PUT(value = "emergency-contacts/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateContact(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> $completion);
    
    @retrofit2.http.DELETE(value = "emergency-contacts/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteContact(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.POST(value = "emergency-contacts/reorder")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object reorderContacts(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.ReorderContactsRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
}