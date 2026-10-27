<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/importador.php';
require_once __DIR__ . '/includes/grafica.php';
$pagina = 'index';
$titulo = 'Panel Principal';
$icono = 'dash';
$usuario = requerir_pagina($pagina);

$cfg = config_sistema();
$error = null;
try {
    $d = panel_datos();
} catch (PDOException $ex) {
    $error = 'No se pudo conectar a la base de datos. Verifique que el servicio de MySQL esté activo.';
    $d = ['ultimo' => null, 'recientes' => [], 'promedios' => null, 'total' => 0];
}
$u = $d['ultimo'];
$p = $d['promedios'];

// Lista de los ultimos 6 registros de una variable (como mostrarRecientes del escritorio).
// Con sesion es un enlace al Historico; el invitado no puede entrar ahi.
function lista_reciente(array $recientes, string $campo, string $unidad): string
{
    $enlace = puede_ver('historico');
    $html = $enlace ? '<a class="ls rec" href="historico.php" title="Ver histórico">' : '<div class="ls rec">';
    for ($i = 0; $i < 6; $i++) {
        $r = $recientes[$i] ?? null;
        $html .= $r
            ? '<div><span>' . e($r['fecha'] . ' ' . $r['hora']) . '</span><span>' . num($r[$campo]) . ' ' . $unidad . '</span></div>'
            : '<div class="sub"><span>Sin datos</span></div>';
    }
    return $html . ($enlace ? '</a>' : '</div>');
}

function promedio_24h(?array $p, string $campo, string $unidad): string
{
    return 'Promedio 24 h: ' . ($p ? num($p[$campo]) . ' ' . $unidad : '--');
}

function caja_alerta(array $alerta): string
{
    [$nivel, $texto] = $alerta;
    $ic = ['g' => 'check', 'a' => 'alert', 'r' => 'alert'][$nivel];
    return '<div class="al ' . $nivel . '"><i data-i="' . $ic . '"></i><span>' . e($texto) . '</span></div>';
}

// "hace 12 min"; tambien dice si el dato ya es viejo (mas de una hora) y los minutos transcurridos
function antiguedad(DateTime $fh): array
{
    $min = (int)floor((time() - $fh->getTimestamp()) / 60);
    $texto = match (true) {
        $min < 1 => 'hace un momento',
        $min < 60 => "hace $min min",
        $min < 2880 => 'hace ' . intdiv($min, 60) . ' h',
        default => 'hace ' . intdiv($min, 1440) . ' días',
    };
    return [$texto, $min > 60, $min];
}

// Tarjeta de una variable: cifra grande, mini grafica (alineada entre tarjetas), alerta y ultimos registros
function tarjeta(array $d, string $campo, string $titulo, string $ic, string $color, string $unidad, array $extra): string
{
    $u = $d['ultimo'];
    $serie = array_reverse(array_column($d['recientes'], $campo));
    return '<article class="card var" style="--c:var(' . $color . ')">'
        . '<div class="kh"><div class="ic"><i data-i="' . $ic . '"></i></div><h3 class="k">' . $titulo . '</h3></div>'
        . '<div class="big">' . ($u ? num($u[$campo]) . '<small>' . $unidad . '</small>' : '--') . '</div>'
        . '<div class="sub">' . $extra['prom'] . '</div>'
        . svg_mini($serie)
        . ($extra['caja'] ?? '')
        . lista_reciente($d['recientes'], $campo, $unidad)
        . '</article>';
}

$fh = $u ? fecha_hora_estacion((string)$u['fecha'], (string)$u['hora']) : null;
[$edad, $viejo, $minutos] = $fh ? antiguedad($fh) : ['Sin registros', false, 0];

// Estado general: el nivel mas alto entre humedad y viento
$alertas = $u ? [alerta_humedad((float)$u['out_hum'], $cfg['umbralHumedad']), alerta_viento((float)$u['wind_speed'], $cfg['umbralViento'])] : [];
$rango = ['g' => 0, 'a' => 1, 'r' => 2];
$nivel = 'g';
foreach ($alertas as [$n]) {
    if ($rango[$n] > $rango[$nivel]) {
        $nivel = $n;
    }
}
if ($error) {
    $estado = ['r', 'Sin conexión con la base de datos', $error];
} elseif (!$u) {
    $estado = ['a', 'Esperando datos del sensor', 'Todavía no hay registros en la base de datos.'];
} elseif ($nivel === 'g') {
    $estado = ['g', 'Condiciones normales', 'Humedad y viento por debajo de los umbrales ('
        . num($cfg['umbralHumedad'], 0) . ' % · ' . num($cfg['umbralViento'], 0) . ' km/h).'];
} else {
    // sin el prefijo "ALERTA CRÍTICA:" / "Atención:", que ya es el titulo de la franja
    $avisos = array_map(fn($a) => preg_replace('/^[^:]+:\s*/u', '', $a[1]), array_filter($alertas, fn($a) => $a[0] !== 'g'));
    $estado = [$nivel, $nivel === 'r' ? 'Alerta crítica' : 'Atención', ucfirst(implode(' ', $avisos))];
    // Una lectura de hace mas de un dia no es una alerta vigente: se informa sin el rojo
    if ($minutos > 1440) {
        $estado = ['a', 'Sin datos recientes', 'La última lectura marcaba ' . mb_strtolower($estado[1]) . ': ' . lcfirst($estado[2])];
    }
}

// ?parcial=1 devuelve solo <main> para el refresco automatico (js/app.js)
if (!isset($_GET['parcial'])) {
    require __DIR__ . '/includes/header.php';
}
?>
    <main class="sec" id="panel">
        <div class="bar" style="align-items:center">
            <button class="btn" id="refrescar"><i data-i="refresh"></i>Actualizar vista</button>
            <?php if (es_ingeniero()): ?><a class="btn" href="ingreso.php"><i data-i="upload"></i>Ingresar datos</a><?php endif; ?>
            <span class="sub" style="font-size:13px"><i data-i="pin"></i> <?= e($cfg['nombreEstacion'] . ' - ' . $cfg['ubicacion']) ?></span>
        </div>
        <div id="vivo">
            <section class="estado <?= $estado[0] ?>" aria-live="polite">
                <span class="luz" aria-hidden="true"></span>
                <div>
                    <h2 class="nv"><?= e($estado[1]) ?></h2>
                    <p class="det"><?= e($estado[2]) ?></p>
                </div>
                <div class="meta">
                    <div class="edad"><?php if ($viejo): ?><span class="chip"><i data-i="clock"></i>Dato desactualizado</span><?php endif; ?><span><?= e($edad) ?></span></div>
                    <div>Último registro <?= $u ? e($u['fecha'] . ' ' . $u['hora']) : '--' ?> · <?= number_format($d['total']) ?> registros · se actualiza cada 5 s</div>
                </div>
            </section>
            <div class="g3">
                <?= tarjeta($d, 'temp_out', 'Temperatura exterior', 'thermo', '--c-tem', '°C', ['prom' => promedio_24h($p, 'temp', '°C')]) ?>
                <?= tarjeta($d, 'out_hum', 'Humedad relativa', 'drop', '--c-hum', '%', ['prom' => promedio_24h($p, 'hum', '%'), 'caja' => $u ? caja_alerta($alertas[0]) : '']) ?>
                <?= tarjeta($d, 'wind_speed', 'Velocidad del viento', 'wind', '--c-vie', 'km/h', ['prom' => promedio_24h($p, 'viento', 'km/h'), 'caja' => $u ? caja_alerta($alertas[1]) : '']) ?>
            </div>
            <section class="card">
                <h4><i data-i="gauge"></i>Otras lecturas del último registro</h4>
                <div class="lect">
                    <div><span class="k">Dirección del viento</span><b><?= e($u['wind_dir'] ?? '' ?: '--') ?></b></div>
                    <div><span class="k">Ráfaga máxima</span><b><?= $u ? num($u['hi_speed']) : '--' ?><small>km/h</small></b></div>
                    <div><span class="k">Presión</span><b><?= $u ? num($u['bar']) : '--' ?><small>hPa</small></b></div>
                    <div><span class="k">Lluvia</span><b><?= $u ? num($u['rain']) : '--' ?><small>mm</small></b></div>
                </div>
            </section>
        </div>
    </main>
<?php
if (!isset($_GET['parcial'])) {
    require __DIR__ . '/includes/footer.php';
}
