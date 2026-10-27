<?php
// Reporte listo para imprimir / guardar como PDF (equivale a PDFGenerator del escritorio)
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/reporte.php';
requerir_pagina('reportes');

$cfg = config_sistema();
$p = parametros_reporte();
$r = resumen_periodo($p['ini'], $p['fin']);
$registros = registros_periodo($p['ini'], $p['fin']);
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title><?= e($p['nombre'] . ' ' . $p['periodo']) ?> | Weather Forecast</title>
    <style>
        /* mismos colores vino que la pagina (tema "vino" de css/style.css) */
        body{font-family:Georgia,'Times New Roman',serif;color:#2b0d15;margin:24px;font-size:13px}
        header{border-bottom:3px solid #800020;padding-bottom:12px;margin-bottom:16px;display:flex;justify-content:space-between;align-items:flex-end}
        h1{margin:0;font-size:22px;color:#800020} h2{font-size:15px;color:#800020;border-bottom:1px solid #e4cdd3;padding-bottom:4px;margin:20px 0 8px}
        .sub{color:#75575f} p{line-height:1.6;margin:0 0 8px;text-align:justify}
        table{width:100%;border-collapse:collapse;font:12px system-ui,'Segoe UI',Arial,sans-serif;font-variant-numeric:tabular-nums}
        th,td{border:1px solid #e4cdd3;padding:4px 6px;text-align:left} th.n,td.n{text-align:right}
        th{background:#f6ecee;color:#5c0016;font-weight:600;-webkit-print-color-adjust:exact;print-color-adjust:exact}
        .tot td:first-child{width:40%;color:#75575f} tr{break-inside:avoid}
        .no-print{margin-bottom:14px} @media print{.no-print{display:none}body{margin:0}}
    </style>
</head>
<body>
    <div class="no-print"><button onclick="print()">Imprimir / Guardar como PDF</button></div>
    <header>
        <div><h1>WEATHER FORECAST · <?= e($p['nombre']) ?></h1><div class="sub"><?= e($cfg['nombreEstacion'] . ' - ' . $cfg['ubicacion']) ?></div></div>
        <div class="sub" style="text-align:right">Periodo: <b><?= e($p['periodo']) ?></b><br>Generado: <?= date('d/m/Y H:i') ?></div>
    </header>
    <h2>Narrativa meteorológica</h2>
    <?php foreach (narrativa($p, $r) as $parrafo): ?><p><?= e($parrafo) ?></p><?php endforeach; ?>
<?php if ($registros): ?>
    <h2>Resumen de totales</h2>
    <table class="tot"><?php foreach (totales($r) as [$k, $v]): ?><tr><td><?= e($k) ?></td><td><b><?= e($v) ?></b></td></tr><?php endforeach; ?></table>
    <h2>Detalle de registros</h2>
    <table>
        <thead><tr><th>Fecha</th><th>Hora</th><th class="n">Temp °C</th><th class="n">Humedad %</th><th class="n">Viento km/h</th><th>Dir.</th><th class="n">Lluvia mm</th><th class="n">Presión hPa</th><th>Observaciones</th></tr></thead>
        <tbody>
<?php foreach ($registros as $x): ?>
            <tr><td><?= e($x['fecha']) ?></td><td><?= e($x['hora']) ?></td><td class="n"><?= num($x['temp_out']) ?></td><td class="n"><?= num($x['out_hum']) ?></td><td class="n"><?= num($x['wind_speed']) ?></td><td><?= e($x['wind_dir']) ?></td><td class="n"><?= num($x['rain']) ?></td><td class="n"><?= num($x['bar']) ?></td><td><?= e($x['observaciones'] ?? '') ?></td></tr>
<?php endforeach; ?>
        </tbody>
    </table>
<?php endif; ?>
    <script>addEventListener('load', function () { print() })</script>
</body>
</html>
