<?php
// Datos y narrativa de los reportes (equivale a NarrativaMeteorologica y ReporteController)
require_once __DIR__ . '/registros.php';

const TIPOS_REPORTE = ['diario' => 'Diario', 'semanal' => 'Semanal', 'mensual' => 'Mensual'];

// Lee el periodo de $_GET. Por defecto usa la ultima fecha con datos.
function parametros_reporte(): array
{
    $tipo = array_key_exists($_GET['tipo'] ?? '', TIPOS_REPORTE) ? $_GET['tipo'] : 'mensual';
    $ultima = ultima_fecha() ?? date('Y-m-d');
    $fecha = fecha_param('fecha', $ultima);
    $anio = (int)($_GET['anio'] ?? substr($ultima, 0, 4));
    $mes = (int)($_GET['mes'] ?? substr($ultima, 5, 2));
    $mes = $mes >= 1 && $mes <= 12 ? $mes : 1;
    if ($tipo === 'diario') {
        [$ini, $fin] = [$fecha, $fecha];
        $nombre = 'Reporte diario';
        $periodo = fecha_larga($fecha);
    } elseif ($tipo === 'semanal') {
        [$ini, $fin] = [date('Y-m-d', strtotime($fecha . ' -6 days')), $fecha];
        $nombre = 'Resumen semanal';
        $periodo = fecha_larga($ini) . ' al ' . fecha_larga($fin);
    } else {
        $ini = sprintf('%04d-%02d-01', $anio, $mes);
        $fin = date('Y-m-t', strtotime($ini));
        $nombre = 'Resumen mensual';
        $periodo = MESES[$mes - 1] . ' ' . $anio;
    }
    return compact('tipo', 'fecha', 'anio', 'mes', 'ini', 'fin', 'nombre', 'periodo');
}

const REPORT_FH = '(SELECT *, ' . FH_SQL . ' AS fh FROM report) t WHERE fh >= ? AND fh < ? + INTERVAL 1 DAY';

function resumen_periodo(string $ini, string $fin): array
{
    $st = db()->prepare('SELECT COUNT(*) n, MAX(hi_temp) max_temp, MIN(low_temp) min_temp, AVG(temp_out) temp,
            MAX(heat_index) max_calor, AVG(dew_pt) rocio, AVG(wind_chill) sensacion, AVG(wind_speed) viento,
            MAX(hi_speed) rafaga, SUM(wind_run) recorrido, AVG(bar) presion, SUM(rain) lluvia,
            MAX(rain_rate) tasa, AVG(out_hum) humedad, AVG(in_hum) humedad_int
        FROM ' . REPORT_FH);
    $st->execute([$ini, $fin]);
    $r = $st->fetch();
    $st = db()->prepare("SELECT wind_dir FROM " . REPORT_FH . " AND wind_dir <> ''
        GROUP BY wind_dir ORDER BY COUNT(*) DESC, MIN(fh) LIMIT 1"); // empate: la que aparecio primero, como el escritorio
    $st->execute([$ini, $fin]);
    $r['direccion'] = $st->fetchColumn() ?: '--';
    return $r;
}

function registros_periodo(string $ini, string $fin): array
{
    $st = db()->prepare('SELECT id, fecha, hora, temp_out, out_hum, wind_speed, wind_dir, rain, bar, Observaciones
        FROM ' . REPORT_FH . ' ORDER BY fh');
    $st->execute([$ini, $fin]);
    return $st->fetchAll();
}

// Mismo texto que NarrativaMeteorologica.construir(); devuelve los parrafos
function narrativa(array $p, array $r): array
{
    if ((int)$r['n'] === 0) {
        return ['No hay registros meteorológicos para el periodo seleccionado.'];
    }
    $frases = [
        'diario' => ['Durante este día', 'recorrido acumulado de', 'La presión barométrica durante el día',
            'acumulado total de lluvia de', 'promedio de'],
        'semanal' => ['Durante esta semana', 'recorrido acumulado semanal de',
            'La presión barométrica en el transcurso de la semana', 'acumulado semanal total de lluvia de', 'promedio semanal de'],
        'mensual' => ['Durante este mes', 'recorrido acumulado mensual de',
            'La presión barométrica en el transcurso del mes', 'acumulado mensual total de lluvia de', 'promedio mensual de'],
    ][$p['tipo']];
    [$durante, $recorrido, $presion, $lluvia, $humedad] = $frases;
    $apertura = [
        'diario' => 'el día ' . fecha_larga($p['ini']),
        'semanal' => 'la semana del ' . fecha_larga($p['ini']) . ' al ' . fecha_larga($p['fin']),
        'mensual' => 'el mes de ' . mb_strtolower(MESES[$p['mes'] - 1]) . ' del año ' . $p['anio'],
    ][$p['tipo']];
    return [
        "A continuación, el resumen meteorológico correspondiente a $apertura.",
        "$durante, las temperaturas registradas oscilaron entre máximas absolutas de " . num($r['max_temp'])
        . ' grados y mínimas de ' . num($r['min_temp']) . ' grados, con un índice de calor que alcanzó un pico de '
        . num($r['max_calor']) . ' grados, un punto de rocío de ' . num($r['rocio'])
        . ' grados y una sensación térmica por viento de ' . num($r['sensacion']) . ' grados.',
        'En cuanto al comportamiento del viento, se registraron velocidades promedio de ' . num($r['viento'])
        . ' kilómetros por hora con dirección predominante del ' . $r['direccion'] . ', alcanzando ráfagas máximas de '
        . num($r['rafaga']) . " kilómetros por hora y un $recorrido " . num($r['recorrido']) . ' kilómetros.',
        "$presion se mantuvo en un promedio de " . num($r['presion'])
        . " hectopascales. En el apartado de precipitaciones, se tuvo un $lluvia " . num($r['lluvia'])
        . ' milímetros, registrando una tasa máxima de intensidad de ' . num($r['tasa'])
        . ' milímetros por hora durante las jornadas de mayor actividad.',
        "Por último, la humedad relativa en el exterior se situó en un $humedad " . num($r['humedad'])
        . ' por ciento, mientras que la humedad en ambientes interiores se mantuvo en un ' . num($r['humedad_int'])
        . ' por ciento.',
    ];
}

// Resumen de totales: [etiqueta, valor]
function totales(array $r): array
{
    return [
        ['Registros', number_format((int)$r['n'])],
        ['Temperatura promedio', num($r['temp']) . ' °C'],
        ['Temperatura máxima / mínima', num($r['max_temp']) . ' / ' . num($r['min_temp']) . ' °C'],
        ['Humedad promedio', num($r['humedad']) . ' %'],
        ['Viento promedio', num($r['viento']) . ' km/h · ' . $r['direccion']],
        ['Ráfaga máxima', num($r['rafaga']) . ' km/h'],
        ['Lluvia acumulada', num($r['lluvia']) . ' mm'],
        ['Tasa máxima de lluvia', num($r['tasa']) . ' mm/h'],
        ['Presión promedio', num($r['presion']) . ' hPa'],
    ];
}
