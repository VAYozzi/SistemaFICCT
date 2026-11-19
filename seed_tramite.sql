INSERT INTO expedientes_estudiantiles (registro_estudiantil, cedula_identidad, nombres, apellidos, carrera, correo, telefono, estado_academico)
VALUES ('218000001', '8372619', 'Juan Carlos', 'Perez Rodriguez', 'Ingenieria en Sistemas', 'juan.perez@uagrm.edu.bo', '71234567', 'REGULAR')
ON CONFLICT (registro_estudiantil) DO NOTHING;

INSERT INTO tipos_tramites (codigo, nombre, descripcion, dias_limite_atencion, activo)
VALUES ('TR-CONV', 'Convalidacion de Materias', 'Tramite oficial de traspaso y homologacion de materias cursadas', 5, true)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tramites_estudiantiles (codigo_tramite, expediente_id, tipo_tramite_id, estado_actual)
SELECT 'TR-2026-0001', e.id, t.id, 'INGRESADO'
FROM expedientes_estudiantiles e, tipos_tramites t
WHERE e.registro_estudiantil = '218000001' AND t.codigo = 'TR-CONV'
ON CONFLICT (codigo_tramite) DO NOTHING;
