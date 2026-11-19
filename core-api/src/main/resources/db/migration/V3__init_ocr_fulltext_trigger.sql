-- V3: Trigger nativo de búsqueda de texto completo con Soberanía Humana
-- Solo cuando texto_corregido o la validación humana se registra, se genera el tsvector para búsqueda.
-- Si texto_corregido es nulo (pendiente de validación), vector_busqueda permanece NULL.

CREATE OR REPLACE FUNCTION trg_actualizar_vector_busqueda_ocr()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.texto_corregido IS NOT NULL AND TRIM(NEW.texto_corregido) <> '' THEN
        NEW.vector_busqueda := to_tsvector('spanish', NEW.texto_corregido);
    ELSE
        NEW.vector_busqueda := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_documento_paginas_ocr_vector ON documento_paginas_ocr;

CREATE TRIGGER trg_documento_paginas_ocr_vector
BEFORE INSERT OR UPDATE OF texto_corregido ON documento_paginas_ocr
FOR EACH ROW
EXECUTE FUNCTION trg_actualizar_vector_busqueda_ocr();

