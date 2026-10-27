<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/registros.php';
$pagina = 'usuarios';
$titulo = 'Gestión de Usuarios';
$icono = 'users';
$usuario = requerir_pagina($pagina);

const ROLES = ['Ingeniero', 'Consulta'];
$yo = (int)$usuario['id'];
$error = null;
$form = null; // datos del formulario de alta/edicion cuando esta abierto

function buscar_usuario(int $id): ?array
{
    $st = db()->prepare('SELECT id, nombre_completo, usuario, rol, activo + 0 AS activo FROM usuarios WHERE id = ?');
    $st->execute([$id]);
    return $st->fetch() ?: null;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    exigir_csrf('usuarios.php');
    $accion = $_POST['accion'] ?? '';
    $id = (int)($_POST['id'] ?? 0);
    try {
        if ($accion === 'estado' && ($u = buscar_usuario($id))) {
            if ($id === $yo) {
                avisar('r', 'No puede desactivar su propia cuenta.');
            } else {
                // activo es BIT(1): el parametro llega como texto y '0' se guardaria como 1, por eso CAST
                db()->prepare('UPDATE usuarios SET activo = CAST(? AS UNSIGNED) WHERE id = ?')->execute([$u['activo'] ? 0 : 1, $id]);
                avisar('g', 'Usuario ' . $u['usuario'] . ($u['activo'] ? ' desactivado.' : ' activado.'));
            }
            redirigir('usuarios.php');
        }
        if ($accion === 'eliminar' && ($u = buscar_usuario($id))) {
            if ($id === $yo) {
                avisar('r', 'No puede eliminar su propia cuenta.');
            } else {
                db()->prepare('DELETE FROM usuarios WHERE id = ?')->execute([$id]);
                avisar('g', 'Usuario ' . $u['usuario'] . ' eliminado.');
            }
            redirigir('usuarios.php');
        }
        if ($accion === 'guardar') {
            // Validaciones de UsuarioController.registrar / actualizar
            $form = ['id' => $id, 'nombre_completo' => trim($_POST['nombre_completo'] ?? ''),
                'usuario' => trim($_POST['usuario'] ?? ''), 'rol' => in_array($_POST['rol'] ?? '', ROLES, true) ? $_POST['rol'] : 'Consulta'];
            $clave = $_POST['contrasena'] ?? '';
            $st = db()->prepare('SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND id <> ?');
            $st->execute([$form['usuario'], $id]);
            if ($form['nombre_completo'] === '') {
                $error = 'El nombre completo es obligatorio.';
            } elseif ($form['usuario'] === '') {
                $error = 'El nombre de usuario es obligatorio.';
            } elseif ($st->fetchColumn() > 0) {
                $error = 'El nombre de usuario ya está registrado.';
            } elseif ((!$id || $clave !== '') && mb_strlen($clave) < 6) {
                $error = 'La contraseña debe tener al menos 6 caracteres.';
            } elseif ($id === $yo && $form['rol'] !== 'Ingeniero') {
                $error = 'No puede quitarse a sí mismo el rol de Ingeniero.';
            } elseif ($id) {
                $sql = 'UPDATE usuarios SET nombre_completo = ?, usuario = ?, rol = ?' . ($clave !== '' ? ', contrasena_hash = ?' : '') . ' WHERE id = ?';
                $p = [$form['nombre_completo'], $form['usuario'], $form['rol']];
                if ($clave !== '') {
                    $p[] = hash_contrasena($clave);
                }
                $p[] = $id;
                db()->prepare($sql)->execute($p);
                if ($id === $yo) { // refleja el cambio en el encabezado
                    $_SESSION['usuario'] = array_merge($_SESSION['usuario'], array_intersect_key($form, array_flip(['nombre_completo', 'usuario', 'rol'])));
                }
                avisar('g', 'Usuario ' . $form['usuario'] . ' actualizado.');
                redirigir('usuarios.php');
            } else {
                db()->prepare('INSERT INTO usuarios (nombre_completo, usuario, contrasena_hash, rol, activo) VALUES (?, ?, ?, ?, 1)')
                    ->execute([$form['nombre_completo'], $form['usuario'], hash_contrasena($clave), $form['rol']]);
                avisar('g', 'Usuario ' . $form['usuario'] . ' creado.');
                redirigir('usuarios.php');
            }
        }
    } catch (PDOException $ex) {
        $error = 'Error de base de datos: ' . $ex->getMessage();
    }
} elseif (isset($_GET['nuevo'])) {
    $form = ['id' => 0, 'nombre_completo' => '', 'usuario' => '', 'rol' => 'Consulta'];
} elseif (isset($_GET['editar'])) {
    $form = buscar_usuario((int)$_GET['editar']);
}

$lista = db()->query('SELECT id, nombre_completo, usuario, rol, activo + 0 AS activo, fecha_registro FROM usuarios ORDER BY id')->fetchAll();

require __DIR__ . '/includes/header.php';
?>
    <main class="sec">
        <?= mostrar_avisos() ?>
        <?php if ($error): ?><div class="al r"><i data-i="alert"></i><span><?= e($error) ?></span></div><?php endif; ?>
<?php if ($form): ?>
        <form class="card" method="post" action="usuarios.php" style="max-width:640px">
            <?= campo_csrf() ?><input type="hidden" name="accion" value="guardar"><input type="hidden" name="id" value="<?= (int)$form['id'] ?>">
            <h4><i data-i="<?= $form['id'] ? 'edit' : 'userplus' ?>"></i><?= $form['id'] ? 'Editar usuario' : 'Nuevo usuario' ?></h4>
            <div class="fg" style="grid-template-columns:1fr 1fr">
                <div><label for="nombre_completo">Nombre completo</label><input id="nombre_completo" name="nombre_completo" value="<?= e($form['nombre_completo']) ?>" maxlength="100" required></div>
                <div><label for="u">Usuario</label><input id="u" name="usuario" value="<?= e($form['usuario']) ?>" maxlength="50" autocomplete="off" required></div>
                <div><label for="rol">Rol</label><select id="rol" name="rol"><?php foreach (ROLES as $r): ?><option<?= $r === $form['rol'] ? ' selected' : '' ?>><?= $r ?></option><?php endforeach; ?></select></div>
                <div><label for="contrasena">Contraseña<?= $form['id'] ? ' (vacía = no cambiar)' : '' ?></label><input type="password" id="contrasena" name="contrasena" minlength="6" autocomplete="new-password"<?= $form['id'] ? '' : ' required' ?>></div>
            </div>
            <div class="ac" style="margin-top:14px"><a class="btn" href="usuarios.php"><i data-i="x"></i>Cancelar</a><button class="btn p"><i data-i="disk"></i>Guardar</button></div>
            <p class="note">Ingeniero: acceso total. Consulta: solo Panel, Histórico, Gráficas y Reportes.</p>
        </form>
<?php else: ?>
        <div class="bar"><a class="btn p" href="usuarios.php?nuevo=1"><i data-i="userplus"></i>Nuevo usuario</a></div>
<?php endif; ?>
        <div class="tw"><table><thead><tr><th class="n">ID</th><th>Nombre completo</th><th>Usuario</th><th>Rol</th><th>Estado</th><th>Fecha registro</th><th>Acciones</th></tr></thead><tbody>
<?php foreach ($lista as $u): ?>
            <tr><td class="n"><?= $u['id'] ?></td><td><?= e($u['nombre_completo']) ?><?= (int)$u['id'] === $yo ? ' <span class="sub">(usted)</span>' : '' ?></td><td><?= e($u['usuario']) ?></td><td><?= e($u['rol']) ?></td>
                <td class="<?= $u['activo'] ? 'ok' : 'off' ?>"><?= $u['activo'] ? 'Activo' : 'Inactivo' ?></td><td><?= date('d/m/Y H:i', strtotime($u['fecha_registro'])) ?></td>
                <td><form method="post" action="usuarios.php" class="acciones"><?= campo_csrf() ?><input type="hidden" name="id" value="<?= $u['id'] ?>">
                    <a class="btn s" href="usuarios.php?editar=<?= $u['id'] ?>"><i data-i="edit"></i>Editar</a>
<?php if ((int)$u['id'] !== $yo): ?>
                    <button class="btn s" name="accion" value="estado"><i data-i="power"></i><?= $u['activo'] ? 'Desactivar' : 'Activar' ?></button>
                    <button class="btn s d" name="accion" value="eliminar" data-confirmar="¿Eliminar al usuario <?= e($u['usuario']) ?>? Esta acción no se puede deshacer."><i data-i="trash"></i>Eliminar</button>
<?php endif; ?>
                </form></td></tr>
<?php endforeach; ?>
        </tbody></table></div>
    </main>
<?php require __DIR__ . '/includes/footer.php'; ?>
