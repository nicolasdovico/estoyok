package com.estoyok.app.features.tracking.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J(\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\t2\b\b\u0001\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\f\u00a8\u0006\r"}, d2 = {"Lcom/estoyok/app/features/tracking/data/remote/SosApiService;", "", "triggerSos", "Lretrofit2/Response;", "Lcom/estoyok/app/features/tracking/data/model/SosAlertResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadAudio", "Lcom/estoyok/app/features/tracking/data/model/AudioUploadResponse;", "id", "", "audio", "Lokhttp3/MultipartBody$Part;", "(Ljava/lang/String;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface SosApiService {
    
    @retrofit2.http.POST(value = "emergency-alerts/sos")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object triggerSos(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.SosAlertResponse>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "emergency-alerts/{id}/audio")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadAudio(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part audio, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.AudioUploadResponse>> $completion);
}