<?php
// Importacion de archivos .txt/.csv (equivale a ImportadorArchivos.java)
//  - Formato de la estacion: cabecera "Date<TAB>Time" y 30 columnas tabuladas.
//  - Formato sencillo: fecha y hora, humedad, viento, lluvia[, observaciones] separados por ; , o tabulador.
require_once __DIR__ . '/registros.php';

// Orden de columnas del archivo de la estacion a partir de la columna 2
const COLUMNAS_ESTACION = ['temp_out', 'hi_temp', 'low_temp', 'out_hum', 'dew_pt', 'wind_speed', 'wind_dir',
    'wind_run', 'hi_speed', 'hi_dir', 'wind_chill', 'heat_index', 'thw_index', 'bar', 'rain', 'rain_rate',
    'heat_dd', 'cool_dd', 'in_temp', 'in_hum', 'in_dew', 'in_heat', 'in_emc', 'in_air_density', 'wind_samp',
    'iss_tx', 'recept', 'arc_int'];

// "6,5" / "86 %" / "---" -> float o null
function numero_texto(?string $t): ?float
{
    $t = strtolower(trim((string)$t));
    $t = trim(str_replace(['%', 'km/h', 'mm/h', 'mm'], '', $t));
    if (str_contains($t, ',') && !str_contains($t, '.')) {
        $t = str_replace(',', '.', $t);
    }
    return is_numeric($t) ? (float)$t : null;
}

function es_cabecera_estacion(string $linea): bool
{
    $p = explode("\t", $linea);
    return count($p) >= 2 && strcasecmp(trim($p[0]), 'Date') === 0 && strcasecmp(trim($p[1]), 'Time') === 0;
}

// Fecha "dd/MM/yy" + hora "6:00 a" de la estacion -> DateTime
function fecha_hora_estacion(string $fecha, string $hora): ?DateTime
{
    $h = strtolower(trim($hora));
    if (preg_match('/^(\d{1,2}):(\d{2})\s*([ap])m?$/', $h, $m)) {
        $h = $m[1] . ':' . $m[2] . ' ' . $m[3] . 'm';
        $d = DateTime::createFromFormat('!d/m/y g:i a', trim($fecha) . ' ' . $h);
    } else {
        $d = DateTime::createFromFormat('!d/m/y H:i', trim($fecha) . ' ' . $h);
    }
    return $d ?: null;
}

// Formatos aceptados por el escritorio para el formato sencillo
function fecha_hora_libre(string $t): ?DateTime
{
    foreach (['!d/m/Y H:i', '!d/m/Y H:i:s', '!Y-m-d H:i', '!Y-m-d H:i:s', '!d/m/Y', '!Y-m-d'] as $f) {
        $d = DateTime::createFromFormat($f, trim($t));
        if ($d && !DateTime::getLastErrors()) {
            return $d;
        }
    }
    return null;
}

// Devuelve [registros, errores]. Cada registro es [columna => valor] listo para insertar.
function leer_archivo(string $contenido): array
{
    $contenido = preg_replace('/^\xEF\xBB\xBF/', '', $contenido);
    if (!mb_check_encoding($contenido, 'UTF-8')) {
        $contenido = mb_convert_encoding($contenido, 'UTF-8', 'Windows-1252');
    }
    $lineas = preg_split('/\r\n|\r|\n/', $contenido);
    $registros = [];
    $errores = [];

    $cabecera = null;
    foreach ($lineas as $i => $l) {
        if (es_cabecera_estacion(trim($l))) {
            $cabecera = $i;
            break;
        }
    }

    if ($cabecera !== null) {
        for ($i = $cabecera + 1; $i < count($lineas); $i++) {
            $l = trim($lineas[$i]);
            if ($l === '' || $l[0] === '#') {
                continue;
            }
            $p = explode("\t", $l);
            $d = count($p) >= 2 ? fecha_hora_estacion($p[0], $p[1]) : null;
            if (!$d) {
                $errores[] = 'Línea ' . ($i + 1) . ': fecha u hora inválida.';
                continue;
            }
            $r = ['fecha' => fecha_estacion($d), 'hora' => hora_estacion($d)];
            foreach (COLUMNAS_ESTACION as $k => $col) {
                $v = $p[$k + 2] ?? '';
                $r[$col] = in_array($col, ['wind_dir', 'hi_dir'], true)
                    ? (trim($v) === '---' ? '' : trim($v))
                    : numero_texto($v);
            }
            $registros[] = $r;
        }
        return [$registros, $errores];
    }

    $sep = null;
    foreach ($lineas as $i => $l) {
        $l = trim($l);
        if ($l === '' || $l[0] === '#') {
            continue;
        }
        $sep ??= str_contains($l, ';') ? ';' : (str_contains($l, "\t") ? "\t" : ',');
        $p = explode($sep, $l);
        $n = 'Línea ' . ($i + 1) . ': ';
        if (count($p) < 4) {
            $errores[] = $n . 'se requieren al menos 4 campos (fecha y hora, humedad, viento, lluvia).';
            continue;
        }
        $d = fecha_hora_libre($p[0]);
        [$hum, $vie, $llu] = [numero_texto($p[1]), numero_texto($p[2]), numero_texto($p[3])];
        if (!$d) {
            $errores[] = $n . 'el campo fecha y hora es inválido. Use dd/MM/yyyy HH:mm.';
        } elseif ($hum === null || $hum < 0 || $hum > 100) {
            $errores[] = $n . 'la humedad debe estar entre 0 y 100.';
        } elseif ($vie === null || $vie < 0) {
            $errores[] = $n . 'la velocidad del viento debe ser mayor o igual a 0.';
        } elseif ($llu === null || $llu < 0) {
            $errores[] = $n . 'la lluvia debe ser mayor o igual a 0.';
        } else {
            $registros[] = ['fecha' => fecha_estacion($d), 'hora' => hora_estacion($d), 'out_hum' => $hum,
                'wind_speed' => $vie, 'rain' => $llu, 'observaciones' => trim(implode(',', array_slice($p, 4)))];
        }
    }
    return [$registros, $errores];
}

// Inserta en una transaccion, omitiendo fechas/horas que ya existen. Devuelve [insertados, duplicados]
function insertar_registros(array $registros): array
{
    $pdo = db();
    $existentes = array_flip($pdo->query("SELECT CONCAT(fecha, '|', hora) FROM report")->fetchAll(PDO::FETCH_COLUMN));
    $insertados = 0;
    $duplicados = 0;
    $sentencias = []; // un INSERT preparado por cada combinacion de columnas
    $pdo->beginTransaction();
    try {
        foreach ($registros as $r) {
            $clave = $r['fecha'] . '|' . $r['hora'];
            if (isset($existentes[$clave])) {
                $duplicados++;
                continue;
            }
            $existentes[$clave] = true;
            $cols = array_keys($r);
            $sentencias[implode(',', $cols)] ??= $pdo->prepare('INSERT INTO report (' . implode(', ', $cols)
                . ') VALUES (' . implode(', ', array_fill(0, count($cols), '?')) . ')');
            $sentencias[implode(',', $cols)]->execute(array_values($r));
            $insertados++;
        }
        $pdo->commit();
    } catch (Throwable $ex) {
        $pdo->rollBack();
        throw $ex;
    }
    return [$insertados, $duplicados];
}
