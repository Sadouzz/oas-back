# Facturation Backend API

This is the backend for the OAS Facturation system, built with Spring Boot and Java 21.

## Architecture & Technology Stack

- **Java Version:** 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL (with H2 for testing)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security with JWT Authentication
- **API Documentation:** Springdoc OpenAPI (Swagger UI)
- **PDF Generation:** OpenPDF
- **Monitoring:** Spring Boot Actuator & Prometheus (Micrometer)
- **Environment Management:** dotenv-java
- **Boilerplate Reduction:** Lombok

## Project Structure

The project is structured into features/domains under `src/main/java/sn/oas/facturation`:

- `auth` & `security`: Handles JWT generation, validation, and Spring Security configurations.
- `facturation`, `facture`, `devisPrevisionnel`, `proforma`: Core billing and estimation modules.
- `bonDeCommande`, `bonDeReception`, `bonDeSortie`: Order and delivery management.
- `client`, `fournisseur`: Stakeholder management.
- `vehicule`, `ficheAtelier`, `mecanicien`, `main_doeuvre`, `piecedetache`: Auto repair shop specific modules.
- `shared`, `config`: Global configurations, exceptions, and shared utilities.

## Configuration

The application uses `application.yml` and environment-specific files like `application-dev.yml`.

### Environment Variables (.env)
Copy `.env.example` to `.env` at the backend root and replace the placeholder values. The backend loads this file on startup. Main settings:
- PostgreSQL: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- Authentication: `JWT_SECRET` (use a long, randomly generated secret)
- Email: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- Media uploads: `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
- Browser access: `CORS_ORIGINS`; cookie options: `COOKIE_SECURE`, `COOKIE_SAME_SITE`
- Optional Web Push: `OAS_PUSH_VAPID_PUBLIC_KEY`, `OAS_PUSH_VAPID_PRIVATE_KEY`, `OAS_PUSH_VAPID_SUBJECT`

`DATABASE_HOSTNAME` remains supported as a legacy alias for the database username in the `dev` profile. The frontend API URL is configured in `oas-front/src/environments/environment.ts` and `environment.prod.ts`; Angular does not load the backend `.env` file.

### Profiles
The active profile is controlled via the `SPRING_ACTIVE_PROFILE` environment variable (defaults to `dev`).
In `dev` mode:
- DDL Auto is set to `update`.
- Server runs on port `9090`.
- CORS allows connections from `http://localhost:4200`, `http://localhost:5173`, `http://10.10.10.51`.

## Running the Application

1. **Prerequisites:** Ensure you have Java 21 installed and a running instance of PostgreSQL.
2. **Setup DB:** Create a database in PostgreSQL that matches your `DATABASE_URL`.
3. **Compile & Run:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Or build the jar and run:
   ```bash
   ./mvnw clean install
   java -jar target/facturation-0.0.1-SNAPSHOT.jar
   ```

## API Documentation

Once the application is running, access the Swagger UI for API testing and documentation at:
`http://localhost:9090/swagger-ui.html`
