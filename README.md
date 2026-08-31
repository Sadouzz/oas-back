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
You must set the following environment variables (or define them in a `.env` file at the root of the project):
- `DATABASE_URL`: PostgreSQL connection string (e.g., `jdbc:postgresql://localhost:5432/facturation`)
- `DATABASE_HOSTNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `JWT_SECRET`: Secret key used for signing JWT tokens

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
