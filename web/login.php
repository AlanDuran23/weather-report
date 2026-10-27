<?php
// Inicio de sesion y cuenta de consulta (equivale a LoginController).
// Si alguien olvida su contraseña, un Ingeniero se la cambia desde Gestion de Usuarios.
require_once __DIR__ . '/includes/auth.php';

if (usuario_actual()) {
    header('Location: index.php');
    exit;
}

$error = '';          // error del formulario de login
$aviso = null;        // [tipo, texto] dentro de la ventana de registro
$exito = '';          // mensaje verde sobre el login
$abrir = '';          // 'registro' para mostrar esa ventana al cargar
$v = fn($k) => trim($_POST[$k] ?? '');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $accion = $_POST['accion'] ?? 'login';
    try {
        if (!verificar_csrf()) {
            $error = 'La sesión expiró. Intente de nuevo.';
        } elseif ($accion === 'registro') {
            // Cuenta de solo consulta (LoginController.crearCuentaConsulta)
            $abrir = 'registro';
            $clave = $_POST['clave_reg'] ?? '';
            $st = db()->prepare('SELECT COUNT(*) FROM usuarios WHERE usuario = ?');
            $st->execute([$v('usuario_reg')]);
            if ($v('nombre_reg') === '') {
                $aviso = ['r', 'El nombre completo es obligatorio.'];
            } elseif ($v('usuario_reg') === '') {
                $aviso = ['r', 'El nombre de usuario es obligatorio.'];
            } elseif (mb_strlen($clave) < 6) {
                $aviso = ['r', 'La contraseña debe tener al menos 6 caracteres.'];
            } elseif ($clave !== ($_POST['confirmar_reg'] ?? '')) {
                $aviso = ['r', 'Las contraseñas no coinciden.'];
            } elseif ($st->fetchColumn() > 0) {
                $aviso = ['r', 'El nombre de usuario ya está registrado.'];
            } else {
                db()->prepare("INSERT INTO usuarios (nombre_completo, usuario, contrasena_hash, rol, activo) VALUES (?, ?, ?, 'Consulta', 1)")
                    ->execute([$v('nombre_reg'), $v('usuario_reg'), hash_contrasena($clave)]);
                $abrir = '';
                $exito = 'Cuenta de consulta creada. Ya puede iniciar sesión.';
            }
        } else {
            if ($v('usuario') === '' || ($_POST['contrasena'] ?? '') === '') {
                $error = 'Ingrese usuario y contraseña.';
            } elseif ($espera = login_bloqueado()) {
                $error = 'Demasiados intentos fallidos. Intente de nuevo en ' . (int)ceil($espera / 60) . ' min.';
            } elseif ($u = autenticar($v('usuario'), $_POST['contrasena'])) {
                limpiar_fallos_login();
                session_regenerate_id(true);
                $_SESSION['usuario'] = $u;
                header('Location: index.php');
                exit;
            } else {
                registrar_fallo_login();
                $error = 'Credenciales inválidas o usuario inactivo.';
            }
        }
    } catch (PDOException $ex) {
        $error = 'No se pudo conectar a la base de datos. Verifique que el servicio de MySQL esté activo.';
        $abrir = '';
    }
}

function caja(?array $aviso): string
{
    return $aviso ? '<div class="al ' . $aviso[0] . '"><i data-i="' . ($aviso[0] === 'g' ? 'check' : 'alert') . '"></i><span>' . e($aviso[1]) . '</span></div>' : '';
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesión | Weather Forecast</title>
    <?php require __DIR__ . '/includes/tema.php'; ?>
    <link rel="stylesheet" href="<?= recurso('css/style.css') ?>">
</head>
<body class="loginpage">
    <section class="lft">
        <div class="logos"><img src="img/logo_weather_report.png" alt="Weather Report"></div>
        <span class="tag">ESTACIÓN METEOROLÓGICA</span>
        <h1>WEATHER FORECAST</h1>
        <p>Sistema de monitoreo meteorológico</p>
    </section>
    <section class="rgt">
        <form class="lc" method="post" action="login.php">
            <h2><i data-i="lock"></i>Iniciar sesión</h2>
            <p>Ingrese sus credenciales para continuar</p>
            <?= caja($error ? ['r', $error] : ($exito ? ['g', $exito] : null)) ?>
            <input type="hidden" name="csrf" value="<?= e(token_csrf()) ?>"><input type="hidden" name="accion" value="login">
            <div class="f"><label for="usuario"><i data-i="user"></i>Usuario</label><input type="text" id="usuario" name="usuario" value="<?= e($v('usuario')) ?>" autocomplete="username" required<?= $abrir ? '' : ' autofocus' ?>></div>
            <div class="f"><label for="contrasena"><i data-i="key"></i>Contraseña</label><input type="password" id="contrasena" name="contrasena" autocomplete="current-password" required></div>
            <button class="btn p full"><i data-i="login"></i>Entrar</button>
            <div class="row" style="justify-content:center">
                <button type="button" class="btn s" data-abrir="registro"><i data-i="userplus"></i>Crear cuenta de consulta</button>
            </div>
            <a class="volver" href="index.php"><i data-i="left"></i>Volver al Panel Principal</a>
        </form>
    </section>
    <dialog class="modal" id="registro" aria-labelledby="registro-t"<?= $abrir === 'registro' ? ' data-auto' : '' ?>>
        <form class="mb" method="post" action="login.php">
            <h3 id="registro-t"><i data-i="userplus"></i>Crear cuenta de consulta</h3>
            <?= $abrir === 'registro' ? caja($aviso) : '' ?>
            <input type="hidden" name="csrf" value="<?= e(token_csrf()) ?>"><input type="hidden" name="accion" value="registro">
            <div class="f"><label for="nombre_reg"><i data-i="user"></i>Nombre completo</label><input id="nombre_reg" name="nombre_reg" value="<?= e($v('nombre_reg')) ?>" maxlength="100" required></div>
            <div class="f"><label for="usuario_reg"><i data-i="user"></i>Usuario</label><input id="usuario_reg" name="usuario_reg" value="<?= e($v('usuario_reg')) ?>" maxlength="50" autocomplete="off" required></div>
            <div class="f"><label for="clave_reg"><i data-i="lock"></i>Contraseña (mínimo 6 caracteres)</label><input type="password" id="clave_reg" name="clave_reg" minlength="6" autocomplete="new-password" required></div>
            <div class="f"><label for="confirmar_reg"><i data-i="lock"></i>Confirmar contraseña</label><input type="password" id="confirmar_reg" name="confirmar_reg" minlength="6" autocomplete="new-password" required></div>
            <p class="note" style="margin:0 0 12px">La cuenta de consulta solo puede ver Panel, Histórico, Gráficas y Reportes.</p>
            <div class="ac"><button type="button" class="btn" data-cerrar><i data-i="x"></i>Cerrar</button><button class="btn p"><i data-i="userplus"></i>Crear cuenta</button></div>
        </form>
    </dialog>
    <script src="<?= recurso('js/icons.js') ?>"></script>
    <script src="<?= recurso('js/app.js') ?>"></script>
</body>
</html>
