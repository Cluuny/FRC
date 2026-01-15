# RFC

Proyecto Spring Boot para reconciliación (RFC).

## Resumen

Microservicio que expone endpoints para reconciliar movimientos bancarios y generar reportes de conciliación. Usa PostgreSQL como base de datos.

## Requisitos

- Java 21 
- Maven
- Docker & Docker Compose (opcional, para levantar la base de datos en contenedor)

## Configuración

En la raíz del proyecto hay un archivo `.env` que contiene las variables de entorno usadas tanto por Docker Compose como por la aplicación.

Variables principales (ejemplo):

```dotenv
# Docker / Docker Compose
POSTGRES_DB=rfc_db
POSTGRES_USER=rfc_user
POSTGRES_PASSWORD=securepassword123

# URL JDBC para la aplicación Spring Boot
DATABASE_URL=jdbc:postgresql://localhost:5432/rfc_db
```

Notas:
- Si vas a ejecutar la aplicación desde otro contenedor en la misma red (por ejemplo, levantando la app también con Docker), es recomendable usar en `DATABASE_URL` el hostname del servicio de la base de datos, por ejemplo: `jdbc:postgresql://postgres:5432/rfc_db` (donde `postgres` es el nombre del servicio en `compose.yaml`).
- Docker Compose también carga automáticamente `.env` si existe en el mismo directorio.

## Uso

Levantar únicamente la base de datos con Docker Compose:

```bash
docker compose up -d
```

Ver logs del servicio postgres:

```bash
docker compose logs -f postgres
```

Ejecutar la aplicación localmente con Maven (usando las variables de entorno presentes en el sistema o en `.env`):

```bash
mvn spring-boot:run
```

Construir y ejecutar tests:

```bash
mvn -q test
```

## Tips de desarrollo

- Asegúrate de que las variables de entorno estén disponibles para la aplicación. Si ejecutas la app fuera de Docker, puedes exportarlas con `export $(cat .env | xargs)` en shells compatibles o usar herramientas como direnv.
- Si la aplicación no logra conectarse a la base de datos cuando ambos corren en Docker, prueba cambiar `DATABASE_URL` en `.env` a `jdbc:postgresql://postgres:5432/rfc_db`.

## Estructura relevante

- `compose.yaml` — definición del servicio Postgres que carga variables desde `.env`.
- `src/main/resources/application.properties` — configuración de conexión a la BD que usa variables de entorno (DATABASE_URL, POSTGRES_USER, POSTGRES_PASSWORD).

## Contribuciones

Abrir un PR con cambios claros y pruebas cuando aplique.

## Contacto

Para preguntas específicas sobre este repositorio, comenta un issue o contacta al mantenedor del proyecto.
