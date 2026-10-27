<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/registros.php';
require_once __DIR__ . '/includes/grafica.php';
$pagina = 'graficas';
$titulo = 'Gráficas';
$icono = 'chart';
$usuario = requerir_pagina($pagina);

// variable => [nombre, columna, unidad, icono, color, decimales] (como VariableMeteorologica)
$VARIABLES = [
    'humedad' => ['Humedad', 'out_hum', '%', 'drop', 'var(--c-hum)', 0],
    'viento' => ['Viento', 'wind_speed', 'km/h', 'wind', 'var(--c-vie)', 1],
    'lluvia' => ['Lluvia', 'rain', 'mm', 'rain', 'var(--c-llu)', 1],
    'temperatura' => ['Temperatura', 'temp_out', '°C', 'thermo', 'var(--c-tem)', 1],
    'presion' => ['Presión', 'bar', 'hPa', 'gauge', 'var(--c-pre)', 1],
    'rocio' => ['Punto de rocío', 'dew_pt', '°C', 'drops', 'var(--c-roc)', 1],
];
// periodo => [nombre, dias hacia atras contando el ultimo registro]
$PERIODOS = ['semanal' => ['Semanal', 7], 'mensual' => ['Mensual', 30]];

$periodo = array_key_exists($_GET['periodo'] ?? '', $PERIODOS) ? $_GET['periodo'] : 'semanal';
$var = array_key_exists($_GET['variable'] ?? '', $VARIABLES) ? $_GET['variable'] : 'humedad';
[$nombre, $col, $unidad, $ic, $color, $dec] = $VARIABLES[$var];

// Los ultimos 7 o 30 dias terminando en el ultimo registro, nunca mas atras de un año desde hoy
$limite = date('Y-m-d', strtotime('-1 year'));
$fin = ultima_fecha() ?? date('Y-m-d');
$ini = max(date('Y-m-d', strtotime($fin . ' -' . ($PERIODOS[$periodo][1] - 1) . ' days')), $limite);
$tituloGrafica = 'Últimos ' . $PERIODOS[$periodo][1] . ' días - ' . fecha_larga($ini) . ' a ' . fecha_larga($fin);

$puntos = [];
if ($fin >= $limite) {
    // Promedio por dia (RegistroDAO.agruparDia)
    $t = '(SELECT ' . $col . ' AS v, ' . FH_SQL . ' AS fh FROM report) t';
    $st = db()->prepare("SELECT DATE(fh) d, AVG(v) v FROM $t WHERE fh >= ? AND fh < ? + INTERVAL 1 DAY AND v IS NOT NULL GROUP BY d ORDER BY d");
    $st->execute([$ini, $fin]);
    foreach ($st as $r) {
        $puntos[substr($r['d'], 8, 2) . '/' . substr($r['d'], 5, 2)] = (float)$r['v'];
    }
} else {
    $tituloGrafica = 'Sin registros del último año';
}

function url_graf(array $cambios): string
{
    $base = array_intersect_key($_GET, array_flip(['periodo', 'variable']));
    return 'graficas.php?' . http_build_query(array_merge($base, $cambios));
}

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <div class="bar"><div class="seg" role="group" aria-label="Periodo"><?php foreach ($PERIODOS as $k => [$v]): ?><a class="pill<?= $k === $periodo ? ' on' : '' ?>" href="<?= e(url_graf(['periodo' => $k])) ?>"><i data-i="calendar"></i><?= $v ?></a><?php endforeach; ?></div></div>
        <div class="bar" style="margin-bottom:16px"><?php foreach ($VARIABLES as $k => $v): ?><a class="pill<?= $k === $var ? ' on' : '' ?>" href="<?= e(url_graf(['variable' => $k])) ?>"><i data-i="<?= $v[3] ?>"></i><?= $v[0] ?></a><?php endforeach; ?></div>
        <div class="card chart"><h4><i data-i="chart"></i><?= e($tituloGrafica) ?></h4>
            <?= svg_linea($puntos, $color, $dec) ?>
            <div class="lg" style="color:<?= $color ?>"><i data-i="<?= $VARIABLES[$var][3] ?>"></i> <?= mb_strtoupper($nombre) ?> (<?= $unidad ?>) · promedio por día · contado desde el último registro</div>
        </div>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
