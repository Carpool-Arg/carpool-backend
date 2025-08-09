package com.carpool.carpool.utils;

public class EmailMessageUtils {
    private EmailMessageUtils() {}

    // WELCOME
    public static final String SUBJECT_EMAIL_WELCOME = "¡Bienvenido a Carpool!";
    public static final String TITLE_WELCOME = "¡Hola, {name}!\uD83D\uDC4B";
    public static final String MESSAGE_EMAIL_WELCOME = " Bienvenido a Carpool, la plataforma para compartir viajes de forma fácil, segura y organizada.<br>" +
            "Desde ahora vas a poder publicar, encontrar y coordinar viajes a distintas ciudades sin depender de los grupos de WhatsApp.<br>" +
            "¡Nos alegra tenerte a bordo! \uD83D\uDE97";
    public static final String MESSAGE_FOOTER_WELCOME = "El equipo de Carpool";

    //CHANGE PASSWORD   
    public static final String SUBJECT_EMAIL_CHANGE_PASSWORD = "Recuperación de contraseña";
    public static final String TITLE_CHANGE_PASSWORD = "{name}, ¿Olvidaste tu contraseña? 🔒";
    public static final String MESSAGE_EMAIL_CHANGE_PASSWORD = "Haz clic en el botón de abajo para restablecer tu contraseña:";
    public static final String CONFIRM_CHANGE_PASSWORD = "Restablecer contraseña";
    public static final String MESSAGE_FOOTER_CHANGE_PASSWORD = "Si no solicitaste este cambio, podés ignorar este correo. Recuerda que el mismo es válido durante <strong>30 minutos</strong>.";


    // ACTIVE
    public static final String SUBJECT_EMAIL_ACTIVE = "Activación de cuenta";
    public static final String TITLE_ACTIVE = "¡Casi listo, {name}!\uD83D\uDE4C";
    public static final String MESSAGE_EMAIL_ACTIVE = "Haz clic en el botón de abajo para activar tu cuenta:";
    public static final String ACTIVE = "Activar cuenta";
    public static final String MESSAGE_FOOTER_ACTIVE = "Si no solicitaste esta activación, podés ignorar este correo. Recuerda que el mismo es válido durante <strong>48 horas</strong>.";

    // LOCKED
    public static final String SUBJECT_EMAIL_LOCKED = "Bloqueo de cuenta";
    public static final String TITLE_LOCKED = "Tu cuenta ha sido bloqueada, {name}";
    public static final String MESSAGE_EMAIL_LOCKED = "Por cuestiones de seguridad, hemos bloqueado el acceso a tu cuenta debido a múltiples intentos fallidos de inicio de sesión.<br>" +
            "Si usted es quien intentó acceder, puede desbloquear la misma creando una nueva contraseña haciendo clic en el botón de abajo.<br>" +
            "Si <b>no realizaste estos intentos</b>, por favor comunicate de inmediato con <a href='mailto:%s'>nuestro equipo de soporte</a>.";
    public static final String UNLOCKED = "Desbloquear cuenta";
    public static final String MESSAGE_FOOTER_LOCKED = "El enlace para desbloquear su cuenta es válido durante <strong>48 horas</strong>.";
}
