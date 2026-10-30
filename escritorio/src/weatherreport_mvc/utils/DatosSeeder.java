package weatherreport_mvc.utils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import weatherreport_mvc.database.RegistroDAO;
import weatherreport_mvc.models.RegistroMeteorologico;

/**
 * Genera datos meteorológicos de prueba en la base de datos para que los
 * módulos de Dashboard, Histórico y Gráficas tengan información real con la
 * que trabajar. Es idempotente: solo inserta cuando la tabla está vacía.
 */
public final class DatosSeeder {

    private static final String[] OBS = {
        "Sin novedades.", "Cielo nublado.", "Llovizna ligera.", "Ráfagas moderadas.",
        "Visibilidad reducida.", "Condiciones estables.", "Frente frío en la zona.",
        "Viento del norte.", "Humedad elevada.", "Parcialmente despejado."
    };

    private DatosSeeder() {
    }

    public static int asegurarDatosPrueba() {
        Random rnd = new Random(2026L);
        int insertados = 0;
        try {
            if (RegistroDAO.contar() > 0) {
                return 0;
            }
            LocalDate hoy = LocalDate.now();

            // Registros de los últimos 14 días (3 por día) para la serie semanal.
            int horaActual = LocalTime.now().getHour();
            for (int diaAtras = 13; diaAtras >= 0; diaAtras--) {
                LocalDate dia = hoy.minusDays(diaAtras);
                int horaInicio = diaAtras == 0
                        ? Math.max(0, horaActual - 4) : 6;
                for (int k = 0; k < 3; k++) {
                    int hora = Math.min(22, horaInicio + k * 6);
                    if (diaAtras == 0 && hora > horaActual) {
                        continue;
                    }
                    RegistroMeteorologico r = crearRegistro(dia, LocalTime.of(hora, 0), rnd);
                    RegistroDAO.insertar(r);
                    insertados++;
                }
            }

            // Registros en meses anteriores del año para el acumulado mensual.
            for (int mes = 1; mes < hoy.getMonthValue(); mes++) {
                int cantidad = 5 + rnd.nextInt(5);
                for (int k = 0; k < cantidad; k++) {
                    int dia = 1 + rnd.nextInt(27);
                    int hora = 8 + rnd.nextInt(10);
                    LocalDate fecha;
                    try {
                        fecha = LocalDate.of(hoy.getYear(), mes, dia);
                    } catch (Exception e) {
                        continue;
                    }
                    RegistroMeteorologico r = crearRegistro(fecha, LocalTime.of(hora, 30), rnd);
                    RegistroDAO.insertar(r);
                    insertados++;
                }
            }

            RegistroMeteorologico ultimo = crearRegistro(
                    LocalDate.now(), LocalTime.now().withNano(0), rnd);
            RegistroDAO.insertar(ultimo);
            insertados++;
        } catch (SQLException e) {
            Bitacora.error("DatosSeeder.asegurarDatosPrueba", e);
        }
        return insertados;
    }

    private static RegistroMeteorologico crearRegistro(LocalDate fecha, LocalTime hora,
                                                       Random rnd) {
        RegistroMeteorologico r = new RegistroMeteorologico();
        r.setFecha(fecha);
        r.setHora(hora);
        r.setHumedad(Math.round((35 + rnd.nextDouble() * 63) * 100.0) / 100.0);
        r.setVelocidadViento(Math.round((5 + rnd.nextDouble() * 80) * 100.0) / 100.0);
        r.setLluvia(Math.round((rnd.nextDouble() * 38) * 100.0) / 100.0);
        r.setObservaciones(OBS[rnd.nextInt(OBS.length)]);
        return r;
    }
}
