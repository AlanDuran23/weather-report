<?php
// Detalle de un registro (equivale a DetalleRegistroDialog del escritorio)
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/registros.php';
$pagina = 'historico';
$titulo = 'Detalle del registro';
$icono = 'eye';
$usuario = requerir_pagina($pagina);

$st = db()->prepare('SELECT * FROM report WHERE id = ?');
$st->execute([(int)($_GET['id'] ?? 0)]);
$r = $st->fetch();

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <div class="bar no-print">
            <a class="btn" href="javascript:history.back()"><i data-i="left"></i>Regresar</a>
            <?php if ($r && es_ingeniero()): ?><a class="btn p" href="ingreso.php?id=<?= $r['id'] ?>"><i data-i="edit"></i>Editar registro</a><?php endif; ?>
        </div>
<?php if (!$r): ?>
        <div class="al r"><i data-i="alert"></i><span>No existe un registro con ese ID.</span></div>
<?php else: ?>
        <div class="card">
            <h4><i data-i="calendar"></i>Registro <?= $r['id'] ?> · <?= e($r['fecha'] . ' ' . $r['hora']) ?></h4>
<?php foreach (SECCIONES as [$ic, $nombre, $lista]): ?>
            <h4><i data-i="<?= $ic ?>"></i><?= $nombre ?></h4>
            <div class="kv">
<?php foreach ($lista as $col => [$etiqueta, $tipo]): ?>
                <div><span><?= e($etiqueta) ?></span><b><?= $tipo === 't' ? e($r[$col] ?: '--') : ($tipo === 'i' ? e($r[$col] ?? '--') : num($r[$col])) ?></b></div>
<?php endforeach; ?>
            </div>
<?php endforeach; ?>
            <h4><i data-i="activity"></i>Observaciones</h4>
            <p style="margin:0"><?= e(trim((string)$r['observaciones']) ?: '--') ?></p>
        </div>
<?php endif; ?>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
