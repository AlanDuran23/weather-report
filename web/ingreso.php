<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/importador.php';
$pagina = 'ingreso';
$titulo = 'Ingreso de Datos';
$icono = 'edit';
$usuario = requerir_pagina($pagina);

// Solo los datos mas relevantes. Al editar, las demas columnas del registro no se tocan;
// el archivo TXT de la estacion sigue importando las 30 columnas.
$campos = [
    'temp_out' => ['Temperatura (°C)', 'n'],
    'out_hum' => ['Humedad relativa (%)', 'n'],
    'wind_speed' => ['Velocidad del viento (km/h)', 'n'],
    'wind_dir' => ['Dirección del viento', 't'],
    'rain' => ['Lluvia (mm)', 'n'],
];
$valores = [];   // lo que se muestra en el formulario
$id = null;      // registro en edicion
$error = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    exigir_csrf('ingreso.php');

    if (($_POST['accion'] ?? '') === 'importar') {
        $f = $_FILES['archivo'] ?? null;
        if (!$f || $f['error'] !== UPLOAD_ERR_OK) {
            avisar('r', 'Seleccione un archivo para importar.');
        } elseif (!preg_match('/\.(txt|csv)$/i', $f['name'])) {
            avisar('r', 'Formato no soportado. Use .txt o .csv (exportación de la estación o CSV).');
        } else {
            [$registros, $errores] = leer_archivo(file_get_contents($f['tmp_name']));
            if (!$registros) {
                avisar('r', "El archivo no contiene datos válidos.\n" . implode("\n", array_slice($errores, 0, 3)));
            } else {
                try {
                    [$ok, $dup] = insertar_registros($registros);
                    $msg = "Se importaron $ok registros desde " . $f['name'] . '.';
                    if ($dup) {
                        $msg .= "\nSe omitieron $dup registros que ya existían (misma fecha y hora).";
                    }
                    if ($errores) {
                        $msg .= "\nSe omitieron " . count($errores) . " filas con errores:\n" . implode("\n", array_slice($errores, 0, 3));
                    }
                    avisar($ok ? 'g' : 'a', $msg);
                } catch (PDOException $ex) {
                    avisar('r', 'Error de base de datos: ' . $ex->getMessage());
                }
            }
        }
        redirigir('ingreso.php');
    }

    // Guardar (nuevo o edicion), con las validaciones de RegistroController
    $id = (int)($_POST['id'] ?? 0) ?: null;
    $valores = array_map(fn($v) => is_string($v) ? trim($v) : '', $_POST);
    $d = DateTime::createFromFormat('!Y-m-d H:i', ($valores['fecha'] ?? '') . ' ' . ($valores['hora'] ?? ''));
    $fila = [];
    foreach ($campos as $col => [$etiqueta, $tipo]) {
        $v = $valores[$col] ?? '';
        if ($tipo === 't') {
            $fila[$col] = mb_substr(strtoupper($v), 0, 8);
        } elseif ($v === '') {
            $fila[$col] = null;
        } elseif (!is_numeric($v)) {
            $error ??= "El campo \"$etiqueta\" debe ser un número.";
        } else {
            $fila[$col] = $tipo === 'i' ? (int)$v : (float)$v;
        }
    }
    if (!$d) {
        $error = 'Indique la fecha y la hora del registro.';
    } elseif (!is_numeric($valores['out_hum'] ?? '') || $valores['out_hum'] < 0 || $valores['out_hum'] > 100) {
        $error = 'La humedad debe estar entre 0 y 100 %.';
    } elseif (!is_numeric($valores['wind_speed'] ?? '') || $valores['wind_speed'] < 0) {
        $error = 'La velocidad del viento debe ser un número mayor o igual a 0.';
    } elseif (!is_numeric($valores['rain'] ?? '') || $valores['rain'] < 0) {
        $error = 'La lluvia debe ser un número mayor o igual a 0.';
    }
    if (!$error) {
        $fila = ['fecha' => fecha_estacion($d), 'hora' => hora_estacion($d)] + $fila
            + ['observaciones' => $valores['observaciones'] ?? ''];
        $st = db()->prepare('SELECT id FROM report WHERE fecha = ? AND hora = ? AND id <> ? LIMIT 1');
        $st->execute([$fila['fecha'], $fila['hora'], $id ?? 0]);
        if ($otro = $st->fetchColumn()) {
            $error = "Ya existe un registro con esa fecha y hora (ID $otro).";
        }
    }
    if (!$error) {
        try {
            $cols = array_keys($fila);
            if ($id) {
                $st = db()->prepare('UPDATE report SET ' . implode(', ', array_map(fn($c) => "$c = ?", $cols)) . ' WHERE id = ?');
                $st->execute([...array_values($fila), $id]);
                avisar('g', "Registro $id actualizado correctamente.");
            } else {
                $st = db()->prepare('INSERT INTO report (' . implode(', ', $cols) . ') VALUES (' . implode(', ', array_fill(0, count($cols), '?')) . ')');
                $st->execute(array_values($fila));
                avisar('g', 'Registro ' . db()->lastInsertId() . ' guardado correctamente.');
            }
            redirigir('ingreso.php');
        } catch (PDOException $ex) {
            $error = 'Error de base de datos: ' . $ex->getMessage();
        }
    }
} elseif (isset($_GET['id']) && $_GET['id'] !== '') {
    // Cargar registro por ID para editarlo
    $st = db()->prepare('SELECT * FROM report WHERE id = ?');
    $st->execute([(int)$_GET['id']]);
    if ($r = $st->fetch()) {
        $id = (int)$r['id'];
        $valores = $r;
        $d = fecha_hora_estacion((string)$r['fecha'], (string)$r['hora']);
        $valores['fecha'] = $d ? $d->format('Y-m-d') : '';
        $valores['hora'] = $d ? $d->format('H:i') : '';
    } else {
        $error = 'No existe un registro con el ID ' . (int)$_GET['id'] . '.';
    }
}

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <?= mostrar_avisos() ?>
        <?php if ($error): ?><div class="al r"><i data-i="alert"></i><span><?= e($error) ?></span></div><?php endif; ?>
        <div class="fl" style="margin-bottom:18px;justify-content:space-between">
            <form class="fl" method="get" action="ingreso.php"><div style="width:150px"><label for="buscar-id"><i data-i="search"></i>ID del registro</label><input type="number" min="1" id="buscar-id" name="id" value="<?= e($_GET['id'] ?? '') ?>"></div><button class="btn s" style="margin-bottom:2px"><i data-i="download"></i>Cargar registro</button></form>
            <form class="fl" method="post" action="ingreso.php" enctype="multipart/form-data">
                <?= campo_csrf() ?><input type="hidden" name="accion" value="importar">
                <div><label for="archivo"><i data-i="upload"></i>Importar TXT / CSV de la estación</label><input type="file" id="archivo" name="archivo" accept=".txt,.csv" required style="padding:6px"></div>
                <button class="btn s" style="margin-bottom:2px"><i data-i="upload"></i>Importar</button>
            </form>
        </div>
        <form class="card" method="post" action="ingreso.php">
            <?= campo_csrf() ?>
            <?php if ($id): ?><input type="hidden" name="id" value="<?= $id ?>"><div class="al a" style="margin-top:0"><i data-i="edit"></i><span>Editando el registro <?= $id ?>. Al guardar se actualizará.</span></div><?php endif; ?>
            <h4><i data-i="calendar"></i>Fecha y hora</h4>
            <div class="fg">
                <div><label for="fecha">Fecha *</label><input type="date" id="fecha" name="fecha" value="<?= e($valores['fecha'] ?? '') ?>" required></div>
                <div><label for="hora">Hora *</label><input type="time" id="hora" name="hora" value="<?= e($valores['hora'] ?? '') ?>" required></div>
            </div>
            <h4><i data-i="cloudsun"></i>Medición</h4>
            <div class="fg">
<?php foreach ($campos as $col => [$etiqueta, $tipo]):
    $req = in_array($col, ['out_hum', 'wind_speed', 'rain'], true); ?>
                <div><label for="<?= $col ?>"><?= e($etiqueta) ?><?= $req ? ' *' : '' ?></label><input id="<?= $col ?>" name="<?= $col ?>" value="<?= e($valores[$col] ?? '') ?>"<?= $tipo === 't' ? ' maxlength="8" placeholder="Ej. SE"' : ' type="number" step="any"' ?><?= $col === 'out_hum' ? ' min="0" max="100"' : '' ?><?= in_array($col, ['wind_speed', 'rain'], true) ? ' min="0"' : '' ?><?= $req ? ' required' : '' ?>></div>
<?php endforeach; ?>
            </div>
            <h4><i data-i="activity"></i>Observaciones</h4>
            <textarea name="observaciones" rows="3" maxlength="1000"><?= e($valores['observaciones'] ?? '') ?></textarea>
            <div class="ac" style="margin-top:14px">
                <a class="btn" href="ingreso.php"><i data-i="eraser"></i>Limpiar</a>
                <button class="btn p"><i data-i="disk"></i><?= $id ? 'Actualizar registro' : 'Guardar registro' ?></button>
            </div>
            <p class="note">* Campos obligatorios. Los demás datos de la estación (presión, interior, sensores...) se cargan con "Importar TXT".</p>
        </form>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
