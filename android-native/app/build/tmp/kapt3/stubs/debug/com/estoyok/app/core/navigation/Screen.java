package com.estoyok.app.core.navigation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0007\r\u000e\u000f\u0010\u0011\u0012\u0013B\u001f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000b\u0082\u0001\u0007\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen;", "", "route", "", "title", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;)V", "getIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "getRoute", "()Ljava/lang/String;", "getTitle", "Ajustes", "Familia", "Login", "Mapa", "Panel", "Register", "VerifyEmail", "Lcom/estoyok/app/core/navigation/Screen$Ajustes;", "Lcom/estoyok/app/core/navigation/Screen$Familia;", "Lcom/estoyok/app/core/navigation/Screen$Login;", "Lcom/estoyok/app/core/navigation/Screen$Mapa;", "Lcom/estoyok/app/core/navigation/Screen$Panel;", "Lcom/estoyok/app/core/navigation/Screen$Register;", "Lcom/estoyok/app/core/navigation/Screen$VerifyEmail;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.ui.graphics.vector.ImageVector icon = null;
    
    private Screen(java.lang.String route, java.lang.String title, androidx.compose.ui.graphics.vector.ImageVector icon) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.graphics.vector.ImageVector getIcon() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Ajustes;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Ajustes extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Ajustes INSTANCE = null;
        
        private Ajustes() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Familia;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Familia extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Familia INSTANCE = null;
        
        private Familia() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Login;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Login extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Login INSTANCE = null;
        
        private Login() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Mapa;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Mapa extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Mapa INSTANCE = null;
        
        private Mapa() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Panel;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Panel extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Panel INSTANCE = null;
        
        private Panel() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$Register;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "app_debug"})
    public static final class Register extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.Register INSTANCE = null;
        
        private Register() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/estoyok/app/core/navigation/Screen$VerifyEmail;", "Lcom/estoyok/app/core/navigation/Screen;", "()V", "createRoute", "", "email", "app_debug"})
    public static final class VerifyEmail extends com.estoyok.app.core.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.estoyok.app.core.navigation.Screen.VerifyEmail INSTANCE = null;
        
        private VerifyEmail() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        java.lang.String email) {
            return null;
        }
    }
}