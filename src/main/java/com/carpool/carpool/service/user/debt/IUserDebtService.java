package com.carpool.carpool.service.user.debt;

import com.carpool.carpool.dto.user.UserDebtResponseDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.response.Response;

/**
 * Servicio interno encargado de gestionar y consultar el estado de deuda de los usuarios.
 *
 * <p>
 * Las implementaciones de este servicio deben definir los criterios necesarios para determinar
 * si un usuario posee deudas pendientes dentro del sistema.
 * </p>
 *
 */
public interface IUserDebtService {
    /**
     * Determina si un usuario es deudor. Metodo sobrecargado para usarlo desde el controlador
     * Cuando ya hay una sesion activa.
     *
     * <p>
     * Un usuario será considerado deudor si cumple con las condiciones de deuda definidas
     * por la implementación concreta del servicio (por ejemplo, reservas en estado UNPAID,
     * reservas vencidas, etc.).
     * </p>
     *
     *
     * @return {@code true} si el usuario posee deuda pendiente,
     *         {@code false} en caso contrario
     */
    Response<UserDebtResponseDTO> isDebtor();
}
