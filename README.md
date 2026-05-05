# SGDEA - Guia de Arquitectura

## 1. Proposito

SGDEA es una arquitectura basada en microservicios Spring Boot para gestionar modulos independientes del sistema, como empresas y usuarios, exponiendo APIs REST y GraphQL.

El objetivo principal es separar responsabilidades por dominio, mantener una estructura de paquetes consistente y permitir integracion entre servicios mediante un GraphQL Gateway.

## 2. Tecnologias Principales

| Tecnologia | Uso |
| --- | --- |
| Java 17 | Lenguaje base |
| Spring Boot 3.5.14 | Framework principal |
| Spring Web | APIs REST |
| Spring Data JPA | Persistencia |
| PostgreSQL | Base de datos |
| Spring Security | Seguridad HTTP |
| Spring Validation | Validacion de DTOs |
| Spring GraphQL | API GraphQL |
| SpringDoc OpenAPI | Swagger para REST |
| Lombok | Reduccion de boilerplate |
| Maven | Gestion de dependencias y build |
| JWT | Base para autenticacion futura |
| MinIO | Base para almacenamiento futuro de archivos |
| MapStruct | Base para mapeos futuros |

## 3. Arquitectura General

```text
Cliente / Frontend / Postman
        |
        | REST
        v
Microservicio especifico
        |
        v
Service -> Repository -> PostgreSQL
```

```text
Cliente / Frontend / Postman
        |
        | GraphQL
        v
Gateway :9080
        |
        +-- sgdea :9090/graphql
        |
        +-- users :9091/graphql
```

## 4. Estructura De Paquetes

Cada microservicio de dominio debe seguir esta estructura:

```text
com.api.<servicio>
├── config
├── <modulo>
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── graphql
│   ├── mapper
│   ├── repository
│   └── service
└── <Servicio>Application.java
```

Ejemplo en `sgdea`:

```text
com.api.sgdea
├── config
├── sgdea
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── graphql
│   ├── mapper
│   ├── repository
│   └── service
└── SgdeaApplication.java
```

Ejemplo en `users`:

```text
com.api.users
├── config
├── users
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── mapper
│   ├── repository
│   └── service
├── graphql
└── UsersApplication.java
```

## 5. Responsabilidad De Cada Capa

### Entity

Representa la tabla de base de datos.

```text
entity/Company.java
entity/User.java
```

Reglas:

- Usa anotaciones JPA como `@Entity`, `@Table`, `@Id`.
- No debe contener logica de negocio compleja.
- Puede tener callbacks como `@PrePersist` y `@PreUpdate`.

### DTO

Define los datos que entran y salen por la API.

```text
CompanyCreateDto
CompanyUpdateDto
CompanyResponseDto
```

Reglas:

- No exponer entidades directamente.
- Usar validaciones como `@NotBlank`, `@Size`, `@Email`.
- Separar DTOs de creacion, actualizacion y respuesta.

### Repository

Acceso a base de datos.

```java
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByNit(String nit);
}
```

Reglas:

- Extender `JpaRepository`.
- Definir consultas simples por convencion de nombres.
- Evitar logica de negocio.

### Mapper

Convierte entre Entity y DTO.

```text
mapper/CompanyMapper.java
mapper/UserMapper.java
```

Reglas:

- Centralizar conversiones.
- Evitar mapeos manuales repetidos en service o controller.

### Service

Contiene la logica de negocio.

Responsabilidades:

- Validar reglas de negocio.
- Consultar repositorios.
- Lanzar excepciones controladas.
- Coordinar mapper y repository.

Ejemplo:

```java
if (companyRepository.existsByNit(createDto.getNit())) {
    throw new BusinessException("Ya existe una empresa con el NIT " + createDto.getNit());
}
```

### Controller REST

Expone endpoints HTTP tradicionales.

Ejemplo:

```text
GET    /administrador/companies/all
POST   /administrador/companies
PUT    /administrador/companies/{id}
PATCH  /administrador/companies/{id}/status
DELETE /administrador/companies/{id}
```

Reglas:

- No contener logica de negocio.
- Delegar siempre al service.
- Documentar con Swagger/OpenAPI.

### GraphQL Schema

Define el contrato GraphQL.

Ubicacion:

```text
src/main/resources/graphql/*.graphqls
```

Ejemplo:

```graphql
type Company {
  id: ID!
  name: String!
  nit: String!
  active: Boolean!
}

type Query {
  companies: [Company!]!
  companyById(id: ID!): Company!
}

type Mutation {
  createCompany(input: CompanyCreateInput!): Company!
}
```

### GraphQL Controller

Conecta el schema con el service.

```java
@QueryMapping
public List<CompanyResponseDto> companies() {
    return companyService.findAll();
}

@MutationMapping
public CompanyResponseDto createCompany(@Argument("input") CompanyCreateDto createDto) {
    return companyService.create(createDto);
}
```

### GraphQL Exception Resolver

Permite devolver errores controlados en GraphQL.

Ejemplo de respuesta esperada:

```json
{
  "errors": [
    {
      "message": "Ya existe una empresa con el NIT 900111222",
      "extensions": {
        "status": 409,
        "code": "BUSINESS_ERROR"
      }
    }
  ],
  "data": null
}
```

### Security Config

Configuracion actual para desarrollo:

- CSRF desactivado.
- Swagger permitido.
- GraphQL permitido.
- Endpoints REST del modulo permitidos.

Ejemplo:

```java
.csrf(csrf -> csrf.disable())
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/graphql", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated()
)
```

## 7. REST Vs GraphQL

| Aspecto | REST | GraphQL |
| --- | --- | --- |
| Uso principal | CRUD tradicional | Consultas flexibles |
| Documentacion | Swagger | Schema `.graphqls` |
| Endpoint | Varios endpoints | Un endpoint `/graphql` |
| Campos devueltos | Fijos por DTO | El cliente decide |
| Mutaciones | POST, PUT, PATCH, DELETE | `mutation` |
| Consultas | GET | `query` |

## 8. Gateway GraphQL

El gateway permite consultar varios microservicios desde una sola API GraphQL.

```text
gateway :9080
├── consulta sgdea :9090/graphql
└── consulta users :9091/graphql
```

Ejemplo:

```graphql
query {
  companyById(id: 3) {
    id
    name
    nit
    users {
      id
      firstName
      email
      role
    }
  }
}
```

En este ejemplo:

- `companyById` se consulta en `sgdea`.
- `users` se resuelve consultando `usersByCompanyId` en el servicio `users`.

## 9. Relacion Entre Empresas Y Usuarios

La relacion entre servicios se hace por identificador, no por llave foranea directa entre bases de datos.

```text
companies.id      -> users.companyId
```

Esto mantiene los microservicios desacoplados.

## 10. Comandos De Ejecucion

### Ejecutar sgdea

```powershell
cd C:\Users\Jeisson Guabave\Documents\sgdea
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

### Ejecutar users

```powershell
cd C:\Users\Jeisson Guabave\Documents\sgdea\users
mvn spring-boot:run
```

### Ejecutar gateway

```powershell
cd C:\Users\Jeisson Guabave\Documents\sgdea\gateway
mvn spring-boot:run
```

## 11. URLs Principales

| Servicio | URL |
| --- | --- |
| Swagger sgdea | `http://localhost:9090/swagger-ui.html` |
| GraphQL sgdea | `http://localhost:9090/graphql` |
| Swagger users | `http://localhost:9091/swagger-ui.html` |
| GraphQL users | `http://localhost:9091/graphql` |
| GraphQL gateway | `http://localhost:9080/graphql` |

## 12. Ejemplos De Prueba GraphQL

### Crear Empresa

```json
{
  "query": "mutation { createCompany(input: { name: \"Empresa Demo\", nit: \"900777001\", address: \"Calle 100\", phone: \"3001112233\", email: \"empresa.demo@test.com\", active: true }) { id name nit email active } }"
}
```

### Crear Usuario Asociado A Empresa

```json
{
  "query": "mutation { createUser(input: { firstName: \"Carlos\", lastName: \"Perez\", email: \"carlos.demo@test.com\", username: \"carlos.demo\", phone: \"3002223344\", companyId: 1, role: \"ADMIN\", active: true }) { id firstName email companyId role active } }"
}
```

### Consultar Desde Gateway

```json
{
  "query": "query { companyById(id: 1) { id name nit users { id firstName email role companyId } } }"
}
```

## 13. Paso A Paso Para Crear Un Nuevo Modulo

1. Crear la entidad JPA en `entity`.
2. Crear DTOs: `CreateDto`, `UpdateDto`, `ResponseDto`.
3. Crear repository extendiendo `JpaRepository`.
4. Crear mapper.
5. Crear excepciones si aplica.
6. Crear service con reglas de negocio.
7. Crear REST controller.
8. Probar en Swagger.
9. Crear schema GraphQL `.graphqls`.
10. Crear GraphQL controller.
11. Crear GraphQL exception resolver si el modulo requiere errores personalizados.
12. Si el modulo debe integrarse con otros servicios, agregarlo al gateway.

## 14. Convenciones Recomendadas

### Nombres

```text
CompanyCreateDto
CompanyUpdateDto
CompanyResponseDto
CompanyRepository
CompanyMapper
CompanyService
CompanyController
CompanyGraphqlController
```

### Endpoints REST

```text
/administrador/<modulo>
/administrador/<modulo>/all
/administrador/<modulo>/paginated
/administrador/<modulo>/{id}
/administrador/<modulo>/{id}/status
```

### GraphQL

```text
Query    -> consultar datos
Mutation -> modificar datos
```

Ejemplo:

```graphql
type Query {
  companies: [Company!]!
}

type Mutation {
  createCompany(input: CompanyCreateInput!): Company!
}
```

## 15. Recomendaciones Para Produccion

La configuracion actual es adecuada para desarrollo. Antes de produccion se recomienda:

- No dejar contrasenas en `application.yaml`.
- Usar variables de entorno.
- Activar autenticacion real con JWT.
- Proteger `/graphql`.
- Proteger Swagger o desactivarlo.
- Usar migraciones con Flyway o Liquibase.
- Separar `Service` en interfaz e implementacion.
- Usar MapStruct para mapeos grandes.
- Agregar pruebas unitarias y de integracion.
- Agregar logs estructurados.
- Agregar observabilidad con Actuator, metrics y tracing.

