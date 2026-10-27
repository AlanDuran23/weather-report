# Weather Report — App Android

App de clima para Android hecha con **Kotlin y Jetpack Compose**. Muestra el pronóstico de
[Open-Meteo](https://open-meteo.com/) y, si se configura, lo combina con las lecturas en tiempo real
de una **estación meteorológica física** (Davis Vantage) a través de su API.

Forma parte del sistema **Weather Report**, junto con la
[app de escritorio](../escritorio) y el [panel web](../web).

<!-- CAPTURAS: guárdalas en docs/capturas/ y quita este comentario para que se vean.
<p align="center">
  <img src="docs/capturas/inicio.png" width="250" alt="Pantalla principal">
  <img src="docs/capturas/calidad-aire.png" width="250" alt="Calidad del aire">
  <img src="docs/capturas/ajustes.png" width="250" alt="Ajustes">
</p>
-->

## Funciones

- **Clima actual:** temperatura, sensación térmica, humedad, presión, viento y máximas y mínimas.
- **Pronóstico** por hora (24 h) y por día (7 días), con probabilidad de lluvia.
- **Calidad del aire:** índice AQI (EPA de EE. UU.) y contaminantes (PM2.5, PM10, O₃, NO₂, SO₂, CO).
- **Índice UV**, salida y puesta del sol.
- **Datos de estación propia:** inicia sesión en el backend de la estación y usa sus lecturas
  (temperatura, humedad, viento, presión) en lugar de las del modelo. Si la estación no responde
  en 6 segundos, sigue funcionando solo con Open-Meteo.
- **Ajustes guardados** con DataStore: ubicación (latitud y longitud), servidor y credenciales de
  la estación.

## Lo técnico

| Área | Qué se usó |
|---|---|
| UI | Jetpack Compose + Material 3, tema oscuro con tarjetas tipo vidrio (*glassmorphism*) |
| Gráficas | Dibujadas a mano con `Canvas`: línea de temperatura, medidor de UV, anillo de humedad, manómetro de presión, brújula de viento y arco solar |
| Arquitectura | MVVM: `WeatherViewModel` → `WeatherRepository` → APIs |
| Red | Retrofit + OkHttp + kotlinx.serialization, con `CookieJar` para mantener la sesión de la estación |
| Concurrencia | Coroutines: las 3 fuentes (pronóstico, aire y estación) se piden en paralelo con `async` |
| Persistencia | DataStore Preferences |
| Navegación | Navigation Compose |

```
app/src/main/java/com/alanduran/weatherreport/
  data/
    model/        Modelos de la app (WeatherData, AirQuality...)
    remote/       APIs de Open-Meteo y de la estación + DTOs
    repository/   WeatherRepository: junta y combina las fuentes
    SettingsStore.kt
  ui/
    components/   Gráficas y medidores con Canvas
    screens/      Principal, Calidad del aire, Ajustes
    theme/        Colores, tipografía, efecto vidrio
  util/           Formatos, códigos de clima, categorías AQI/UV
```

## Cómo correrla

Requisitos: Android Studio (Narwhal o más nuevo), JDK 17 y un dispositivo o emulador con Android 7.0 (API 24) o superior.

1. Abre la carpeta `android` en Android Studio.
2. Espera a que Gradle sincronice.
3. Pulsa **Run**.

La app funciona sin configurar nada (usa Open-Meteo). Para conectar una estación, entra a
**Ajustes**, activa "Usar estación" y escribe la dirección del servidor y el usuario.

## Créditos

Datos del clima y calidad del aire: [Open-Meteo.com](https://open-meteo.com/) (licencia CC BY 4.0). La API gratuita de Open-Meteo es solo para uso no comercial.

## Próximos pasos

- Notificaciones cuando el viento o la humedad pasen un umbral.
- Widget para la pantalla de inicio.
- Historial de lecturas de la estación sin conexión (Room).
