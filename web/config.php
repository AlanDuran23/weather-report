<?php
// Conexion a MySQL (XAMPP). Es la misma base que usa la version de escritorio.
return [
    'db_host' => 'localhost',
    'db_port' => '3306',
    'db_name' => 'weather_db',
    'db_user' => 'root',
    'db_pass' => '',
    'zona_horaria' => 'America/Mexico_City',

    // Valores por defecto (iguales a ConfiguracionSistema.java). Si existe el
    // archivo de configuracion del escritorio, sus valores tienen prioridad.
    'escritorio_config' => (getenv('USERPROFILE') ?: getenv('HOME') ?: __DIR__) . '\.weatherreport\config.properties',
    'nombreEstacion' => 'Estación Central',
    'ubicacion' => 'Ciudad Principal',
    'umbralHumedad' => 80.0,
    'umbralViento' => 60.0,
    'rutaReportes' => (getenv('USERPROFILE') ?: getenv('HOME') ?: __DIR__) . '\WeatherReportReportes',
];
