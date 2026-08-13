IF DB_ID('ProveedorTelefonicoDB') IS NULL
    CREATE DATABASE ProveedorTelefonicoDB;
GO

USE ProveedorTelefonicoDB;
GO

IF OBJECT_ID('dbo.clientes', 'U') IS NULL
BEGIN
    CREATE TABLE clientes (
        id INT IDENTITY(1,1) PRIMARY KEY,
        telefono VARCHAR(20) NOT NULL UNIQUE,
        tipo_servicio VARCHAR(20) NOT NULL
            CONSTRAINT CK_clientes_tipo_servicio
            CHECK (tipo_servicio IN ('PREPAGO', 'POSTPAGO')),
        saldo DECIMAL(10,2) NOT NULL DEFAULT 0
            CONSTRAINT CK_clientes_saldo CHECK (saldo >= 0),
        bono_mismo_proveedor DECIMAL(10,2) NOT NULL DEFAULT 0
            CONSTRAINT CK_clientes_bono CHECK (bono_mismo_proveedor >= 0),
        activo BIT NOT NULL DEFAULT 1
    );
END;

IF COL_LENGTH('dbo.clientes', 'bono_mismo_proveedor') IS NULL
    ALTER TABLE clientes
        ADD bono_mismo_proveedor DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_clientes_bono DEFAULT 0;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_clientes_tipo_servicio')
    ALTER TABLE clientes WITH CHECK
        ADD CONSTRAINT CK_clientes_tipo_servicio
        CHECK (tipo_servicio IN ('PREPAGO', 'POSTPAGO'));

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_clientes_saldo')
    ALTER TABLE clientes WITH CHECK
        ADD CONSTRAINT CK_clientes_saldo CHECK (saldo >= 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_clientes_bono')
    ALTER TABLE clientes WITH CHECK
        ADD CONSTRAINT CK_clientes_bono CHECK (bono_mismo_proveedor >= 0);
GO

IF OBJECT_ID('dbo.tarifas', 'U') IS NULL
BEGIN
    CREATE TABLE tarifas (
        id INT IDENTITY(1,1) PRIMARY KEY,
        tipo_llamada VARCHAR(30) NOT NULL UNIQUE,
        costo_minuto DECIMAL(10,2) NOT NULL
            CONSTRAINT CK_tarifas_costo CHECK (costo_minuto > 0)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_tarifas_costo')
    ALTER TABLE tarifas WITH CHECK
        ADD CONSTRAINT CK_tarifas_costo CHECK (costo_minuto > 0);

IF OBJECT_ID('dbo.movimientos', 'U') IS NULL
BEGIN
    CREATE TABLE movimientos (
        id INT IDENTITY(1,1) PRIMARY KEY,
        telefono VARCHAR(20) NOT NULL,
        fecha_llamada DATETIME NOT NULL,
        telefono_destino VARCHAR(20) NOT NULL,
        costo DECIMAL(10,2) NOT NULL
            CONSTRAINT CK_movimientos_costo CHECK (costo >= 0),
        duracion VARCHAR(6) NOT NULL
            CONSTRAINT CK_movimientos_duracion
            CHECK (LEN(duracion) = 6 AND duracion NOT LIKE '%[^0-9]%'),
        CONSTRAINT FK_movimientos_clientes
            FOREIGN KEY (telefono) REFERENCES clientes(telefono)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_movimientos_costo')
    ALTER TABLE movimientos WITH CHECK
        ADD CONSTRAINT CK_movimientos_costo CHECK (costo >= 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_movimientos_duracion')
    ALTER TABLE movimientos WITH CHECK
        ADD CONSTRAINT CK_movimientos_duracion
        CHECK (LEN(duracion) = 6 AND duracion NOT LIKE '%[^0-9]%');

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_movimientos_clientes')
    ALTER TABLE movimientos WITH CHECK
        ADD CONSTRAINT FK_movimientos_clientes
        FOREIGN KEY (telefono) REFERENCES clientes(telefono);
GO

MERGE clientes AS destino
USING (VALUES
    ('25743715', 'PREPAGO', 5000.00, 500.00, 1),
    ('25262020', 'PREPAGO', 2500.00, 250.00, 1),
    ('22334455', 'PREPAGO', 1000.00, 0.00, 1),
    ('88887777', 'PREPAGO', 1000.00, 100.00, 1),
    ('88886666', 'POSTPAGO', 0.00, 0.00, 1),
    ('89154242', 'POSTPAGO', 0.00, 0.00, 1)
) AS origen(telefono, tipo_servicio, saldo, bono_mismo_proveedor, activo)
ON destino.telefono = origen.telefono
WHEN NOT MATCHED THEN
    INSERT (telefono, tipo_servicio, saldo, bono_mismo_proveedor, activo)
    VALUES (origen.telefono, origen.tipo_servicio, origen.saldo,
            origen.bono_mismo_proveedor, origen.activo)
WHEN MATCHED AND destino.bono_mismo_proveedor = 0 THEN
    UPDATE SET bono_mismo_proveedor = origen.bono_mismo_proveedor;

MERGE tarifas AS destino
USING (VALUES
    ('MISMO_PROVEEDOR', 25.13),
    ('OTRO_PROVEEDOR', 25.13),
    ('INTERNACIONAL', 1.03),
    ('NACIONAL_FIJO', 8.72),
    ('NACIONAL_MOVIL', 25.13),
    ('INTERNACIONAL_C1', 0.14),
    ('INTERNACIONAL_B', 0.31),
    ('INTERNACIONAL_D', 0.54),
    ('INTERNACIONAL_RESTO', 1.03)
) AS origen(tipo_llamada, costo_minuto)
ON destino.tipo_llamada = origen.tipo_llamada
WHEN MATCHED THEN
    UPDATE SET costo_minuto = origen.costo_minuto
WHEN NOT MATCHED THEN
    INSERT (tipo_llamada, costo_minuto)
    VALUES (origen.tipo_llamada, origen.costo_minuto);
GO

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'javauser')
    CREATE LOGIN javauser WITH PASSWORD = 'Java123456';
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'javauser')
    CREATE USER javauser FOR LOGIN javauser;
GO

ALTER ROLE db_datareader ADD MEMBER javauser;
ALTER ROLE db_datawriter ADD MEMBER javauser;
GO
