# Weather Report

Sistema de monitoreo meteorológico formado por **tres aplicaciones conectadas** que trabajan con
los datos de una estación meteorológica Davis Vantage: registran las lecturas, generan alertas
automáticas de viento y humedad, y producen gráficas y reportes.

| Carpeta | Aplicación | Tecnologías |
|---|---|---|
| [`escritorio/`](escritorio) | App de escritorio: núcleo del sistema, reportes PDF y Excel | Java, Swing, MySQL, JFreeChart, PDFBox, Apache POI |
| [`web/`](web) | Panel web con usuarios, histórico, gráficas y reportes | PHP 8, MySQL, PDO |
| [`android/`](android) | App móvil con pronóstico, calidad del aire y datos de la estación | Kotlin, Jetpack Compose, Retrofit |
| [`datos-ejemplo/`](datos-ejemplo) | Un mes de lecturas inventadas en el formato de la estación | — |

La app de escritorio y el panel web comparten la misma base de datos MySQL, así que pueden usarse
al mismo tiempo.

## Sobre el proyecto

Proyecto académico desarrollado para una materia universitaria. Se implementó de forma gratuita en
una institución de protección civil para monitorear su estación meteorológica.

Los datos incluidos en este repositorio son **de ejemplo, generados artificialmente**; no contiene
datos reales de ninguna estación ni institución.

## Cómo probarlo

Cada carpeta tiene su propio README con instrucciones. El camino más rápido es el panel web:

1. Instala [XAMPP](https://www.apachefriends.org/) e importa
   [`escritorio/script/database.sql`](escritorio/script/database.sql) en phpMyAdmin.
2. Copia `web/` en `C:\xampp\htdocs\weather` y abre <http://localhost/weather/>.
3. Entra con `admin` / `admin123` e importa `datos-ejemplo/estacion_ejemplo.txt`.

## Autor

**Alan Duran** · [GitHub](https://github.com/AlanDuran23)

## Licencia

[MIT](LICENSE)
