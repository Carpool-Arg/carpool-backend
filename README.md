# 🚗 Carpool API - Backend

API REST desarrollada para una plataforma de carpooling, donde usuarios pueden organizar y compartir viajes en auto. La API permite gestionar usuarios, vehículos, solicitudes de viaje, emparejamientos entre conductores y pasajero, entre otras cosas.
---

## 🧾 Requisitos previos

Asegurate de tener instalado:

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Git](https://git-scm.com/)
- [Java](https://www.java.com/es/download/manual.jsp)
- [Maven](https://maven.apache.org/)

---

## 🚀 Cómo levantar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/Carpool-Arg/carpool-backend.git
cd carpool-backend
```

---

### 2. Crear archivo `.env` en la raíz del proyecto

Este archivo contendrá las variables de entorno necesarias para la base de datos y JWT:

```env
# Puerto del backend
SERVER_PORT

# Configuración de PostgreSQL
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
```

---

### 3. Crear el archivo `application.properties` en `src/main/resources/`

```properties
spring.application.name=carpool
spring.datasource.url=jdbc:postgresql://localhost:5433/carpool-db
spring.datasource.username=
spring.datasource.password=
server.servlet.context-path=/carpool/api/v1
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql= true
```

> ✅ Este archivo no viene en el repositorio. Debés crearlo manualmente con esta configuración.

---

### 4. Levantar el proyecto con Docker Compose

```bash
docker-compose up --build
```

Esto compilará el backend y levantará la API junto con una base de datos PostgreSQL.

---

### 5. Acceder a la aplicación

- API base: [http://localhost:8080/carpool/api/v1](http://localhost:8080/carpool/api/v1)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### 6. Apagar los contenedores

```bash
docker-compose down
```

---

## 📂 Estructura del proyecto

```
carpool-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tuempresa/carpool/
│   │   └── resources/
│   │       └── application.properties  <-- debe crearse
├── .env                                <-- debe crearse
├── docker-compose.yml
├── Dockerfile
└── README.md
```

---

## 🔒 Autenticación

La autenticación se realiza mediante JWT. Para consumir los endpoints protegidos, se debe enviar el token en el encabezado:

```
Authorization: Bearer <tu_token_jwt>
```

---

## 🧪 Pruebas

Podés probar los endpoints usando Postman, Insomnia o Swagger UI.

---
