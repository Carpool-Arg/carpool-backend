package com.carpool.carpool.service.statistics.admin.general;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminCo2StatDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminTripMonthlyStatDTO;
import com.carpool.carpool.response.Response;

public interface IAdminStatsService {
    /**
     * Obtiene las ganancias recaudadas por la aplicación en un rango de fechas.
     * Incluye el total histórico y el total filtrado por el rango indicado.
     * @param fromDate fecha de inicio del filtro (obligatoria)
     * @param toDate   fecha de fin del filtro (obligatoria)
     * @return {@link Response} con {@link AdminStatSimpleDTO} conteniendo el histórico y el filtrado
     */
    Response<AdminStatSimpleDTO> getAppEarningsStats(LocalDate fromDate, LocalDate toDate);

    /**
     * Obtiene el monto total transaccionado en la plataforma en un rango de fechas.
     * Representa la suma de todos los pagos realizados por los pasajeros en reservas completadas.
     * @param fromDate fecha de inicio del filtro (obligatoria)
     * @param toDate   fecha de fin del filtro (obligatoria)
     * @return {@link Response} con {@link AdminStatSimpleDTO} conteniendo el histórico y el filtrado
     */
    Response<AdminStatSimpleDTO> getTotalTransactedStats(LocalDate fromDate, LocalDate toDate);

    /**
     * Obtiene la cantidad de viajes finalizados en un rango de fechas.
     * @param fromDate fecha de inicio del filtro (obligatoria)
     * @param toDate   fecha de fin del filtro (obligatoria)
     * @return {@link Response} con {@link AdminStatSimpleDTO} conteniendo el histórico y el filtrado
     */
    Response<AdminStatSimpleDTO> getFinishedTripsStats(LocalDate fromDate, LocalDate toDate);

    /**
     * Obtiene el total histórico de CO2 ahorrado en la plataforma.
     * El cálculo se basa en los kilómetros recorridos por los pasajeros
     * en reservas completadas, multiplicados por el factor de emisión de CO2.
     * @return {@link Response} con {@link AdminCo2StatDTO} conteniendo el total de CO2 ahorrado
     */
    Response<AdminCo2StatDTO> getCo2Stats();

    /**
     * Obtiene la cantidad de viajes publicados en el mes actual y el delta
     * respecto al mes anterior. El delta representa la diferencia entre los
     * viajes publicados en el mes actual y los del mes anterior.
     * @return {@link Response} con {@link AdminTripMonthlyStatDTO} conteniendo
     *         la cantidad del mes actual y el delta
     */
    Response<AdminTripMonthlyStatDTO> getMonthlyPublishedTripsStats();

}