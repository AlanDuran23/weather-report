<?php
date_default_timezone_set((require __DIR__ . '/../config.php')['zona_horaria']);

// db() devuelve una unica conexion PDO para toda la peticion
function db(): PDO
{
    static $pdo = null;
    if ($pdo === null) {
        $c = require __DIR__ . '/../config.php';
        $dsn = "mysql:host={$c['db_host']};port={$c['db_port']};dbname={$c['db_name']};charset=utf8mb4";
        $pdo = new PDO($dsn, $c['db_user'], $c['db_pass'], [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
            PDO::ATTR_CASE => PDO::CASE_LOWER, // la columna puede llamarse Observaciones u observaciones
        ]);
    }
    return $pdo;
}
