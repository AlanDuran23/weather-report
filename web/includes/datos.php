<?php
// Consultas y reglas del Panel Principal (equivalen a DashboardController y AlertaUtil del escritorio)
require_once __DIR__ . '/db.php';

// fecha "31/08/26" + hora "10:00 p" -> DATETIME, para ordenar y filtrar en MySQL
const FH_SQL = "STR_TO_DATE(CONCAT(fecha, ' ', hora, 'm'), '%d/%m/%y %l:%i %p')";

// Claves que comparte la web con ConfiguracionSistema.java
const CLAVES_TEXTO = ['nombreEstacion', 'ubicacion', 'rutaReportes'];
const CLAVES_NUMERO = ['umbralHumedad', 'umbralViento'];

// Lee un archivo .properties de Java -> [clave => valor]
function leer_properties(string $archivo): array
{
    $props = [];
    if (!is_readable($archivo)) {
        return $props;
    }
    foreach (file($archivo, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) as $linea) {
        $linea = ltrim($linea);
        if ($linea === '' || $linea[0] === '#' || $linea[0] === '!'
            || !preg_match('/^([^=:\s]+)\s*[=:]\s*(.*)$/', $linea, $m)) {
            continue;
        }
        // Java escribe los acentos como ó y escapa \ : = #
        $props[$m[1]] = preg_replace_callback('/\\\\(u[0-9a-fA-F]{4}|.)/', fn($x) => strlen($x[1]) === 5
            ? mb_chr(hexdec(substr($x[1], 1)), 'UTF-8')
            : (['t' => "\t", 'n' => "\n"][$x[1]] ?? $x[1]), $m[2]);
    }
    return $props;
}

// Configuracion: config.php + archivo .properties del escritorio (si existe)
function config_sistema(bool $recargar = false): array
{
    static $cfg = null;
    if ($cfg !== null && !$recargar) {
        return $cfg;
    }
    $cfg = require __DIR__ . '/../config.php';
    foreach (leer_properties($cfg['escritorio_config']) as $k => $v) {
        if (in_array($k, CLAVES_TEXTO, true)) {
            $cfg[$k] = $v;
        } elseif (in_array($k, CLAVES_NUMERO, true) && is_numeric($v)) {
            $cfg[$k] = (float)$v;
        }
    }
    return $cfg;
}

function panel_datos(): array
{
    $pdo = db();
    $recientes = $pdo->query("SELECT fecha, hora, temp_out, out_hum, wind_speed, bar, rain,
                                     wind_dir, hi_speed, Observaciones
                              FROM report
                              WHERE " . FH_SQL . " IS NOT NULL
                              ORDER BY " . FH_SQL . " DESC
                              LIMIT 6")->fetchAll();
    $prom = $pdo->query("SELECT COUNT(*) n, AVG(out_hum) hum, AVG(wind_speed) viento, AVG(temp_out) temp
                         FROM report
                         WHERE " . FH_SQL . " >= NOW() - INTERVAL 24 HOUR")->fetch();
    return [
        'ultimo' => $recientes[0] ?? null,
        'recientes' => $recientes,
        'promedios' => $prom['n'] > 0 ? $prom : null,
        'total' => (int)$pdo->query('SELECT COUNT(*) FROM report')->fetchColumn(),
    ];
}

function num($v, int $dec = 1): string
{
    return $v === null ? '--' : number_format((float)$v, $dec, '.', '');
}

// Niveles: 'g' normal, 'a' atencion, 'r' alerta critica
function alerta_humedad(float $h, float $umbral): array
{
    if ($h > $umbral + 5) {
        return ['r', 'ALERTA CRÍTICA: humedad muy alta (' . num($h) . '%). Riesgo de saturación.'];
    }
    if ($h > $umbral) {
        return ['a', 'Atención: humedad elevada (' . num($h) . '%) superando el umbral (' . num($umbral, 0) . '%).'];
    }
    return ['g', 'Humedad dentro de rangos normales (' . num($h) . '%).'];
}

function alerta_viento(float $v, float $umbral): array
{
    if ($v > $umbral + 15) {
        return ['r', 'ALERTA CRÍTICA: vientos peligrosos (' . num($v) . ' km/h). Tome precauciones.'];
    }
    if ($v > $umbral) {
        return ['a', 'Atención: viento fuerte (' . num($v) . ' km/h) superando el umbral (' . num($umbral, 0) . ' km/h).'];
    }
    return ['g', 'Viento estable (' . num($v) . ' km/h).'];
}
