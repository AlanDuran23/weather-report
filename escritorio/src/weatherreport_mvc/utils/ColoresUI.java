package weatherreport_mvc.utils;

import java.awt.Color;

/**
 * Paleta de colores de la aplicación: Vino, Negro, Blanco y variaciones de
 * gris, ampliada con el tema oscuro premium (mate + vino neón/rubí).
 * Las constantes originales se conservan intactas para no alterar el LoginView.
 */
public final class ColoresUI {

    // ---- Paleta base original (usada por LoginView y como base vino) ----
    public static final Color VINO = new Color(0x80, 0x00, 0x20);
    public static final Color VINO_OSCURO = new Color(0x5C, 0x00, 0x16);
    public static final Color VINO_CLARO = new Color(0x9B, 0x2C, 0x2C);
    public static final Color NEGRO = new Color(0x00, 0x00, 0x00);
    public static final Color NEGRO_SUAVE = new Color(0x14, 0x14, 0x14);
    public static final Color BLANCO = new Color(0xFF, 0xFF, 0xFF);
    public static final Color GRIS_MUY_CLARO = new Color(0xF5, 0xF5, 0xF5);
    public static final Color GRID_BG = new Color(0xFA, 0xFA, 0xFA);
    public static final Color GRIS_CLARO = new Color(0xE0, 0xE0, 0xE0);
    public static final Color GRIS_MEDIO = new Color(0x9E, 0x9E, 0x9E);
    public static final Color GRIS_OSCURO = new Color(0x61, 0x61, 0x61);
    public static final Color GRIS_TEXTO = new Color(0x4A, 0x4A, 0x4A);

    // ---- Tema oscuro: superficie mate casi negro ----
    /** Fondo base de la aplicación (gris mate muy oscuro). */
    public static final Color FONDO_RAIZ = new Color(0x0F, 0x0F, 0x13);
    /** Barra lateral de navegación (un tono más oscuro que la base). */
    public static final Color LATERAL = new Color(0x0B, 0x0B, 0x0F);
    /** Superficie estándar (un nivel más claro que la base). */
    public static final Color SUPERFICIE = new Color(0x1A, 0x1A, 0x20);
    /** Superficie elevada para tarjetas y contenedores. */
    public static final Color SUPERFICIE_ELEVADA = new Color(0x22, 0x22, 0x2B);
    /** Líneas de separación y bordes sutiles. */
    public static final Color BORDE = new Color(0x2E, 0x2E, 0x38);
    /** Borde más claro para trazos finos brillantes. */
    public static final Color BORDE_CLARO = new Color(0x3A, 0x3A, 0x46);

    // ---- Texto sobre tema oscuro ----
    public static final Color TEXTO_PRIMARIO = new Color(0xF2, 0xF2, 0xF5);
    public static final Color TEXTO_SECUNDARIO = new Color(0xB9, 0xB9, 0xC2);
    public static final Color TEXTO_SUAVE = new Color(0x82, 0x82, 0x8C);
    /** Vino claro luminoso (títulos y encabezados de tabla). */
    public static final Color TEXTO_VINO = new Color(0xE8, 0x55, 0x77);

    // ---- Acentos vino neón / rubí (fuentes de luz) ----
    public static final Color VINO_NEON = new Color(0xE2, 0x00, 0x3F);
    public static final Color RUBI = new Color(0xFF, 0x3B, 0x63);
    public static final Color RUBI_OSCURO = new Color(0xC1, 0x1A, 0x3C);
    public static final Color DESTELLO = new Color(0xFF, 0x7A, 0x9E);
    /** Vino translúcido para selección de tabla (vidrio ahumado). */
    public static final Color VINO_SELECCION = new Color(0x9E, 0x00, 0x27);

    // ---- Sala de guardia: el vino marca la acción y la ubicación; el rojo queda para alertas ----
    /** Relleno de la acción principal (vino institucional, sin neón). */
    public static final Color ACCION = new Color(0x9E, 0x1B, 0x36);
    public static final Color ACCION_HOVER = new Color(0xB3, 0x21, 0x3F);
    public static final Color ACCION_PRESIONADA = new Color(0x7E, 0x13, 0x2A);
    /** Anillo de foco y riel del módulo activo. */
    public static final Color ENFOQUE = new Color(0xD0, 0x30, 0x55);
    /** Hover de botones secundarios y filas de navegación. */
    public static final Color HOVER = new Color(0x2A, 0x2A, 0x34);
    /** Fondo de campos de entrada: hundido, más oscuro que la tarjeta. */
    public static final Color CAMPO = new Color(0x13, 0x13, 0x18);
    public static final Color BORDE_CAMPO = new Color(0x36, 0x36, 0x42);
    /** Fila seleccionada en tablas: vino apagado, no compite con las alertas. */
    public static final Color SELECCION = new Color(0x45, 0x18, 0x25);
    /** Fila bajo el cursor en tablas. */
    public static final Color FILA_HOVER = new Color(0x21, 0x21, 0x28);
    /** Líneas entre filas de tabla, más suaves que BORDE. */
    public static final Color LINEA_TABLA = new Color(0x25, 0x25, 0x2E);

    // ---- Series de gráficas: un color fijo por variable, ninguno de estado ----
    // Validado para daltonismo sobre SUPERFICIE (humedad/viento/lluvia van juntos en reportes).
    public static final Color SERIE_HUMEDAD = new Color(0x39, 0x87, 0xE5);
    public static final Color SERIE_VIENTO = new Color(0xD9, 0x59, 0x26);
    public static final Color SERIE_LLUVIA = new Color(0x19, 0x9E, 0x70);
    public static final Color SERIE_TEMPERATURA = new Color(0xC9, 0x85, 0x00);
    public static final Color SERIE_PRESION = new Color(0xD5, 0x51, 0x81);
    public static final Color SERIE_ROCIO = new Color(0x90, 0x85, 0xE9);

    // ---- Alertas (originales y variantes luminosas para fondo oscuro) ----
    public static final Color ALERTA_NORMAL = new Color(0x1E, 0x88, 0x3C);
    public static final Color ALERTA_ATENCION = new Color(0xF9, 0xA8, 0x25);
    public static final Color ALERTA_CRITICA = new Color(0xE5, 0x39, 0x35);
    public static final Color ALERTA_NORMAL_OSC = new Color(0x43, 0xE0, 0x8B);
    public static final Color ALERTA_ATENCION_OSC = new Color(0xFF, 0xB3, 0x3D);
    public static final Color ALERTA_CRITICA_OSC = new Color(0xFF, 0x52, 0x52);

    private ColoresUI() {
    }
}