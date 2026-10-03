Scripts que MySQL ejecuta al crear el volumen por primera vez (`/docker-entrypoint-initdb.d`). Si cambias algo aquí, hay que recrear el volumen: `make dev-reset`.

Las tablas `chinesereads.user` y `user_roles` no se crean aquí: las crea el backend del proyecto matriz al arrancar (Hibernate `ddl-auto=update`). Los GRANT sobre tablas que aún no existen son válidos en MySQL 8.
