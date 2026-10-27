<?php
require_once __DIR__ . '/includes/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && verificar_csrf()) {
    $_SESSION = [];
    session_destroy();
}
header('Location: index.php'); // vuelve al Panel Principal como invitado
