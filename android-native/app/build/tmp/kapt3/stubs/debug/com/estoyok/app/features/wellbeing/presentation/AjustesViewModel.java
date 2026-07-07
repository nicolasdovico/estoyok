package com.estoyok.app.features.wellbeing.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b1\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\u0016\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010]\u001a\u00020^J\u0006\u0010_\u001a\u00020^J\u000e\u0010`\u001a\u00020^2\u0006\u0010a\u001a\u00020\u0010J\u0006\u0010b\u001a\u00020^J\u0006\u0010c\u001a\u00020^J\u000e\u0010d\u001a\u00020^2\u0006\u0010e\u001a\u00020\u0010J\u000e\u0010f\u001a\u00020^2\u0006\u0010e\u001a\u00020\u0010J\u001e\u0010g\u001a\u00020^2\u0006\u0010h\u001a\u00020\b2\u0006\u0010i\u001a\u00020 2\u0006\u0010j\u001a\u00020\bJ\u000e\u0010k\u001a\u00020^2\u0006\u0010l\u001a\u00020\u0010J\u001e\u0010m\u001a\u00020^2\u0006\u0010n\u001a\u00020\b2\u0006\u0010o\u001a\u00020 2\u0006\u0010p\u001a\u00020 J\u000e\u0010q\u001a\u00020^2\u0006\u0010n\u001a\u00020\bJ\u0016\u0010r\u001a\u00020^2\f\u0010s\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018H\u0002R+\u0010\t\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR+\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0007\u001a\u00020\u00108F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R7\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00190\u00182\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00190\u00188F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001f\u0010\u000f\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010!\u001a\u0004\u0018\u00010 2\b\u0010\u0007\u001a\u0004\u0018\u00010 8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b&\u0010\u000f\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R+\u0010\'\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b)\u0010\u000f\u001a\u0004\b\'\u0010\u000b\"\u0004\b(\u0010\rR/\u0010*\u001a\u0004\u0018\u00010 2\b\u0010\u0007\u001a\u0004\u0018\u00010 8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b-\u0010\u000f\u001a\u0004\b+\u0010#\"\u0004\b,\u0010%R+\u0010.\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b1\u0010\u000f\u001a\u0004\b/\u0010#\"\u0004\b0\u0010%R+\u00102\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b5\u0010\u000f\u001a\u0004\b3\u0010#\"\u0004\b4\u0010%R+\u00106\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b9\u0010\u000f\u001a\u0004\b7\u0010#\"\u0004\b8\u0010%R+\u0010:\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b=\u0010\u000f\u001a\u0004\b;\u0010#\"\u0004\b<\u0010%R+\u0010>\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bA\u0010\u000f\u001a\u0004\b?\u0010\u000b\"\u0004\b@\u0010\rR+\u0010B\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bE\u0010\u000f\u001a\u0004\bC\u0010#\"\u0004\bD\u0010%R+\u0010F\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bI\u0010\u000f\u001a\u0004\bG\u0010#\"\u0004\bH\u0010%R+\u0010J\u001a\u00020 2\u0006\u0010\u0007\u001a\u00020 8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bM\u0010\u000f\u001a\u0004\bK\u0010#\"\u0004\bL\u0010%R+\u0010N\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bQ\u0010\u000f\u001a\u0004\bO\u0010\u000b\"\u0004\bP\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010S\u001a\u0004\u0018\u00010R2\b\u0010\u0007\u001a\u0004\u0018\u00010R8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bX\u0010\u000f\u001a\u0004\bT\u0010U\"\u0004\bV\u0010WR+\u0010Y\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\\\u0010\u000f\u001a\u0004\bZ\u0010\u000b\"\u0004\b[\u0010\r\u00a8\u0006t"}, d2 = {"Lcom/estoyok/app/features/wellbeing/presentation/AjustesViewModel;", "Landroidx/lifecycle/ViewModel;", "settingsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;", "contactsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "(Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;)V", "<set-?>", "", "allowSmsWhatsappCheckin", "getAllowSmsWhatsappCheckin", "()Z", "setAllowSmsWhatsappCheckin", "(Z)V", "allowSmsWhatsappCheckin$delegate", "Landroidx/compose/runtime/MutableState;", "", "checkinIntervalHours", "getCheckinIntervalHours", "()I", "setCheckinIntervalHours", "(I)V", "checkinIntervalHours$delegate", "Landroidx/compose/runtime/MutableIntState;", "", "Lcom/estoyok/app/features/wellbeing/data/model/EmergencyContactDto;", "contacts", "getContacts", "()Ljava/util/List;", "setContacts", "(Ljava/util/List;)V", "contacts$delegate", "", "errorMessage", "getErrorMessage", "()Ljava/lang/String;", "setErrorMessage", "(Ljava/lang/String;)V", "errorMessage$delegate", "isLoading", "setLoading", "isLoading$delegate", "messageSuccess", "getMessageSuccess", "setMessageSuccess", "messageSuccess$delegate", "newContactEmail", "getNewContactEmail", "setNewContactEmail", "newContactEmail$delegate", "newContactName", "getNewContactName", "setNewContactName", "newContactName$delegate", "newContactPhone", "getNewContactPhone", "setNewContactPhone", "newContactPhone$delegate", "newContactRelationship", "getNewContactRelationship", "setNewContactRelationship", "newContactRelationship$delegate", "quietHoursEnabled", "getQuietHoursEnabled", "setQuietHoursEnabled", "quietHoursEnabled$delegate", "quietHoursEnd", "getQuietHoursEnd", "setQuietHoursEnd", "quietHoursEnd$delegate", "quietHoursStart", "getQuietHoursStart", "setQuietHoursStart", "quietHoursStart$delegate", "safeWifiSsid", "getSafeWifiSsid", "setSafeWifiSsid", "safeWifiSsid$delegate", "sensorCheckinEnabled", "getSensorCheckinEnabled", "setSensorCheckinEnabled", "sensorCheckinEnabled$delegate", "Lcom/estoyok/app/features/auth/data/model/UserDto;", "userProfile", "getUserProfile", "()Lcom/estoyok/app/features/auth/data/model/UserDto;", "setUserProfile", "(Lcom/estoyok/app/features/auth/data/model/UserDto;)V", "userProfile$delegate", "wifiCheckinEnabled", "getWifiCheckinEnabled", "setWifiCheckinEnabled", "wifiCheckinEnabled$delegate", "addContact", "", "clearMessages", "deleteContact", "id", "loadContacts", "loadSettings", "moveContactDown", "index", "moveContactUp", "saveAutomationSettings", "wifiEnabled", "ssid", "sensorEnabled", "saveCheckinInterval", "hours", "saveQuietHoursSettings", "enabled", "start", "end", "toggleSmsWhatsapp", "updatePriorityOrder", "newList", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AjustesViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository contactsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState userProfile$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableIntState checkinIntervalHours$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState quietHoursEnabled$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState quietHoursStart$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState quietHoursEnd$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState allowSmsWhatsappCheckin$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState wifiCheckinEnabled$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState safeWifiSsid$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState sensorCheckinEnabled$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState contacts$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState newContactName$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState newContactPhone$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState newContactEmail$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState newContactRelationship$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState messageSuccess$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    
    @javax.inject.Inject()
    public AjustesViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository contactsRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.estoyok.app.features.auth.data.model.UserDto getUserProfile() {
        return null;
    }
    
    private final void setUserProfile(com.estoyok.app.features.auth.data.model.UserDto p0) {
    }
    
    public final int getCheckinIntervalHours() {
        return 0;
    }
    
    private final void setCheckinIntervalHours(int p0) {
    }
    
    public final boolean getQuietHoursEnabled() {
        return false;
    }
    
    private final void setQuietHoursEnabled(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getQuietHoursStart() {
        return null;
    }
    
    private final void setQuietHoursStart(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getQuietHoursEnd() {
        return null;
    }
    
    private final void setQuietHoursEnd(java.lang.String p0) {
    }
    
    public final boolean getAllowSmsWhatsappCheckin() {
        return false;
    }
    
    private final void setAllowSmsWhatsappCheckin(boolean p0) {
    }
    
    public final boolean getWifiCheckinEnabled() {
        return false;
    }
    
    private final void setWifiCheckinEnabled(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSafeWifiSsid() {
        return null;
    }
    
    private final void setSafeWifiSsid(java.lang.String p0) {
    }
    
    public final boolean getSensorCheckinEnabled() {
        return false;
    }
    
    private final void setSensorCheckinEnabled(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto> getContacts() {
        return null;
    }
    
    private final void setContacts(java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNewContactName() {
        return null;
    }
    
    public final void setNewContactName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNewContactPhone() {
        return null;
    }
    
    public final void setNewContactPhone(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNewContactEmail() {
        return null;
    }
    
    public final void setNewContactEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNewContactRelationship() {
        return null;
    }
    
    public final void setNewContactRelationship(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    private final void setLoading(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMessageSuccess() {
        return null;
    }
    
    private final void setMessageSuccess(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    public final void loadSettings() {
    }
    
    public final void loadContacts() {
    }
    
    public final void saveCheckinInterval(int hours) {
    }
    
    public final void saveQuietHoursSettings(boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String start, @org.jetbrains.annotations.NotNull()
    java.lang.String end) {
    }
    
    public final void toggleSmsWhatsapp(boolean enabled) {
    }
    
    public final void saveAutomationSettings(boolean wifiEnabled, @org.jetbrains.annotations.NotNull()
    java.lang.String ssid, boolean sensorEnabled) {
    }
    
    public final void addContact() {
    }
    
    public final void deleteContact(int id) {
    }
    
    public final void moveContactUp(int index) {
    }
    
    public final void moveContactDown(int index) {
    }
    
    private final void updatePriorityOrder(java.util.List<com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto> newList) {
    }
    
    public final void clearMessages() {
    }
}