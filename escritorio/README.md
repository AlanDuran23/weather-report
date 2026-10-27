# Weather Report — App de Escritorio

Aplicación de escritorio en **Java (Swing)** para registrar, analizar y reportar los datos de una
**estación meteorológica** (Davis Vantage). Hecha con arquitectura **MVC** y base de datos **MySQL**.

Es el núcleo del sistema Weather Report: el [panel web](../web)
y la [app Android](../android) trabajan con los mismos datos.

<!-- CAPTURAS: guárdalas en docs/capturas/ y quita este comentario para que se vean.
![Panel principal](docs/capturas/panel.png)
![Gráficas](docs/capturas/graficas.png)
-->

## Funciones

- **Panel principal** con la última lectura, promedios de 24 horas y alertas de humedad y viento
  por nivel (normal, atención, crítica).
- **Importación** de los archivos `.txt` que exporta la estación (30 columnas) y de archivos `.csv`
  sencillos, con validación línea por línea y sin duplicados.
- **Ingreso manual** de registros.
- **Histórico** con búsqueda, filtros y detalle de cada registro.
- **Gráficas** de temperatura, humedad, viento, presión y lluvia (JFreeChart).
- **Reportes PDF** diarios, semanales y mensuales con resumen estadístico y una **narrativa
  meteorológica** generada automáticamente (Apache PDFBox).
- **Exportación a Excel** (Apache POI).
- **Usuarios y roles** (*Ingeniero* y *Consulta*), registro de cuentas y recuperación de contraseña con un código de un solo uso que genera un Ingeniero.
- **Respaldo y restauración** de la base de datos (detecta `mysqldump` automáticamente).
- **Configuración** de la estación, ubicación, umbrales de alerta y carpetas de salida.

## Lo técnico

| Área | Qué se usó |
|---|---|
| Lenguaje / UI | Java, Swing con estilos propios (`UIFactory`, `EstiloUI`) |
| Arquitectura | MVC: `models/`, `views/`, `controllers/`, acceso a datos con DAO (`database/`) |
| Base de datos | MySQL con JDBC (MySQL Connector/J) |
| Gráficas | JFreeChart |
| PDF / Excel | Apache PDFBox / Apache POI |
| IDE | NetBeans (proyecto Ant) |

```
src/weatherreport_mvc/
  models/        RegistroMeteorologico, Usuario, ConfiguracionSistema
  views/         Paneles y diálogos de Swing
  controllers/   Lógica de cada pantalla
  database/      Conexión y DAOs (RegistroDAO, UsuarioDAO)
  utils/         Importador, PDF, Excel, gráficas, alertas, narrativa
script/
  database.sql   Crea la base de datos y el usuario inicial
```

## Cómo correrla

Requisitos: JDK 25, NetBeans y MySQL (por ejemplo, el de XAMPP).

1. Importa `script/database.sql` en MySQL.
2. Abre el proyecto en NetBeans.
3. Crea una carpeta `lib` dentro de `escritorio` y pon ahí los `.jar` de MySQL Connector/J,
   JFreeChart 1.5.6, PDFBox 3.0.8 y Apache POI 5.2.3 (con sus dependencias). Los nombres exactos
   están en `nbproject/project.properties`.
4. Ejecuta el proyecto (**F6**).

Usuario inicial: `admin` / `admin123` (cámbiala después de entrar).

Para probar con datos, importa desde **Ingreso Manual** el archivo
[`datos-ejemplo/estacion_ejemplo.txt`](../datos-ejemplo/estacion_ejemplo.txt).

## Créditos

El logo y los íconos de `src/resources/` fueron generados con IA (Gemini).
