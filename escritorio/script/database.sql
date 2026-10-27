-- ============================================================
--  Weather Report - Script de creación de la base de datos
--  Motor: MySQL | Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS weather_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE weather_db;

-- ------------------------------------------------------------
-- Tabla: usuarios
-- contrasena_hash: PBKDF2-HMAC-SHA256 con sal ("pbkdf2_sha256$iter$sal$hash").
-- Se aceptan hashes SHA-256 (hex) antiguos; se migran al iniciar sesión.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo      VARCHAR(100)  NOT NULL,
    usuario              VARCHAR(50)   NOT NULL UNIQUE,
    contrasena_hash      VARCHAR(255)  NOT NULL,
    rol                  VARCHAR(20)   NOT NULL DEFAULT 'Consulta',
    codigo_recuperacion  CHAR(64)      NULL,
    codigo_expiracion    DATETIME      NULL,
    fecha_registro       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo               TINYINT(1)    NOT NULL DEFAULT 1
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Usuario por defecto (rol Ingeniero: acceso total)
-- usuario: admin    contraseña: admin123  (hash SHA-256)
-- ------------------------------------------------------------
INSERT INTO usuarios (nombre_completo, usuario, contrasena_hash, rol)
VALUES ('Administrador Principal', 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'Ingeniero');

-- ------------------------------------------------------------
-- Tabla: report
-- Registros meteorológicos de la estación (una fila por medición).
-- fecha/hora en el formato de la estación ("16/01/26", "6:00 p").
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    fecha               VARCHAR(10) NULL,
    hora                VARCHAR(8)  NULL,
    temp_out            DOUBLE      NULL,
    hi_temp             DOUBLE      NULL,
    low_temp            DOUBLE      NULL,
    dew_pt              DOUBLE      NULL,
    wind_chill          DOUBLE      NULL,
    heat_index          DOUBLE      NULL,
    thw_index           DOUBLE      NULL,
    out_hum             DOUBLE      NULL,
    wind_speed          DOUBLE      NULL,
    wind_dir            VARCHAR(8)  NULL,
    wind_run            DOUBLE      NULL,
    hi_speed            DOUBLE      NULL,
    hi_dir              VARCHAR(8)  NULL,
    bar                 DOUBLE      NULL,
    rain                DOUBLE      NULL,
    rain_rate           DOUBLE      NULL,
    heat_dd             DOUBLE      NULL,
    cool_dd             DOUBLE      NULL,
    in_temp             DOUBLE      NULL,
    in_hum              DOUBLE      NULL,
    in_dew              DOUBLE      NULL,
    in_heat             DOUBLE      NULL,
    in_emc              DOUBLE      NULL,
    in_air_density      DOUBLE      NULL,
    wind_samp           INT         NULL,
    iss_tx              INT         NULL,
    arc_int             INT         NULL,
    recept              DOUBLE      NULL,
    Observaciones       TEXT        NULL
) ENGINE = InnoDB;
