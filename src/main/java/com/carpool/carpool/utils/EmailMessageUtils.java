package com.carpool.carpool.utils;

public class EmailMessageUtils {

    private EmailMessageUtils() {}

    // WELCOME
    public static final String SUBJECT_EMAIL_WELCOME = "¡Bienvenido a Carpool!";
    public static final String TITLE_WELCOME = "¡Hola, {name}!\uD83D\uDC4B";
    public static final String MESSAGE_EMAIL_WELCOME = "¡Bienvenido a <strong>Carpool</strong>! Nos pone muy felices que te sumes a esta comunidad que cree en viajar juntos, compartir y hacer los trayectos más simples y humanos.<br>" +
            "A partir de ahora vas a poder conectarte con otras personas que viajan como vos, y organizar tus trayectos de forma práctica, segura y acompañada.<br><br>" +
            "Si tenés preguntas o sugerencias, <strong>estamos para ayudarte</strong>. ¡Esto recién empieza!<br><br>" +
            "<strong>¡Bienvenido a bordo! \uD83D\uDE97✨</strong>";
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

    // EMAIL CHANGE
    public static final String SUBJECT_EMAIL_CHANGE = "Verificación de nuevo correo electrónico";
    public static final String TITLE_EMAIL_CHANGE = "¡Hola, {name}!";
    public static final String MESSAGE_EMAIL_CHANGE = "Recibimos una solicitud para cambiar tu correo electrónico en <strong>Carpool</strong>.<br>" +
            "Para confirmar esta modificación, por favor hacé clic en el botón de abajo.<br><br>" +
            "Si no realizaste esta solicitud, podés ignorar este mensaje.";
    public static final String CONFIRM_EMAIL_CHANGE = "Confirmar nuevo correo";
    public static final String MESSAGE_FOOTER_EMAIL_CHANGE = "Este enlace estará disponible por <strong>48 horas</strong>. Después de ese tiempo, deberás solicitar nuevamente el cambio si aún lo deseás.";

    //RESERVATION
    public static final String SUBJECT_EMAIL_NEW_RESERVATION = "¡Nueva solicitud de reserva en tu viaje!";
    public static final String TITLE_NEW_RESERVATION = "¡Hola, {name}!";
    public static final String MESSAGE_NEW_RESERVATION = "¡Buenas noticias! Recibiste una nueva solicitud de reserva.<br>" +
            "El pasajero <strong>{passengerName}</strong> quiere unirse a tu viaje!<br><br>" +
            "Para gestionarla, por favor hacé clic en el botón de abajo.";
    public static final String BUTTON_NEW_RESERVATION = "Ver Solicitud";
    public static final String MESSAGE_FOOTER_NEW_RESERVATION = "Te recomendamos responder a la brevedad para asegurar la reserva del pasajero.<br>Gracias por utilizar <strong>Carpool</strong>.";


}
