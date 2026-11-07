-- ====================================================================
-- PROYECTO INTEGRADOR: CESDE GAMIFIED CHALLENGES
-- SCRIPT DE BASE DE DATOS Y DATOS INICIALES
-- ====================================================================

CREATE DATABASE IF NOT EXISTS proyecto_integrador_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE proyecto_integrador_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS registros;
DROP TABLE IF EXISTS retos;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS categorias;
DROP TABLE IF EXISTS empresas;

-- ####################################################################
-- TAREA INDIVIDUAL 1: MÓDULO DE CATEGORÍAS
-- ####################################################################
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    color VARCHAR(10) DEFAULT '#6366f1',
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categorias (id, nombre, codigo, descripcion, color, estado, created_at) VALUES
(1, 'Tecnología e Inteligencia Artificial', 'CAT-TECH-01', 'Proyectos de machine learning, desarrollo de software y transformación digital.', '#6366f1', 'ACTIVO', '2026-01-10 08:00:00'),
(2, 'Sostenibilidad y Energías Limpias', 'CAT-SOST-02', 'Iniciativas de economía circular, reducción de huella de carbono y energías renovables.', '#10b981', 'ACTIVO', '2026-01-12 10:15:00'),
(3, 'Fintech e Inclusión Financiera', 'CAT-FINT-03', 'Soluciones financieras ágiles, pagos digitales y microcréditos.', '#f59e0b', 'ACTIVO', '2026-02-01 15:30:00'),
(4, 'Salud y Biotecnología', 'CAT-HLTH-04', 'Innovaciones médicas, telemedicina y dispositivos diagnósticos.', '#ec4899', 'ACTIVO', '2026-02-15 09:00:00');

-- ####################################################################
-- TAREA INDIVIDUAL 2: MÓDULO DE EMPRESAS
-- ####################################################################
CREATE TABLE empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(30) NOT NULL UNIQUE,
    sector VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30) NOT NULL,
    direccion VARCHAR(200),
    ciudad VARCHAR(100) NOT NULL DEFAULT 'Medellín',
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO empresas (id, nombre, nit, sector, email, telefono, direccion, ciudad, estado, created_at) VALUES
(1, 'Innovación Digital S.A.S.', '901.452.321-8', 'Tecnología', 'contacto@innovaciondigital.co', '+57 4 444 8899', 'Cra 43A # 1-50, El Poblado', 'Medellín', 'ACTIVO', '2026-01-05 08:30:00'),
(2, 'Ecosistemas Verdes Corp', '890.102.943-4', 'Medio Ambiente', 'info@ecosistemasverdes.com', '+57 1 650 3322', 'Calle 100 # 8A-45', 'Bogotá', 'ACTIVO', '2026-01-18 11:00:00'),
(3, 'Finanzas Ágiles Global', '900.781.205-1', 'Financiero', 'soporte@finanzasagiles.co', '+57 4 310 9900', 'Circular 4 # 73-12, Laureles', 'Medellín', 'ACTIVO', '2026-02-05 13:45:00');

-- ####################################################################
-- TAREA INDIVIDUAL 3: MÓDULO DE USUARIOS & ACCESOS
-- ####################################################################
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    rol ENUM('ADMIN', 'EMPRESA', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    avatar_url VARCHAR(255),
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) 
        REFERENCES empresas(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contraseña para todos los usuarios semilla es: Admin123!
INSERT INTO usuarios (id, empresa_id, nombre, apellido, email, password_hash, telefono, rol, estado, created_at) VALUES
(1, NULL, 'Valeria', 'Castrillón', 'admin@sistema.com', '$2a$10$wE9K2jT3U7O4eXW1oV10e.6NfU5I8X1T1V7G8H9J0K1L2M3N4O5P6', '+57 300 123 4567', 'ADMIN', 'ACTIVO', '2026-01-15 09:30:00'),
(2, 1, 'Carlos', 'Mendoza', 'carlos.mendoza@empresa.com', '$2a$10$wE9K2jT3U7O4eXW1oV10e.6NfU5I8X1T1V7G8H9J0K1L2M3N4O5P6', '+57 312 456 7890', 'EMPRESA', 'ACTIVO', '2026-02-10 14:20:00'),
(3, NULL, 'Mariana', 'Gómez', 'mariana.gomez@tech.co', '$2a$10$wE9K2jT3U7O4eXW1oV10e.6NfU5I8X1T1V7G8H9J0K1L2M3N4O5P6', '+57 320 987 6543', 'USUARIO', 'ACTIVO', '2026-02-28 11:00:00'),
(4, NULL, 'Felipe', 'Restrepo', 'felipe.r@soluciones.com', '$2a$10$wE9K2jT3U7O4eXW1oV10e.6NfU5I8X1T1V7G8H9J0K1L2M3N4O5P6', '+57 301 555 1234', 'USUARIO', 'INACTIVO', '2026-03-05 16:45:00');

-- ####################################################################
-- TAREA INDIVIDUAL 4: MÓDULO DE RETOS & GAMIFICACIÓN (XP)
-- ####################################################################
CREATE TABLE retos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT NOT NULL,
    categoria_id INT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    dificultad ENUM('FACIL', 'MEDIA', 'DIFICIL') NOT NULL DEFAULT 'MEDIA',
    puntos INT NOT NULL,
    fecha_limite DATE NOT NULL,
    estado ENUM('ABIERTO', 'EN_PROGRESO', 'CERRADO', 'FINALIZADO') NOT NULL DEFAULT 'ABIERTO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reto_empresa FOREIGN KEY (empresa_id) 
        REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reto_categoria FOREIGN KEY (categoria_id) 
        REFERENCES categorias(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO retos (id, empresa_id, categoria_id, titulo, descripcion, dificultad, puntos, fecha_limite, estado, created_at) VALUES
(1, 1, 1, 'Optimización de Consumo Energético en Servidores', 'Diseñar un algoritmo de autoescalado para reducir el gasto eléctrico en centros de datos durante horas valle.', 'DIFICIL', 1500, '2026-12-15', 'ABIERTO', '2026-02-01 10:00:00'),
(2, 2, 2, 'Sistema de Trazabilidad de Residuos Reciclables', 'Aplicación móvil para registrar y verificar el pesaje y entrega de materiales reciclables en plantas de acopio.', 'MEDIA', 850, '2026-11-30', 'EN_PROGRESO', '2026-02-14 09:30:00'),
(3, 3, 3, 'Microcréditos con Evaluación de Riesgo Alternativo', 'Modelo predictivo para calificar capacidad de pago de microempresarios sin historial crediticio bancario formal.', 'MEDIA', 1200, '2026-10-25', 'ABIERTO', '2026-03-01 16:00:00');

-- ####################################################################
-- TAREA INDIVIDUAL 5: MÓDULO DE REGISTROS (INSCRIPCIONES / AUDITORÍAS)
-- ####################################################################
CREATE TABLE registros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    tipo ENUM('INSCRIPCION', 'EVALUACION', 'AUDITORIA', 'ENTREGA') NOT NULL DEFAULT 'INSCRIPCION',
    descripcion TEXT NOT NULL,
    usuario_id INT NOT NULL,
    empresa_id INT NULL,
    reto_id INT NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'COMPLETADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    observaciones TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_registro_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_registro_empresa FOREIGN KEY (empresa_id) 
        REFERENCES empresas(id) ON DELETE SET NULL,
    CONSTRAINT fk_registro_reto FOREIGN KEY (reto_id) 
        REFERENCES retos(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO registros (id, codigo, tipo, descripcion, usuario_id, empresa_id, reto_id, estado, observaciones, created_at) VALUES
(1, 'REG-2026-001', 'INSCRIPCION', 'Inscripción de equipo al reto de Optimización de Consumo Energético.', 3, 1, 1, 'APROBADO', 'Documentación técnica completa verificada por la empresa aliada.', '2026-02-05 14:30:00'),
(2, 'REG-2026-002', 'EVALUACION', 'Evaluación de propuesta técnica fase 1 del sistema de trazabilidad.', 2, 2, 2, 'PENDIENTE', 'En revisión por comité evaluador técnico.', '2026-02-20 10:15:00'),
(3, 'REG-2026-003', 'AUDITORIA', 'Verificación de políticas de cumplimiento y confidencialidad de datos.', 1, 3, 3, 'COMPLETADO', 'Auditoría aprobada sin no conformidades críticas.', '2026-03-02 16:40:00');

SET FOREIGN_KEY_CHECKS = 1;
