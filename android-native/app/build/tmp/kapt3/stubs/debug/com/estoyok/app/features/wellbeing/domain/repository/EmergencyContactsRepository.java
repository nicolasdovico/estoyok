package com.estoyok.app.features.wellbeing.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u0005H&J\u001c\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00040\u00032\u0006\u0010\t\u001a\u00020\nH&J\u001a\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\f0\u00040\u0003H&J\"\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00040\u00032\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\n0\fH&J$\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0006\u001a\u00020\u0005H&\u00a8\u0006\u0010"}, d2 = {"Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "", "createContact", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;", "contact", "deleteContact", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "id", "", "getContacts", "", "reorderContacts", "ids", "updateContact", "app_debug"})
public abstract interface EmergencyContactsRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>>> getContacts();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> createContact(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto>> updateContact(int id, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto contact);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> deleteContact(int id);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> reorderContacts(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> ids);
}