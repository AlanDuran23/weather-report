# Weather Report — Panel Web

Sistema web para **administrar los datos de una estación meteorológica** (Davis Vantage): importa
las lecturas, muestra el estado actual con alertas, gráficas e histórico, y genera reportes
diarios, semanales y mensuales listos para imprimir.

Es la versión web de la [app de escritorio en Java](../escritorio): usa la
**misma base de datos MySQL y las mismas reglas**, así que las dos pueden usarse a la vez. Los datos
también se ven desde la [app Android](../android).

<!-- CAPTURAS: guárdalas en docs/capturas/ y quita este comentario para que se vean.
![Panel principal](docs/capturas/panel.png)
![Gráficas](docs/capturas/graficas.png)
![Reporte mensual](docs/capturas/reporte.png)
-->

## Funciones

- **Panel principal:** última lectura, promedios de las últimas 24 horas y **alertas por nivel**
  (normal, atención, crítica) cuando la humedad o el viento superan los umbrales configurados.
  Es la página de entrada y se puede ver sin iniciar sesión; el resto pide login.
- **Temas de color** (vino, negro, gris y blanco) desde el engranaje del menú; se guardan en el navegador.
- **Importación de archivos** `.txt`/`.csv` exportados por el software de la estación (30 columnas
  tabuladas) o en un formato sencillo. Valida cada línea, reporta errores por número de línea y
  omite duplicados, todo dentro de una transacción.
- **Ingreso manual** de registros con los datos principales (temperatura, humedad, viento, dirección y lluvia).
- **Histórico** con búsqueda por campo, paginación, exportación y versión para imprimir.
- **Gráficas** de los últimos 7 o 30 días (contados desde el último registro, nunca más de un año atrás)
  de temperatura, humedad, viento, lluvia, presión y punto de rocío, dibujadas en SVG desde PHP, sin librerías.
- **Reportes** diarios, semanales y mensuales con resumen estadístico (máximas, mínimas, ráfagas,
  lluvia acumulada, dirección dominante del viento) y una **narrativa meteorológica** escrita
  automáticamente a partir de los datos.
- **Usuarios y roles:** *Ingeniero* (acceso total) y *Consulta* (solo lectura). Registro de
  cuentas de consulta desde el login; las contraseñas olvidadas las restablece un Ingeniero.
- **Configuración compartida** con la app de escritorio: lee su archivo `.properties` (umbrales,
  nombre de la estación).

## Lo técnico

- **PHP 8** sin framework, con PDO y consultas preparadas.
- **MySQL / MariaDB.**
- **Seguridad:** tokens CSRF en todos los formularios, cookies de sesión `HttpOnly` y `SameSite`,
  salida escapada contra XSS, permisos por página según el rol, y la sesión se cierra si
  desactivan al usuario.
- HTML, CSS y JavaScript sin dependencias externas; íconos en SVG.

```
web/
  index.php            Panel principal
  ingreso.php          Importar archivo / ingreso manual
  historico.php        Histórico con filtros
  graficas.php         Gráficas
  reportes.php         Reportes + narrativa
  usuarios.php         Gestión de usuarios (solo Ingeniero)
  login.php            Login y registro de cuentas de consulta
  config.php           Conexión a MySQL y valores por defecto
  includes/            Lógica: auth, base de datos, importador, reportes, gráfica SVG
  css/  js/  img/
```

## Cómo correrlo

Requisitos: [XAMPP](https://www.apachefriends.org/) (o cualquier servidor con PHP 8 y MySQL).

1. Copia la carpeta `web` dentro de `C:\xampp\htdocs\` y renómbrala `weather`.
2. Inicia **Apache** y **MySQL** desde el panel de XAMPP.
3. Crea la base de datos importando
   [`database.sql`](../escritorio/script/database.sql) en phpMyAdmin.
4. Si tu MySQL tiene contraseña, ajústala en `config.php`.
5. Abre <http://localhost/weather/>.

Usuario inicial: `admin` / `admin123` (cámbiala después de entrar).

Para probar con datos, entra a **Ingreso** e importa [`datos-ejemplo/estacion_ejemplo.txt`](../datos-ejemplo/estacion_ejemplo.txt) (un mes de lecturas inventadas).

## Próximos pasos

- Alertas por correo o Telegram.
- API REST para la app Android.

## Créditos

Las imágenes ilustrativas de `img/` fueron generadas con IA (Gemini).
