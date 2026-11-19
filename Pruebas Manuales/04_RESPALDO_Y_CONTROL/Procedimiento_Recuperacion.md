# Procedimiento de Recuperación y Respaldo de Base de Datos

## 1. Identificación del Respaldo
- **Base de datos:** `ficct_sistemas_db`
- **Ubicación del archivo de respaldo:** `C:\Users\vajho\Desktop\SistemaFICCT\storage\backups\ficct_sistemas_db_pre_poblacion_20261119.sql`
- **Fecha y Hora de Creación:** 19 de noviembre de 2026, 14:02:44
- **Tamaño verificado:** 130,439 bytes (130 KB)
- **Tipo de volcado:** SQL plano transaccional generado con `pg_dump` de PostgreSQL 16.

---

## 2. Procedimiento de Restauración Paso a Paso

En caso de requerir volver al estado previo a la población inicial:

1. **Detener temporalmente el backend Spring Boot** (para cerrar conexiones activas a PostgreSQL):
   ```cmd
   C:\Users\vajho\Desktop\SistemaFICCT\detener_sistema.bat
   ```

2. **Ejecutar la restauración en PowerShell:**
   ```powershell
   $env:PGPASSWORD = '123456'
   # Terminar conexiones residuales
   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'ficct_sistemas_db' AND pid <> pg_backend_pid();"
   
   # Eliminar y recrear la base de datos limpia
   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d postgres -c "DROP DATABASE ficct_sistemas_db;"
   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d postgres -c "CREATE DATABASE ficct_sistemas_db;"
   
   # Restaurar volcado SQL íntegro
   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d ficct_sistemas_db -f "C:\Users\vajho\Desktop\SistemaFICCT\storage\backups\ficct_sistemas_db_pre_poblacion_20261119.sql"
   ```

3. **Verificar el restablecimiento:**
   ```powershell
   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d ficct_sistemas_db -c "SELECT count(*) FROM usuarios; SELECT count(*) FROM correspondencia;"
   ```

4. **Reiniciar SistemaFICCT:**
   ```cmd
   C:\Users\vajho\Desktop\SistemaFICCT\iniciar_sistema.bat
   ```

