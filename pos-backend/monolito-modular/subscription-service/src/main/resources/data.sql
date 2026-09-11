-- Insertar planes
INSERT IGNORE INTO planes (id, nombre, descripcion, precio, max_perfiles, activo) VALUES
(1, 'Básico',       'Plan básico para pequeños negocios',   89900,  2,   true),
(2, 'Profesional',  'Plan completo para negocios medianos',  189900, 10,  true),
(3, 'Empresarial',  'Plan ilimitado para grandes empresas',  349900, 999, true);

-- Plan Básico: solo ventas, inventario y clientes
INSERT IGNORE INTO plan_features (plan_id, modulo) VALUES
(1, 'VENTAS'),
(1, 'INVENTARIO'),
(1, 'CLIENTES');

-- Plan Profesional: agrega contabilidad, facturación y proveedores
INSERT IGNORE INTO plan_features (plan_id, modulo) VALUES
(2, 'VENTAS'),
(2, 'INVENTARIO'),
(2, 'CLIENTES'),
(2, 'PROVEEDORES'),
(2, 'CONTABILIDAD'),
(2, 'FACTURACION');

-- Plan Empresarial: acceso total
INSERT IGNORE INTO plan_features (plan_id, modulo) VALUES
(3, 'VENTAS'),
(3, 'INVENTARIO'),
(3, 'CLIENTES'),
(3, 'PROVEEDORES'),
(3, 'CONTABILIDAD'),
(3, 'FACTURACION'),
(3, 'NOMINA');