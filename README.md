# ☕ Backend en Java (Spring Boot 3 + MySQL)
## Proyecto Integrador: CESDE Gamified Challenges

API REST modular y orientada a micro-módulos desarrollada en **Java 17+ con Spring Boot 3, Spring Data JPA, Spring Security, JWT y MySQL**.

---

## 📁 Estructura del Proyecto (Java Maven)

```
backend/
├── pom.xml                                       # Configuración Maven (Spring Boot 3, MySQL, JPA, JWT, Security)
├── README.md                                     # Guía completa de ejecución
├── database/
│   └── schema.sql                                # Script SQL con la base de datos y datos iniciales
└── src/
    └── main/
        ├── resources/
        │   └── application.properties            # Configuración de puerto 3000, MySQL y JWT
        └── java/
            └── com/cesde/proyectointegrador/
                ├── ProyectoIntegradorApplication.java  # Clase principal de arranque
                ├── config/
                │   └── SecurityConfig.java       # Configuración de Spring Security, BCrypt y CORS
                ├── security/
                │   ├── JwtTokenProvider.java     # Generación y validación de tokens JWT
                │   └── JwtAuthenticationFilter.java # Filtro de interceptación de solicitudes
                └── modules/
                    ├── health/
                    │   └── HealthController.java # Endpoint de verificación de estado (/api/health)
                    ├── auth/
                    │   ├── dto/ (LoginRequest, RegisterRequest, AuthResponse)
                    │   └── controller/AuthController.java # Login y Registro (/api/auth)
                    ├── categorias/               # 📁 TAREA 1: Categorías
                    │   ├── entity/Categoria.java
                    │   ├── repository/CategoriaRepository.java
                    │   ├── service/CategoriaService.java
                    │   └── controller/CategoriaController.java
                    ├── usuarios/                 # 📁 TAREA 2: Usuarios & Accesos
                    │   ├── entity/Usuario.java
                    │   ├── repository/UsuarioRepository.java
                    │   ├── service/UsuarioService.java
                    │   └── controller/UsuarioController.java
                    ├── retos/                    # 📁 TAREA 3: Retos & Gamificación
                    │   ├── entity/Reto.java
                    │   ├── repository/RetoRepository.java
                    │   ├── service/RetoService.java
                    │   └── controller/RetoController.java
                    ├── empresas/                 # 📁 TAREA 4: Empresas Aliadas
                    │   ├── entity/Empresa.java
                    │   ├── repository/EmpresaRepository.java
                    │   ├── service/EmpresaService.java
                    │   └── controller/EmpresaController.java
                    └── registros/                # 📁 TAREA 5: Bitácora & Auditorías
                        ├── entity/Registro.java
                        ├── repository/RegistroRepository.java
                        ├── service/RegistroService.java
                        └── controller/RegistroController.java
```

---

## 🛠️ Requisitos Previos

1. **Java JDK 17 o superior** (OpenJDK o Oracle JDK).
2. **Apache Maven 3.8+** (o el Maven integrado en tu IDE).
3. **MySQL Server** (activo a través de XAMPP, WampServer, Docker o MySQL Workbench en el puerto 3306).
4. Un IDE Java recomendado: **IntelliJ IDEA**, **Eclipse**, **VS Code** (con Java Extension Pack) o **Apache NetBeans**.

---

## ⚙️ Pasos para Configurar y Ejecutar

### 1. Crear y Poblar la Base de Datos en MySQL
1. Enciende **MySQL** en tu servidor local (ej. botón **Start** en XAMPP).
2. Abre phpMyAdmin (`http://localhost/phpmyadmin`) o MySQL Workbench.
3. Importa y ejecuta el script:
   ```
   backend/database/schema.sql
   ```
   *Esto creará la base de datos `proyecto_integrador_db` con todas las tablas y datos iniciales.*

### 2. Verificar Credenciales en `application.properties`
Abre [src/main/resources/application.properties](file:///c:/Users/valec/Downloads/Proyecto_integrador_dise%C3%B1o_final/backend/src/main/resources/application.properties) y confirma tus credenciales:
```properties
server.port=3000
spring.datasource.url=jdbc:mysql://localhost:3306/proyecto_integrador_db?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

### 3. Ejecutar el Proyecto

#### Opción A: Desde tu IDE (IntelliJ IDEA / Eclipse / VS Code)
1. Abre la carpeta `backend` en tu IDE como proyecto Maven.
2. Localiza el archivo `ProyectoIntegradorApplication.java`.
3. Haz clic derecho y selecciona **Run 'ProyectoIntegradorApplication'** (o presiona Shift + F10 en IntelliJ).

#### Opción B: Por Línea de Comandos (Terminal)
Abre la terminal en la carpeta `backend`:
```bash
mvn clean spring-boot:run
```

El servidor quedará disponible en:
```
http://localhost:3000/api
```

---

## 🔑 Credenciales Iniciales de Prueba (Contraseña: `Admin123!`)

| Rol | Correo Electrónico | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin@sistema.com` | `Admin123!` |
| **Empresa Aliada** | `carlos.mendoza@empresa.com` | `Admin123!` |
| **Estudiante / Dev** | `mariana.gomez@tech.co` | `Admin123!` |

---

## 📡 Endpoints REST Disponibles

| Tarea / Módulo | Método | Endpoint | Descripción |
| :--- | :---: | :--- | :--- |
| **Salud** | `GET` | `/api/health` | Estado del backend y Spring Boot |
| **Auth** | `POST` | `/api/auth/login` | Iniciar sesión y generar JWT |
| **Auth** | `POST` | `/api/auth/register` | Registrar nuevo usuario |
| **Auth** | `GET` | `/api/auth/me` | Obtener usuario autenticado |
| **Tarea 1 (Categorías)** | `GET, POST` | `/api/categorias` | Listar y crear categorías |
| **Tarea 1 (Categorías)** | `GET, PUT, DELETE` | `/api/categorias/{id}` | Consultar, editar y eliminar |
| **Tarea 2 (Usuarios)** | `GET, POST` | `/api/usuarios` | Listar y crear usuarios |
| **Tarea 2 (Usuarios)** | `GET, PUT, DELETE` | `/api/usuarios/{id}` | Consultar, editar y eliminar |
| **Tarea 3 (Retos)** | `GET, POST` | `/api/retos` | Listar y crear retos técnicos |
| **Tarea 3 (Retos)** | `GET, PUT, DELETE` | `/api/retos/{id}` | Consultar, editar y eliminar |
| **Tarea 4 (Empresas)** | `GET, POST` | `/api/empresas` | Listar y registrar empresas |
| **Tarea 4 (Empresas)** | `GET, PUT, DELETE` | `/api/empresas/{id}` | Consultar, editar y eliminar |
| **Tarea 5 (Registros)** | `GET, POST` | `/api/registros` | Listar y crear auditorías/postulaciones |
| **Tarea 5 (Registros)** | `GET, PUT, DELETE` | `/api/registros/{id}` | Consultar, editar y eliminar |

---

## 💡 Nota de Ubicación
El backend es un proyecto **Maven estándar completamente autónomo**. Si deseas moverlo a una carpeta fuera de este repositorio (por ejemplo en `Downloads/backend_java`), puedes simplemente cortar o copiar la carpeta `backend` y abrirla en tu IDE favorito.
