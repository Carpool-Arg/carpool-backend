package com.carpool.carpool.utils;

public class EmailMessageUtils {

    private EmailMessageUtils() {}

    // COMMON
    public static final String TITLE_GREETING = "¡Hola, {name}!";

    // WELCOME
    public static final String SUBJECT_EMAIL_WELCOME = "¡Bienvenido a Carpool!";
    public static final String TITLE_WELCOME = "¡Hola, {name}!\uD83D\uDC4B";
    public static final String MESSAGE_EMAIL_WELCOME = "¡Bienvenido a <strong>Carpool</strong>! Nos pone muy felices que te sumes a esta comunidad que cree en viajar juntos, compartir y hacer los trayectos más simples y humanos.<br>"
                    +
                    "A partir de ahora vas a poder conectarte con otras personas que viajan como vos, y organizar tus trayectos de forma práctica, segura y acompañada.<br><br>"
                    +
                    "Si tenés preguntas o sugerencias, <strong>estamos para ayudarte</strong>. ¡Esto recién empieza!<br><br>"
                    +
                    "<strong>¡Bienvenido a bordo! \uD83D\uDE97✨</strong>";
    public static final String MESSAGE_FOOTER_WELCOME = "El equipo de Carpool";

    // CHANGE PASSWORD
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
    public static final String MESSAGE_EMAIL_LOCKED = "Por cuestiones de seguridad, hemos bloqueado el acceso a tu cuenta debido a múltiples intentos fallidos de inicio de sesión.<br>"
                    +
                    "Si usted es quien intentó acceder, puede desbloquear la misma creando una nueva contraseña haciendo clic en el botón de abajo.<br>"
                    +
                    "Si <b>no realizaste estos intentos</b>, por favor comunicate de inmediato con <a href='mailto:%s'>nuestro equipo de soporte</a>.";
    public static final String UNLOCKED = "Desbloquear cuenta";
    public static final String MESSAGE_FOOTER_LOCKED = "El enlace para desbloquear su cuenta es válido durante <strong>48 horas</strong>.";

    // EMAIL CHANGE
    public static final String SUBJECT_EMAIL_CHANGE = "Verificación de nuevo correo electrónico";
    public static final String MESSAGE_EMAIL_CHANGE = "Recibimos una solicitud para cambiar tu correo electrónico en <strong>Carpool</strong>.<br>" +
            "Para confirmar esta modificación, por favor hacé clic en el botón de abajo.<br><br>" +
            "Si no realizaste esta solicitud, podés ignorar este mensaje.";
    public static final String CONFIRM_EMAIL_CHANGE = "Confirmar nuevo correo";
    public static final String MESSAGE_FOOTER_EMAIL_CHANGE = "Este enlace estará disponible por <strong>48 horas</strong>. Después de ese tiempo, deberás solicitar nuevamente el cambio si aún lo deseás.";

    //RESERVATION
    public static final String SUBJECT_EMAIL_NEW_RESERVATION = "¡Nueva solicitud de reserva en tu viaje!";
    public static final String MESSAGE_NEW_RESERVATION = "¡Buenas noticias! Recibiste una nueva solicitud de reserva.<br>" +
            "El pasajero <strong>{passengerName}</strong> quiere unirse a tu viaje!<br><br>" +
            "Para gestionarla, por favor hacé clic en el botón de abajo.";
    public static final String BUTTON_NEW_RESERVATION = "Ver Solicitud";
    public static final String MESSAGE_FOOTER_NEW_RESERVATION = "Te recomendamos responder a la brevedad para asegurar la reserva del pasajero.<br>Gracias por utilizar <strong>Carpool</strong>.";

    //RESERVATION ACCEPTED
    public static final String SUBJECT_EMAIL_RESERVATION_ACCEPTED = "¡Tu solicitud de reserva ha sido aceptada!";
    public static final String MESSAGE_RESERVATION_ACCEPTED ="\uD83E\uDD73 ¡Buenas noticias! Tu reserva para el viaje <strong>{origin}</strong> → <strong>{destination}</strong> fue aceptada por <strong>{driverName}</strong>.<br><br>" +
            "Ya tenés tu lugar confirmado. ¡Gracias por elegir <strong>Carpool</strong> y buen viaje!";
    public static final String MESSAGE_FOOTER_RESERVATION_ACCEPTED = "El equipo de Carpool";

    //RESERVATION REJECTED
    public static final String SUBJECT_EMAIL_RESERVATION_REJECTED = "Tu solicitud de reserva ha sido rechazada";
    public static final String MESSAGE_RESERVATION_REJECTED ="Lamentablemente, tu solicitud de reserva para el viaje <strong>{origin}</strong> → <strong>{destination}</strong> fue rechazada por <strong>{driverName}</strong>.<br><br>" +
            "Gracias por tu comprensión.<br>" +
           "Esperamos que pronto encuentres otro viaje que se adapte a vos \uD83D\uDE4C";
    public static final String MESSAGE_FOOTER_RESERVATION_REJECTED = "El equipo de Carpool";

    // RESERVATION UNPAID - VIAJE REALIZADO
    public static final String SUBJECT_EMAIL_RESERVATION_UNPAID = "Tenés un pago pendiente por un viaje realizado";

    public static final String TITLE_RESERVATION_UNPAID = "Hola {name},";

    public static final String MESSAGE_RESERVATION_UNPAID = "Detectamos un pago pendiente correspondiente a un viaje que ya realizaste.<br><br>" +
                    "El monto a abonar es de <strong>ARS {total}</strong>.<br><br>" +
                    "Para poder seguir utilizando <strong>Carpool</strong> y acceder a nuevos viajes, necesitás regularizar este pago.";

    public static final String BUTTON_RESERVATION_UNPAID = "Pagar viaje";

    public static final String MESSAGE_FOOTER_RESERVATION_UNPAID = "Hasta que el pago sea realizado, algunas funcionalidades de la aplicación permanecerán bloqueadas.<br>" +
            "Si el pago no es realizado en el plazo estimado, el mismo pasará a estar vencido y se bloqueará su cuenta de CARPOOL..<br>" +
                    "Gracias por ayudarnos a mantener una comunidad justa y confiable.";

    // RESERVATION PAID
    public static final String SUBJECT_EMAIL_RESERVATION_PAID =
            "Un pasajero pagó su reserva 🎉";

    public static final String TITLE_RESERVATION_PAID =
            "Hola {name},";

    public static final String MESSAGE_RESERVATION_PAID =
            "Queremos informarte que el pasajero {name} ya realizó el pago correspondiente a una reserva de tu viaje.<br><br>" +
                    "El monto acreditado es de <strong>ARS {total}</strong>.<br><br>" +
                    "El pago fue procesado correctamente y ya se encuentra registrado en <strong>Carpool</strong>.";

    public static final String MESSAGE_FOOTER_RESERVATION_PAID =
            "Podés consultar el detalle del viaje y el estado de tus reservas desde la aplicación.<br>" +
                    "Gracias por confiar en <strong>Carpool</strong> y por ser parte de una comunidad de viajes compartidos.";

    // TRIP FULL
    public static final String SUBJECT_TRIP_FULL = "¡Tu viaje alcanzo el cupo completo!";
    public static final String MESSAGE_TRIP_FULL = "¡Buenas noticias! Tu viaje de <strong>{origin}</strong> a <strong>{destination}</strong>, programado para el día <strong>{date}</strong> ha sido cerrado ya que se completó el cupo.<br><br>" +
            "<em>¡Te deseamos un excelente trayecto!</em>";
    public static final String MESSAGE_FOOTER_TRIP_FULL = "Gracias por ser parte de la comunidad <strong>Carpool</strong>.";

    // TRIP CLOSED AUTOMATICALLY
    public static final String SUBJECT_TRIP_CLOSED_AUTOMATICALLY = "Tu viaje ya esta listo para arrancar!";
    public static final String TITLE_TRIP_CLOSED_AUTOMATICALLY = "¡Hola, {name}!";
    public static final String MESSAGE_TRIP_CLOSED_AUTOMATICALLY = "Te informamos que tu viaje desde <strong>{origin}</strong> con destino a <strong>{destination}</strong> ha sido cerrado para nuevas reservas.<br><br>" +
            "Esto sucede porque estamos cerca de la hora de salida. ¡Es momento de preparar todo para el trayecto! 🚗✨";
    public static final String MESSAGE_FOOTER_TRIP_CLOSED_AUTOMATICALLY = "Gracias por ser parte de la comunidad <strong>Carpool</strong>.";


// --- TRIP_STARTED ---
    public static final String PUSH_TITLE_TRIP_STARTED = "🚗 ¡Viaje en marcha!";
    public static final String PUSH_BODY_TRIP_STARTED = "El conductor %s ha iniciado el viaje desde %s. ¡Prepárate!";
    public static final String SUBJECT_TRIP_STARTED = "¡Tu viaje ha comenzado! - Carpool";
    public static final String TITLE_TRIP_STARTED = "Hola %s, tu viaje está en curso";
    public static final String MESSAGE_TRIP_STARTED = "Te informamos que el conductor %s ha iniciado el recorrido desde %s con destino a %s.";
    public static final String FOOTER_TRIP_STARTED = "Asegúrate de estar en el punto de encuentro acordado a tiempo.";

    // --- TRIP_CANCELLED_BY_SYSTEM ---
    public static final String PUSH_TITLE_TRIP_CANCELLED_BY_SYSTEM = "⚠️ Viaje cancelado";
    public static final String PUSH_BODY_TRIP_CANCELLED_BY_SYSTEM = "Tu viaje hacia %s se canceló automáticamente por demora del conductor.";
    public static final String SUBJECT_TRIP_CANCELLED_BY_SYSTEM = "Aviso de Viaje Cancelado - Carpool";
    public static final String TITLE_TRIP_CANCELLED_BY_SYSTEM = "Lo sentimos, tu reserva ha sido cancelada";
    public static final String MESSAGE_TRIP_CANCELLED_BY_SYSTEM = "El viaje con destino a %s ha sido cancelado por el sistema debido a que no se inició en el tiempo previsto (máximo 15 min de demora).";
    public static final String FOOTER_TRIP_CANCELLED_BY_SYSTEM = "Puedes buscar nuevas opciones de viaje en la aplicación.";

    // --- TRIP CANCELLED ---
    public static final String SUBJECT_TRIP_CANCELLED = "Aviso de Viaje Cancelado - Carpool";
    public static final String TITLE_TRIP_CANCELLED = "Lamentamos informarte que tu reserva ha sido cancelada";
    public static final String MESSAGE_TRIP_CANCELLED = "El viaje que tenías reservado desde <strong>{origin}</strong> con destino a <strong>{destination}</strong> fue cancelado por el conductor {driver}.";
    public static final String MESSAGE_TRIP_CANCELLED_REASON = " El motivo informado fue el siguiente: {reason}.";
    public static final String FOOTER_TRIP_CANCELLED = "Sabemos que esto puede generar inconvenientes. Te invitamos a buscar otro viaje disponible o a contactarnos si necesitas ayuda.";

    // --- RESERVATION CANCELLED BY DRIVER ---
    public static final String SUBJECT_RESERVATION_CANCELLED_BY_DRIVER = "Reserva Cancelada por el Conductor - Carpool";
    public static final String TITLE_RESERVATION_CANCELLED_BY_DRIVER = "El conductor ha cancelado tu reserva";
    public static final String MESSAGE_RESERVATION_CANCELLED_BY_DRIVER = "El conductor {driver} ha cancelado tu reserva para el viaje desde <strong>{origin}</strong> con destino a <strong>{destination}</strong>.";
    public static final String MESSAGE_RESERVATION_CANCELLED_BY_DRIVER_REASON = " El motivo informado fue el siguiente: {reason}.";
    public static final String FOOTER_RESERVATION_CANCELLED_BY_DRIVER = "Recuerda que puedes volver a solicitar un lugar en el viaje. Lamentamos los inconvenientes ocasionados. Si necesitás ayuda, no dudes en contactarnos.";
    // --- RESERVATION_CANCELLED_BY_SYSTEM ---
    public static final String SUBJECT_EMAIL_RESERVATION_CANCELLED_SYSTEM = "Tu solicitud de reserva ha sido cancelada";
    public static final String MESSAGE_RESERVATION_CANCELLED_SYSTEM = "Te informamos que tu solicitud de reserva para el viaje <strong>{origin}</strong> → <strong>{destination}</strong> ha sido cancelada automáticamente.<br><br>" +
            "Esto se debe a que el viaje ha sido **cerrado** para nuevas admisiones al estar próximo a su hora de salida.<br>" +
            "¡Te invitamos a buscar otros viajes disponibles en la plataforma! \uD83D\uDE4C";
    public static final String MESSAGE_FOOTER_RESERVATION_CANCELLED_SYSTEM = "El equipo de Carpool";
}
