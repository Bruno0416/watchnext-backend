# Noticias, Swagger, migraciones y perfil al registrarse

## 1. news-service (nuevo, puerto 8088)
- `GET /api/v1/news`, `GET /api/v1/news/{id}` y `GET /api/v1/news/slug/{slug}` son públicos.
- `POST`, `PUT` y `DELETE /api/v1/news...` son solo para ADMIN (`@PreAuthorize`).
- Responde con el mismo formato que el mock del front: `id, slug, title, summary, content, imageUrl, publishedAt`.
- Ya está en el gateway (`RateLimitedRoutesConfig`), en `docker-compose.yaml` y como módulo en el `pom.xml` raíz.
- **En Neon:** crear la base `news_db` y agregar `NEWS_DB_URL`, `NEWS_DB_USER` y `NEWS_DB_PASSWORD` al `.env`.

## 2. Migraciones (Flyway)
- `db/migration/V1__esquema_inicial.sql` en auth, user, list, feedback y news. Es el mismo esquema que antes creaba `ddl-auto: update`.
- `baseline-on-migrate`: en las bases de Neon que ya tienen tablas, la V1 se marca como hecha y no se corre. En una base vacía se crea todo.
- `ddl-auto` pasó a `validate`: Hibernate ya no cambia tablas, solo revisa que calcen.
- news trae una `V2` con las 2 noticias del mock.
- En list-service, `jpa` estaba dentro de `content-service:` en el YAML, así que Spring no lo leía. Quedó corregido.

## 3. Swagger
- Cada servicio publica su OpenAPI en `/api/v1/<servicio>/docs`.
- El gateway las junta todas en **http://localhost:8080/swagger-ui.html**, con un selector arriba.
- Los "Try it out" van por el gateway, y el botón **Authorize** recibe el token del login.

## 4. Perfil al registrarse (auth + user)
- Al registrarse (o al entrar con Google por primera vez), auth-service llama a `POST /api/v1/users/internal/provision`. Va con token de servicio, y user-service crea un perfil privado con un username temporal (`user_xxxxxxxxxx`) y `onboardingCompleted=false`.
- El onboarding ahora completa ese perfil en vez de crear uno nuevo. Si user-service no responde al registrar, el onboarding lo crea igual.
- Se corrigió además que `/users/me` y el detalle de una lista daban 500 cuando no había favoritos o ítems (content-service rechaza el bulk vacío).

## 5. Panel admin de usuarios (auth)
- `GET /api/v1/auth/admin/users?q=` lista las cuentas con su perfil, `GET /api/v1/auth/admin/users/{id}` trae una, `POST /api/v1/auth/admin/users` crea una cuenta ya verificada (`email`, `password` de 8 a 60, `role`), `PUT /api/v1/auth/admin/users/{id}/identity` cambia `username` y `displayName`, y `DELETE /api/v1/auth/admin/users/{id}` la borra.
- Solo con token de rol ADMIN: auth-service valida el JWT en `/api/v1/auth/admin/**` (el resto de auth sigue público) y el gateway también pide el token ahí.
- El gateway le da a `/api/v1/auth/admin/**` su propia ruta con límite de 60 por minuto, para no caer en el límite de 5 del login.
- Auth junta cada cuenta con su perfil por rutas internas nuevas de user-service (`/api/v1/users/internal/admin/...`).
- Al borrar una cuenta se borran su perfil, sus seguidores (descontando los contadores de los otros), sus listas y sus reseñas, con rutas internas nuevas en list-service y feedback-service. No deja borrarse a uno mismo (400) ni al último admin (409).

## 6. Seguidores de cualquier perfil (user)
- `GET /api/v1/users/{username}/followers` y `/following`: paginados. Si el perfil es privado, solo el dueño o un seguidor aceptado los ven; el resto recibe 403.
- `GET /api/v1/users/{username}/relationship`: devuelve `following`, `requested` (solicitud pendiente) y `followsYou`.

## Variables nuevas en `.env`
`NEWS_DB_URL`, `NEWS_DB_USER`, `NEWS_DB_PASSWORD`; `USER_SERVICE_INTERNAL_URL` es opcional (por defecto `http://user-service:8085/api/v1/users/internal`). También son opcionales `LIST_SERVICE_INTERNAL_URL` y `FEEDBACK_SERVICE_INTERNAL_URL` (por defecto `list-service:8083` y `feedback-service:8084`), solo hacen falta fuera de Docker.
