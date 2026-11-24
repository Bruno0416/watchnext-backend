# Noticias, Swagger, migraciones, base MySQL y perfil al registrarse

## 1. news-service (nuevo, puerto 8088)
- `GET /api/v1/news`, `GET /api/v1/news/{id}` y `GET /api/v1/news/slug/{slug}` son públicos.
- `POST`, `PUT` y `DELETE /api/v1/news...` son solo para ADMIN (`@PreAuthorize`).
- Responde con el mismo formato que usa el front: `id, slug, title, summary, content, imageUrl, publishedAt`.
- Ya está en el gateway (`RateLimitedRoutesConfig`), en `docker-compose.yaml` y como módulo en el `pom.xml` raíz.

## 2. Base de datos MySQL en Docker (igual que PagoFlex)
- `docker-compose.yaml` levanta `mysql` (MySQL 8.4) y `db-init`. `db-init` corre `db-init/init.sh`, que crea las bases `auth_db`, `user_db`, `list_db`, `feedback_db` y `news_db`, y el usuario `watchnext` con permisos sobre ellas. Se puede correr muchas veces sin borrar nada.
- Los servicios con base (auth, user, list, feedback, news) esperan a que `db-init` termine antes de arrancar.
- Driver `mysql-connector-j` y `flyway-mysql` en vez de los de PostgreSQL; dialecto `MySQLDialect`.
- **Ver las tablas desde Laragon / HeidiSQL:** host `127.0.0.1`, puerto **3307** (el 3306 queda para el MySQL de Laragon), usuario `root` / clave `root`, o usuario `watchnext` / clave `watchnext`.
- Los datos quedan en el volumen `mysql-data`. Para partir con la base vacía y ver cómo se crean las tablas: `docker compose down -v` y `docker compose up`.
- Ya no se usa Neon. Las variables `AUTH_DB_URL`, `USER_DB_URL`, etc. del `.env` quedan sin uso.

## 3. Migraciones (Flyway)
- `db/migration/V1__esquema_inicial.sql` en auth, user, list, feedback y news, en sintaxis MySQL. Es el mismo esquema que Hibernate genera con las entidades.
- Al arrancar, cada servicio corre sus migraciones y crea las tablas; en el log sale `Migrating schema ... to version "1 - esquema inicial"`. Flyway guarda lo que corrió en la tabla `flyway_schema_history`.
- `ddl-auto` pasó a `validate`: Hibernate ya no cambia tablas, solo revisa que calcen.
- news trae una `V2` con 2 noticias de ejemplo.
- En list-service, `jpa` estaba dentro de `content-service:` en el YAML, así que Spring no lo leía. Quedó corregido.

## 4. Swagger
- Cada servicio publica su OpenAPI en `/api/v1/<servicio>/docs`.
- El gateway las junta todas en **http://localhost:8080/swagger-ui.html**, con un selector arriba.
- Los "Try it out" van por el gateway, y el botón **Authorize** recibe el token del login.

## 5. Perfil al registrarse (auth + user)
- Al registrarse (o al entrar con Google por primera vez), auth-service llama a `POST /api/v1/users/internal/provision`. Va con token de servicio, y user-service crea un perfil privado con un username temporal (`user_xxxxxxxxxx`) y `onboardingCompleted=false`.
- El onboarding ahora completa ese perfil en vez de crear uno nuevo. Si user-service no responde al registrar, el onboarding lo crea igual.
- Se corrigió además que `/users/me` y el detalle de una lista daban 500 cuando no había favoritos o ítems (content-service rechaza el bulk vacío).

## 6. Panel admin de usuarios (auth)
- `GET /api/v1/auth/admin/users?q=` lista las cuentas con su perfil, `GET /api/v1/auth/admin/users/{id}` trae una, `POST /api/v1/auth/admin/users` crea una cuenta ya verificada (`email`, `password` de 8 a 60, `role`), `PUT /api/v1/auth/admin/users/{id}/identity` cambia `username` y `displayName`, y `DELETE /api/v1/auth/admin/users/{id}` la borra.
- Solo con token de rol ADMIN: auth-service valida el JWT en `/api/v1/auth/admin/**` (el resto de auth sigue público) y el gateway también pide el token ahí.
- El gateway le da a `/api/v1/auth/admin/**` su propia ruta con límite de 60 por minuto, para no caer en el límite de 5 del login.
- Auth junta cada cuenta con su perfil por rutas internas nuevas de user-service (`/api/v1/users/internal/admin/...`).
- Al borrar una cuenta se borran su perfil, sus seguidores (descontando los contadores de los otros), sus listas y sus reseñas, con rutas internas nuevas en list-service y feedback-service. No deja borrarse a uno mismo (400) ni al último admin (409).

## 7. Seguidores de cualquier perfil (user)
- `GET /api/v1/users/{username}/followers` y `/following`: paginados. Si el perfil es privado, solo el dueño o un seguidor aceptado los ven; el resto recibe 403.
- `GET /api/v1/users/{username}/relationship`: devuelve `following`, `requested` (solicitud pendiente) y `followsYou`.

## Variables (todas opcionales)
- Base: `WATCHNEXT_DB_USER` y `WATCHNEXT_DB_PASSWORD` (por defecto `watchnext`), `WATCHNEXT_DB_ROOT_PASSWORD` (por defecto `root`). Llevan el prefijo `WATCHNEXT_` para no chocar con un `DB_USER` que ya exista en el equipo. Fuera de Docker los servicios se conectan a `localhost:3307` (`WATCHNEXT_DB_HOST` y `WATCHNEXT_DB_PORT`).
- Rutas internas: `USER_SERVICE_INTERNAL_URL`, `LIST_SERVICE_INTERNAL_URL` y `FEEDBACK_SERVICE_INTERNAL_URL`, solo hacen falta fuera de Docker.
