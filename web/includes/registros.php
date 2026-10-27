<?php
// Campos de la tabla 'report' y utilidades compartidas por Historico, Ingreso, Graficas y Reportes
require_once __DIR__ . '/datos.php';

// columna => [etiqueta, tipo] agrupadas por seccion (mismo orden que IngresoManualPanel)
const SECCIONES = [
    ['sun', 'Exterior', [
        'temp_out' => ['Temperatura exterior (°C)', 'n'],
        'hi_temp' => ['Temperatura máxima (°C)', 'n'],
        'low_temp' => ['Temperatura mínima (°C)', 'n'],
        'dew_pt' => ['Punto de rocío (°C)', 'n'],
        'wind_chill' => ['Sensación térmica (°C)', 'n'],
        'heat_index' => ['Índice de calor (°C)', 'n'],
        'thw_index' => ['Índice THW (°C)', 'n'],
        'out_hum' => ['Humedad relativa (%)', 'n'],
    ]],
    ['wind', 'Viento', [
        'wind_speed' => ['Velocidad promedio (km/h)', 'n'],
        'wind_dir' => ['Dirección', 't'],
        'wind_run' => ['Recorrido del viento (km)', 'n'],
        'hi_speed' => ['Ráfaga máxima (km/h)', 'n'],
        'hi_dir' => ['Dirección de la ráfaga', 't'],
    ]],
    ['rain', 'Presión y lluvia', [
        'bar' => ['Presión barométrica (hPa)', 'n'],
        'rain' => ['Lluvia acumulada (mm)', 'n'],
        'rain_rate' => ['Tasa de lluvia (mm/h)', 'n'],
        'heat_dd' => ['Grados día calefacción', 'n'],
        'cool_dd' => ['Grados día refrigeración', 'n'],
    ]],
    ['home', 'Interior', [
        'in_temp' => ['Temperatura interior (°C)', 'n'],
        'in_hum' => ['Humedad interior (%)', 'n'],
        'in_dew' => ['Punto de rocío interior (°C)', 'n'],
        'in_heat' => ['Índice de calor interior (°C)', 'n'],
        'in_emc' => ['EMC interior (%)', 'n'],
        'in_air_density' => ['Densidad del aire (kg/m³)', 'n'],
    ]],
    ['gauge', 'Sensores', [
        'wind_samp' => ['Muestras de viento', 'i'],
        'iss_tx' => ['Estado ISS', 'i'],
        'arc_int' => ['Intervalo de archivo (min)', 'i'],
        'recept' => ['Calidad de recepción (%)', 'n'],
    ]],
];

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto',
    'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

function campos(): array
{
    $c = [];
    foreach (SECCIONES as [, , $lista]) {
        $c += $lista;
    }
    return $c;
}

// DateTime -> "31/08/26" y "10:00 p" (formato de la estacion, igual que DateUtil.java)
function fecha_estacion(DateTimeInterface $d): string
{
    return $d->format('d/m/y');
}

function hora_estacion(DateTimeInterface $d): string
{
    return $d->format('g:i') . ' ' . ($d->format('G') < 12 ? 'a' : 'p');
}

function fecha_larga(string $ymd): string
{
    return DateTime::createFromFormat('Y-m-d', $ymd)->format('d/m/Y');
}

// Ultima fecha con registros (Y-m-d) o null
function ultima_fecha(): ?string
{
    return db()->query('SELECT DATE(MAX(' . FH_SQL . ')) FROM report')->fetchColumn() ?: null;
}

function anios_con_datos(): array
{
    $anios = db()->query('SELECT DISTINCT YEAR(' . FH_SQL . ') a FROM report HAVING a IS NOT NULL ORDER BY a DESC')
        ->fetchAll(PDO::FETCH_COLUMN);
    $actual = (int)date('Y');
    $anios = array_unique(array_merge(array_map('intval', $anios), [$actual, $actual - 1]));
    rsort($anios);
    return $anios;
}

// Valida una fecha Y-m-d de un GET; si no sirve devuelve $defecto
function fecha_param(string $nombre, string $defecto): string
{
    $v = $_GET[$nombre] ?? '';
    $d = DateTime::createFromFormat('!Y-m-d', $v);
    return $d && $d->format('Y-m-d') === $v ? $v : $defecto;
}

// Mensajes de una sola vez entre redirecciones (POST -> GET)
function avisar(string $tipo, string $texto): void
{
    $_SESSION['avisos'][] = [$tipo, $texto];
}

function mostrar_avisos(): string
{
    $html = '';
    foreach ($_SESSION['avisos'] ?? [] as [$tipo, $texto]) {
        $ic = $tipo === 'g' ? 'check' : 'alert';
        $html .= '<div class="al ' . $tipo . '"><i data-i="' . $ic . '"></i><span>' . nl2br(e($texto)) . '</span></div>';
    }
    unset($_SESSION['avisos']);
    return $html;
}

function redirigir(string $url): void
{
    header('Location: ' . $url);
    exit;
}

// Para paginas que reciben POST: corta si el token CSRF no coincide
function exigir_csrf(string $volver): void
{
    if (!verificar_csrf()) {
        avisar('r', 'La sesión expiró. Intente de nuevo.');
        redirigir($volver);
    }
}

function campo_csrf(): string
{
    return '<input type="hidden" name="csrf" value="' . e(token_csrf()) . '">';
}
