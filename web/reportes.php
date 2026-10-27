<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/reporte.php';
$pagina = 'reportes';
$titulo = 'Reportes';
$icono = 'file';
$usuario = requerir_pagina($pagina);

$p = parametros_reporte();
$r = resumen_periodo($p['ini'], $p['fin']);
$parametros = http_build_query(array_intersect_key($p, array_flip(['tipo', 'fecha', 'anio', 'mes'])));

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <div class="bar"><div class="seg" role="group" aria-label="Tipo de reporte"><?php foreach (TIPOS_REPORTE as $k => $v): ?><a class="pill<?= $k === $p['tipo'] ? ' on' : '' ?>" href="reportes.php?tipo=<?= $k ?>"><i data-i="calendar"></i><?= $v ?></a><?php endforeach; ?></div></div>
        <form class="fl" method="get" action="reportes.php" style="margin-bottom:16px">
            <input type="hidden" name="tipo" value="<?= $p['tipo'] ?>">
<?php if ($p['tipo'] === 'mensual'): ?>
            <div class="dt"><label for="anio"><i data-i="calendar"></i>Año</label><select id="anio" name="anio"><?php foreach (anios_con_datos() as $a): ?><option<?= $a === $p['anio'] ? ' selected' : '' ?>><?= $a ?></option><?php endforeach; ?></select></div>
            <div class="dt"><label for="mes"><i data-i="calendar"></i>Mes</label><select id="mes" name="mes"><?php foreach (MESES as $i => $m): ?><option value="<?= $i + 1 ?>"<?= $p['mes'] === $i + 1 ? ' selected' : '' ?>><?= $m ?></option><?php endforeach; ?></select></div>
<?php else: ?>
            <div class="dt"><label for="fecha"><i data-i="calendar"></i><?= $p['tipo'] === 'diario' ? 'Día' : 'Semana que termina el' ?></label><input type="date" id="fecha" name="fecha" value="<?= $p['fecha'] ?>"></div>
<?php endif; ?>
            <button class="btn"><i data-i="eye"></i>Visualizar</button>
            <a class="btn p" href="reporte_imprimir.php?<?= e($parametros) ?>" target="_blank"><i data-i="file"></i>Generar PDF</a>
        </form>
        <div class="card"><h4><i data-i="cloudsun"></i>Narrativa meteorológica · <?= e($p['nombre'] . ' · ' . $p['periodo']) ?></h4>
            <div class="nar"><?php foreach (narrativa($p, $r) as $parrafo): ?><p style="margin:0 0 12px"><?= e($parrafo) ?></p><?php endforeach; ?></div>
            <p class="note">"Generar PDF" abre el reporte listo para imprimir; elija "Guardar como PDF" en la ventana de impresión. Incluye el encabezado de la estación, la narrativa, el resumen de totales y el detalle de registros.</p>
        </div>
<?php if ((int)$r['n'] > 0): ?>
        <div class="card"><h4><i data-i="activity"></i>Resumen de totales</h4>
            <div class="kv"><?php foreach (totales($r) as [$k, $v]): ?><div><span><?= e($k) ?></span><b><?= e($v) ?></b></div><?php endforeach; ?></div>
        </div>
<?php endif; ?>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
