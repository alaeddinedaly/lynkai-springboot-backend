# LynkAI Backend

Spring Boot REST API for LynkAI - providing authentication, document management, and activity tracking.

## 🚀 Features

- **JWT Authentication**: Secure token-based auth with refresh tokens
- **Email Verification**: User registration with email confirmation codes
- **Document Management**: Store and manage document metadata
- **Activity Logging**: Track user actions and system events
- **RESTful API**: Clean, well-documented endpoints
- **MySQL Database**: Persistent data storage

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- SMTP server for email (Gmail, SendGrid, etc.)

## 🛠️ Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd lynkai-backend
   ```

2. **Create MySQL Database**
   ```sql
   CREATE DATABASE lynkai;
   ```

3. **Configure Application Properties**

   Create `src/main/resources/application.properties`:
   ```properties
   # Application Name
   spring.application.name=lynkai
   
   # JWT Configuration
   jwt.secret=q1ZrV3lqU2F0bXhOb1hVeE9yY0dGZ2R6R0NVeVBoUXk=
   
   # --- DATABASE CONFIGURATION ---
   spring.datasource.url=jdbc:mysql://localhost:3306/lynkai?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci
   spring.datasource.username=root
   spring.datasource.password=root
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   # --- JPA SETTINGS ---
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   spring.jpa.properties.hibernate.connection.characterEncoding=utf8mb4
   spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
   spring.jpa.properties.hibernate.connection.useUnicode=true
   spring.jpa.properties.hibernate.connection.collation=utf8mb4_unicode_ci
   
   # --- SECURITY (TEMP DEFAULT USER) ---
   spring.security.user.name=admin
   spring.security.user.password=1234
   
   # --- MAIL CONFIGURATION ---
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   spring.mail.properties.mail.smtp.connectiontimeout=5000
   spring.mail.properties.mail.smtp.timeout=5000
   spring.mail.properties.mail.smtp.writetimeout=5000
   
   # --- FILE UPLOADS ---
   app.upload.dir=uploads
   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB
   
   # --- LOGGING ---
   logging.level.com.lynkai.service=INFO
   ```

   **⚠️ Security Note**: Change the following for production:
    - `jwt.secret` - Generate a new secure key
    - `spring.datasource.password` - Use a strong password
    - `spring.mail.username` and `spring.mail.password` - Use your Gmail credentials
    - Remove default security user or use proper authentication

4. **Install Dependencies**
   ```bash
   mvn clean install
   ```

## 🏃 Running the Application

### Development Mode
```bash
mvn spring-boot:run
```

### Build JAR
```bash
mvn clean package
java -jar target/lynkai-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`

## 📁 Project Structure

```
src/main/java/com/lynkai/
├── controller/          # REST Controllers
│   ├── AuthController.java
│   ├── DocumentController.java
│   └── ActivityLogController.java
├── service/            # Business Logic
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── DocumentService.java
│   └── EmailService.java
├── model/              # Entity Models
│   ├── User.java
│   ├── Document.java
│   ├── ActivityLog.java
│   └── RefreshToken.java
├── repository/         # Data Access Layer
│   ├── UserRepository.java
│   ├── DocumentRepository.java
│   └── ActivityLogRepository.java
├── security/           # Security Configuration
│   ├── JwtAuthFilter.java
│   ├── SecurityConfig.java
│   └── HashEncoder.java
└── dto/               # Data Transfer Objects
    ├── LoginRequest.java
    ├── RegisterRequest.java
    └── DocumentResponse.java
```

## 🔌 API Endpoints

### Authentication (`/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | User login |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Invalidate refresh token |
| POST | `/auth/verify-email` | Verify email with code |
| POST | `/auth/resend-verification` | Resend verification code |
| GET | `/auth/check-verification` | Check verification status |

### Documents (`/documents`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents` | Get all user documents |
| GET | `/documents/{id}` | Get document by ID |
| POST | `/documents` | Create new document |
| PUT | `/documents/{id}` | Update document |
| DELETE | `/documents/{id}` | Delete document |

### Activity Logs (`/activity-logs`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/activity-logs` | Get user activity logs |
| POST | `/activity-logs` | Create activity log |

## 🔒 Security

### JWT Authentication
- **Access Token**: 15 minutes expiry
- **Refresh Token**: 30 days expiry, stored hashed in database
- **Password Hashing**: BCrypt with salt

### Email Verification
- 6-digit verification code
- 10-minute expiry
- Required before login

## 🗄️ Database Schema

The database tables are **automatically created** by Hibernate when you run the application (thanks to `spring.jpa.hibernate.ddl-auto=update`).

### Main Tables Created:

#### Users Table
- `id` - Primary key
- `username` - Unique username
- `email` - Unique email address
- `password_hash` - Hashed password
- `verified` - Email verification status
- `created_at` - Account creation timestamp

#### Documents Table
- `id` - Primary key
- `title` - Document title
- `file_path` - Path to uploaded file
- `user_id` - Foreign key to users
- `created_at` - Upload timestamp

#### Document Chunks Table
- `id` - Primary key
- `text_content` - Chunk text content
- `chunk_index` - Position in document
- `document_id` - Foreign key to documents
- `created_at` - Processing timestamp

#### Activity Logs Table
- `id` - Primary key
- `user_id` - Foreign key to users
- `action_type` - Type of action (LOGIN, REGISTER, etc.)
- `created_at` - Action timestamp

#### Refresh Tokens Table
- `id` - Primary key
- `user_id` - Foreign key to users
- `hashed_token` - Hashed refresh token
- `expires_at` - Token expiration time

#### Email Verification Table
- `id` - Primary key
- `user_id` - Foreign key to users
- `code` - 6-digit verification code
- `expires_at` - Code expiration time

**Note**: You don't need to manually create these tables. Just ensure the `lynkai` database exists, and Spring Boot will handle the rest!

## 🔧 Configuration

### Gmail App Password
1. Enable 2-factor authentication on your Google account
2. Generate app password: [Google Account Settings](https://myaccount.google.com/apppasswords)
3. Use the generated 16-character password in `spring.mail.password`
4. Update `spring.mail.username` with your Gmail address

### File Upload Directory
The application will automatically create the `uploads/` directory if it doesn't exist.

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Skip tests during build
mvn clean package -DskipTests
```

## 📊 Monitoring & Logging

- **Logging**: SLF4J with Logback
- **Actuator Endpoints**: `/actuator/health`, `/actuator/info`

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Test MySQL connection
mysql -u your_user -p -h localhost lynkai
```

### JWT Errors
- Verify `jwt.secret` is Base64 encoded
- Check token expiry times
- Ensure user ID is being correctly extracted

### Email Not Sending
- Check SMTP credentials
- Verify firewall/network settings
- Test with a simple email client first

## 📦 Dependencies

Key dependencies from `pom.xml`:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- MySQL Connector
- JWT (jjwt)
- Lombok
- Spring Boot Starter Mail

## 🚀 Deployment

### Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t lynkai-backend .
docker run -p 8080:8080 lynkai-backend
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🔗 Related Projects

- [LynkAI Frontend](../lynkai-frontend) - Angular Frontend
- [LynkAI RAG](../lynkai-rag) - Python RAG Service