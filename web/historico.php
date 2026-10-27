<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/registros.php';
$pagina = 'historico';
$titulo = 'Histórico';
$icono = 'table';
$usuario = requerir_pagina($pagina);

// Filtros (mismos campos que el combo del escritorio)
$CAMPOS_BUSQUEDA = ['todo' => 'Todo', 'fecha' => 'Fecha', 'hora' => 'Hora', 'temperatura' => 'Temperatura',
    'humedad' => 'Humedad', 'viento' => 'Viento', 'presion' => 'Presión', 'direccion' => 'Dirección',
    'observaciones' => 'Observaciones'];
$campo = array_key_exists($_GET['campo'] ?? '', $CAMPOS_BUSQUEDA) ? $_GET['campo'] : 'todo';
$q = trim($_GET['q'] ?? '');
$filas = in_array((int)($_GET['filas'] ?? 25), [25, 50, 100], true) ? (int)($_GET['filas'] ?? 25) : 25;
$pag = max(1, (int)($_GET['p'] ?? 1));
$cfg = config_sistema();

$num = fn($c) => "CAST(ROUND($c, 1) AS DECIMAL(10,1))"; // igual que String.format("%.1f")
$donde = [FH_SQL . ' IS NOT NULL'];
$params = [];
if ($q !== '') {
    $expr = [
        'fecha' => ['fecha'], 'hora' => ['hora'], 'temperatura' => [$num('temp_out')],
        'humedad' => [$num('out_hum')], 'viento' => [$num('wind_speed')], 'presion' => [$num('bar')],
        'direccion' => ["CONCAT_WS(' ', wind_dir, hi_dir)"], 'observaciones' => ['observaciones'],
        'todo' => ['Observaciones', 'fecha', 'hora', $num('temp_out'), $num('out_hum'), $num('wind_speed'),
            $num('bar'), $num('rain'), 'wind_dir'],
    ][$campo];
    $like = '%' . addcslashes($q, '%_\\') . '%';
    $donde[] = '(' . implode(' OR ', array_map(fn($x) => "$x LIKE ?", $expr)) . ')';
    $params = array_fill(0, count($expr), $like);
}
$where = 'WHERE ' . implode(' AND ', $donde);
$orden = 'ORDER BY ' . FH_SQL . ' DESC, id DESC';

// Exportar a Excel (CSV) e imprimir: solo Ingeniero, como en el escritorio
if (isset($_GET['exportar']) || isset($_GET['imprimir'])) {
    if (!es_ingeniero()) {
        redirigir('historico.php');
    }
}
if (isset($_GET['exportar'])) {
    $st = db()->prepare("SELECT * FROM report $where $orden");
    $st->execute($params);
    header('Content-Type: text/csv; charset=UTF-8');
    header('Content-Disposition: attachment; filename="historico_' . date('Y-m-d_His') . '.csv"');
    $out = fopen('php://output', 'w');
    fwrite($out, "\xEF\xBB\xBF"); // BOM para que Excel respete los acentos
    $c = campos();
    // Un texto que empieza con = + - @ se ejecutaria como formula en Excel; se antepone '
    $seguro = fn($x) => is_string($x) && $x !== '' && !is_numeric($x) && strpbrk($x[0], "=+-@\t\r") !== false
        ? "'" . $x : $x;
    fputcsv($out, array_merge(['ID', 'Fecha', 'Hora'], array_column($c, 0), ['observaciones']));
    while ($r = $st->fetch()) {
        $fila = [$r['id'], $r['fecha'], $r['hora']];
        foreach (array_keys($c) as $col) {
            $fila[] = $r[$col];
        }
        $fila[] = $r['observaciones'];
        fputcsv($out, array_map($seguro, $fila));
    }
    exit;
}

$st = db()->prepare("SELECT COUNT(*) FROM report $where");
$st->execute($params);
$total = (int)$st->fetchColumn();
$imprimir = isset($_GET['imprimir']);
$paginas = max(1, (int)ceil($total / $filas));
$pag = min($pag, $paginas);
$limite = $imprimir ? '' : 'LIMIT ' . $filas . ' OFFSET ' . (($pag - 1) * $filas);
$st = db()->prepare("SELECT id, fecha, hora, temp_out, out_hum, wind_speed, bar, Observaciones
                     FROM report $where $orden $limite");
$st->execute($params);
$registros = $st->fetchAll();

// Celda numerica marcada en ambar/rojo si la lectura rebasa el umbral (mismas reglas que el Panel)
function celda_umbral($v, callable $regla, float $umbral): string
{
    if ($v === null) {
        return '<td class="n">--</td>';
    }
    [$nivel, $texto] = $regla((float)$v, $umbral);
    return $nivel === 'g' ? '<td class="n">' . num($v) . '</td>'
        : '<td class="n ' . $nivel . '" title="' . e($texto) . '">' . num($v) . '</td>';
}

// Conserva los filtros en los enlaces
function url_hist(array $cambios = []): string
{
    $base = array_filter(['campo' => $_GET['campo'] ?? null, 'q' => $_GET['q'] ?? null,
        'filas' => $_GET['filas'] ?? null], fn($v) => $v !== null && $v !== '');
    return 'historico.php?' . http_build_query(array_merge($base, $cambios));
}

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <form class="fl no-print" method="get" action="historico.php" style="margin-bottom:12px">
            <div style="width:150px"><label for="campo"><i data-i="table"></i>Buscar en</label><select id="campo" name="campo"><?php foreach ($CAMPOS_BUSQUEDA as $k => $v): ?><option value="<?= $k ?>"<?= $k === $campo ? ' selected' : '' ?>><?= $v ?></option><?php endforeach; ?></select></div>
            <div style="flex:1;min-width:180px"><label for="q"><i data-i="search"></i>Texto</label><input id="q" name="q" value="<?= e($q) ?>" placeholder="Ej. 31/08/26, 16.8, lluvia"></div>
            <input type="hidden" name="filas" value="<?= $filas ?>">
            <button class="btn p"><i data-i="search"></i>Buscar</button>
            <a class="btn" href="historico.php"><i data-i="refresh"></i>Reiniciar vista</a>
            <?php if (es_ingeniero()): ?>
            <a class="btn" href="<?= e(url_hist(['exportar' => 1])) ?>"><i data-i="sheet"></i>Exportar Excel</a>
            <a class="btn" href="<?= e(url_hist(['imprimir' => 1])) ?>" target="_blank"><i data-i="printer"></i>Imprimir</a>
            <?php endif; ?>
        </form>
        <?php if ($imprimir): ?><h2 class="solo-print">Histórico de Registros Meteorológicos · <?= date('d/m/Y') ?></h2><?php endif; ?>
        <div class="sub" style="margin-bottom:8px;font-size:13px"><i data-i="database"></i> <?= number_format($total) ?> registros<?= $q !== '' ? ' encontrados' : '' ?></div>
        <div class="tw"><table><thead><tr><th class="n">ID</th><th>Fecha</th><th>Hora</th><th class="n">Temp ext (°C)</th><th class="n">Humedad %</th><th class="n">Viento km/h</th><th class="n">Presión hPa</th><th>Observaciones</th><th class="no-print"><span class="sr">Acciones</span></th></tr></thead>
        <tbody>
<?php foreach ($registros as $r): ?>
            <tr><td class="n"><?= $r['id'] ?></td><td><?= e($r['fecha']) ?></td><td><?= e($r['hora']) ?></td><td class="n"><?= num($r['temp_out']) ?></td><?= celda_umbral($r['out_hum'], 'alerta_humedad', $cfg['umbralHumedad']) ?><?= celda_umbral($r['wind_speed'], 'alerta_viento', $cfg['umbralViento']) ?><td class="n"><?= num($r['bar']) ?></td><td><?= e($r['observaciones'] ?? '') ?></td><td class="no-print accion"><a class="btn s ico" href="registro.php?id=<?= $r['id'] ?>" title="Ver detalle del registro <?= $r['id'] ?>" aria-label="Ver detalle del registro <?= $r['id'] ?>"><i data-i="eye"></i></a></td></tr>
<?php endforeach; ?>
<?php if (!$registros): ?>
            <tr><td colspan="9" class="sub" style="text-align:center">No se encontraron registros.</td></tr>
<?php endif; ?>
        </tbody></table></div>
<?php if (!$imprimir): ?>
        <form class="pg" method="get" action="historico.php">
            <?php foreach (['campo' => $campo, 'q' => $q] as $k => $v): ?><input type="hidden" name="<?= $k ?>" value="<?= e($v) ?>"><?php endforeach; ?>
            <div style="width:90px"><label for="filas">Filas</label><select id="filas" name="filas" onchange="this.form.submit()"><?php foreach ([25, 50, 100] as $n): ?><option<?= $n === $filas ? ' selected' : '' ?>><?= $n ?></option><?php endforeach; ?></select></div>
            <?php if ($pag > 1): ?><a class="btn s" href="<?= e(url_hist(['p' => $pag - 1])) ?>"><i data-i="left"></i>Anterior</a><?php else: ?><span class="btn s dis" aria-disabled="true"><i data-i="left"></i>Anterior</span><?php endif; ?>
            <span>Página <?= $pag ?> de <?= $paginas ?></span>
            <?php if ($pag < $paginas): ?><a class="btn s" href="<?= e(url_hist(['p' => $pag + 1])) ?>">Siguiente<i data-i="right"></i></a><?php else: ?><span class="btn s dis" aria-disabled="true">Siguiente<i data-i="right"></i></span><?php endif; ?>
        </form>
<?php else: ?>
        <script>addEventListener('load', function () { print() })</script>
<?php endif; ?>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
