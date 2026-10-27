<?php
if (!isset($usuario['rol'])) { http_response_code(404); exit; } // no se abre directo
// Encabezado comun. Antes de incluirlo la pagina define $pagina, $titulo e $icono
// y ya llamo a requerir_pagina() (que devuelve $usuario; sin sesion es el invitado).
$menu = [
    'index'       => ['dash',  'Panel Principal'],
    'historico'   => ['table', 'Histórico'],
    'graficas'    => ['chart', 'Gráficas'],
    'reportes'    => ['file',  'Reportes'],
    'ingreso'     => ['edit',  'Ingreso de Datos'],
    'usuarios'    => ['users', 'Gestión de Usuarios'],
];
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= e($titulo) ?> | Weather Forecast</title>
    <?php require __DIR__ . '/tema.php'; ?>
    <link rel="stylesheet" href="<?= recurso('css/style.css') ?>">
</head>
<body>
    <header class="banner<?= $pagina === 'index' ? '' : ' compacto' ?>">
        <div class="me">
<?php if (usuario_actual()): ?>
            <div><b><?= e($usuario['nombre_completo']) ?></b><small><?= e($usuario['rol']) ?></small></div>
            <form method="post" action="logout.php"><input type="hidden" name="csrf" value="<?= e(token_csrf()) ?>"><button class="btn"><i data-i="logout"></i>Cerrar sesión</button></form>
<?php else: ?>
            <div><b>Invitado</b><small>Sin sesión</small></div>
            <a class="btn" href="login.php"><i data-i="login"></i>Iniciar sesión</a>
<?php endif; ?>
        </div>
        <h2><img class="logo-wr" src="img/logo_weather_report.png" alt="Weather Report">WEATHER FORECAST</h2>
        <small>Sistema de monitoreo meteorológico</small>
    </header>
    <nav>
        <ul><?php foreach ($menu as $clave => [$ic, $texto]): if (!puede_ver($clave)) continue; ?><li><a href="<?= $clave ?>.php"<?= $clave === $pagina ? ' class="on"' : '' ?>><i data-i="<?= $ic ?>"></i><?= $texto ?></a></li><?php endforeach; ?><li class="cfg"><button type="button" data-abrir="ajustes" title="Ajustes" aria-label="Ajustes"><i data-i="gear"></i></button></li></ul>
    </nav>
    <div class="titlebar">
        <h1><i data-i="<?= e($icono) ?>"></i><?= e($titulo) ?></h1>
    </div>
<?php if (isset($_GET['sinpermiso'])): ?>
    <div class="sec" style="padding-bottom:0"><div class="al r" style="margin:0"><i data-i="lock"></i><span>Tu rol no tiene acceso a esa sección.</span></div></div>
<?php endif; ?>
