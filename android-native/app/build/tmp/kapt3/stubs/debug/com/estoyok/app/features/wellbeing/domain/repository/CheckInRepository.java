package com.estoyok.app.features.wellbeing.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u0007H&J\u001a\u0010\b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00040\u0003H&\u00a8\u0006\u000b"}, d2 = {"Lcom/estoyok/app/features/wellbeing/domain/repository/CheckInRepository;", "", "checkIn", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInResponse;", "source", "", "getCheckIns", "", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInDto;", "app_debug"})
public abstract interface CheckInRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.CheckInResponse>> checkIn(@org.jetbrains.annotations.NotNull()
    java.lang.String source);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto>>> getCheckIns();
}