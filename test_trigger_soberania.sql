-- Limpiar prueba previa
DELETE FROM documento_paginas_ocr WHERE documento_id = 1;

-- 1. Inserción cruda de OCR (Pendiente de validación, texto_corregido es NULL)
INSERT INTO documento_paginas_ocr (documento_id, numero_pagina, texto_extraido_bruto, confianza_pagina)
VALUES (1, 1, 'UNIVERSIDAD AUTONOMA GABRIEL RENE MORENO VICERRECTORADO', 91.50);

-- 2. Comprobar que NO aparece en búsquedas (vector_busqueda es NULL)
SELECT 'BUSQUEDA ANTES DE VALIDAR' AS etapa, COUNT(*) AS resultados
FROM documento_paginas_ocr 
WHERE vector_busqueda @@ to_tsquery('spanish', 'GABRIEL');

-- 3. Funcionario humano valida o corrige el texto
UPDATE documento_paginas_ocr 
SET texto_corregido = 'UNIVERSIDAD AUTONOMA GABRIEL RENE MORENO VICERRECTORADO OFICIAL'
WHERE documento_id = 1 AND numero_pagina = 1;

-- 4. Comprobar que AHORA SÍ aparece en búsquedas con resaltado
SELECT 'BUSQUEDA DESPUES DE VALIDAR' AS etapa, COUNT(*) AS resultados
FROM documento_paginas_ocr 
WHERE vector_busqueda @@ to_tsquery('spanish', 'GABRIEL');

SELECT id, documento_id, ts_headline('spanish', texto_corregido, to_tsquery('spanish', 'GABRIEL'), 'StartSel=<mark>, StopSel=</mark>') AS fragmento
FROM documento_paginas_ocr 
WHERE vector_busqueda @@ to_tsquery('spanish', 'GABRIEL');

