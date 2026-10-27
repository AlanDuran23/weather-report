<?php
// Sesion, login y permisos por rol (mismas reglas que MainController del escritorio)
require_once __DIR__ . '/db.php';

session_start([
    'cookie_httponly' => true,
    'cookie_samesite' => 'Lax',
    'use_strict_mode' => true,
]);

// Paginas que puede ver el rol Consulta; Ingeniero ve todo; sin sesion solo el Panel Principal
const PAGINAS_CONSULTA = ['index', 'historico', 'graficas', 'reportes'];
const PAGINAS_INVITADO = ['index'];
const INVITADO = ['id' => 0, 'nombre_completo' => 'Invitado', 'usuario' => '', 'rol' => 'Invitado'];

function e($s): string
{
    return htmlspecialchars((string)$s, ENT_QUOTES, 'UTF-8');
}

// css/js con ?v=fecha de modificacion: al cambiar el archivo el navegador descarga el nuevo
// en lugar de usar el que tiene en cache
function recurso(string $ruta): string
{
    return $ruta . '?v=' . @filemtime(__DIR__ . '/../' . $ruta);
}

function usuario_actual(): ?array
{
    return $_SESSION['usuario'] ?? null;
}

function es_ingeniero(): bool
{
    return (usuario_actual()['rol'] ?? '') === 'Ingeniero';
}

function puede_ver(string $pagina): bool
{
    if (usuario_actual() === null) {
        return in_array($pagina, PAGINAS_INVITADO, true);
    }
    return es_ingeniero() || in_array($pagina, PAGINAS_CONSULTA, true);
}

// Llamar al inicio de cada pagina protegida
function requerir_pagina(string $pagina): array
{
    $u = usuario_actual();
    if ($u !== null) {
        // Si lo desactivaron o eliminaron mientras tenia sesion, se cierra; el rol se refresca
        $st = db()->prepare('SELECT id, nombre_completo, usuario, rol FROM usuarios WHERE id = ? AND activo = 1');
        $st->execute([$u['id']]);
        $u = $st->fetch() ?: null;
        $_SESSION['usuario'] = $u;
    }
    if ($u === null) {
        if (in_array($pagina, PAGINAS_INVITADO, true)) {
            return INVITADO;
        }
        header('Location: login.php');
        exit;
    }
    if (!puede_ver($pagina)) {
        header('Location: index.php?sinpermiso=1');
        exit;
    }
    return $u;
}

// Contrasenas: PBKDF2-HMAC-SHA256 con sal, mismo formato que HashUtil.java
// ("pbkdf2_sha256$iteraciones$sal_base64$hash_base64"). Se siguen aceptando los
// hashes SHA-256 sin sal antiguos y se migran al iniciar sesion.
const PBKDF2_PREFIJO = 'pbkdf2_sha256';
const PBKDF2_ITERACIONES = 600000;

function hash_contrasena(string $contrasena): string
{
    asegurar_columna_hash();
    $sal = random_bytes(16);
    $hash = hash_pbkdf2('sha256', $contrasena, $sal, PBKDF2_ITERACIONES, 32, true);
    return PBKDF2_PREFIJO . '$' . PBKDF2_ITERACIONES . '$' . base64_encode($sal) . '$' . base64_encode($hash);
}

function verificar_contrasena(string $contrasena, string $guardado): bool
{
    $guardado = trim($guardado);
    $p = explode('$', $guardado);
    if (count($p) === 4 && $p[0] === PBKDF2_PREFIJO) {
        $iteraciones = (int)$p[1];
        $sal = base64_decode($p[2], true);
        $esperado = base64_decode($p[3], true);
        if ($iteraciones < 1 || $sal === false || $esperado === false || $esperado === '') {
            return false;
        }
        return hash_equals($esperado, hash_pbkdf2('sha256', $contrasena, $sal, $iteraciones, strlen($esperado), true));
    }
    return hash_equals(strtolower($guardado), hash('sha256', $contrasena));
}

// El hash nuevo no cabe en CHAR(64); ensancha la columna si hace falta (idempotente)
function asegurar_columna_hash(): void
{
    $largo = db()->query("SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS
                          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuarios'
                          AND COLUMN_NAME = 'contrasena_hash'")->fetchColumn();
    if ($largo !== false && (int)$largo < 255) {
        db()->exec('ALTER TABLE usuarios MODIFY contrasena_hash VARCHAR(255) NOT NULL');
    }
}

// Limite de intentos de login por IP: con 5 fallos en 15 minutos se bloquea
// hasta que el mas antiguo salga de la ventana (frena la fuerza bruta)
const INTENTOS_MAX = 5;
const INTENTOS_VENTANA = 900;

function archivo_intentos(): string
{
    return sys_get_temp_dir() . '/wr_login_' . hash('sha256', $_SERVER['REMOTE_ADDR'] ?? 'local') . '.json';
}

function fallos_recientes(): array
{
    $f = archivo_intentos();
    $lista = is_file($f) ? json_decode((string)@file_get_contents($f), true) : [];
    $limite = time() - INTENTOS_VENTANA;
    return array_values(array_filter(is_array($lista) ? $lista : [], fn($t) => is_int($t) && $t > $limite));
}

// Segundos que faltan para poder intentar de nuevo; 0 si no esta bloqueado
function login_bloqueado(): int
{
    $fallos = fallos_recientes();
    return count($fallos) >= INTENTOS_MAX ? max(1, min($fallos) + INTENTOS_VENTANA - time()) : 0;
}

function registrar_fallo_login(): void
{
    $fallos = fallos_recientes();
    $fallos[] = time();
    @file_put_contents(archivo_intentos(), json_encode($fallos), LOCK_EX);
}

function limpiar_fallos_login(): void
{
    @unlink(archivo_intentos());
}

function autenticar(string $usuario, string $contrasena): ?array
{
    $st = db()->prepare('SELECT id, nombre_completo, usuario, rol, contrasena_hash FROM usuarios
                         WHERE usuario = ? AND activo = 1');
    $st->execute([$usuario]);
    $fila = $st->fetch();
    if (!$fila || !verificar_contrasena($contrasena, (string)$fila['contrasena_hash'])) {
        return null;
    }
    if (!str_starts_with((string)$fila['contrasena_hash'], PBKDF2_PREFIJO . '$')) {
        db()->prepare('UPDATE usuarios SET contrasena_hash = ? WHERE id = ?')
            ->execute([hash_contrasena($contrasena), $fila['id']]);
    }
    unset($fila['contrasena_hash']);
    return $fila;
}

function token_csrf(): string
{
    if (empty($_SESSION['csrf'])) {
        $_SESSION['csrf'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf'];
}

function verificar_csrf(): bool
{
    return hash_equals($_SESSION['csrf'] ?? '', $_POST['csrf'] ?? '');
}
