# 🏦 Loan Processing Web App (Backend)

A backend service for the **Loan Processing Web App** built with **Spring Boot**.  
This service handles user authentication, loan application management, and integrates with a mock service for credit scoring.

---

Deployed on Render [Loan Processing Backend](https://loan-processing-be.onrender.com)

*Note: Click the above link, wait a few minutes, let Render restart the server, once whiteLabel error comes on the screen means server is up🎉 and you can use the complete application from frontend now click👉[LoanProcessing-Frontend](https://loan-processing-fe.onrender.com)*

For dummy login :

Username : acc1, Password : 0000

---
## 🚀 Features

### 🔑 Authentication
- User registration and login  
- Session-based authentication using JSESSIONID

### 📝 Loan Application Management
- Submit a new loan application  
- Fetch all applications for a user  
- Fetch application by ID  


### 🔄 Retry Service
- Runs every 60 seconds  
- Fetches applications with **pending** status from db and sends them to the mock credit service to get credit score and application status and update the db.  

---

## 🛠️ Tech Stack

- ⚡ **Spring Boot**
- 🗄️ **Supabase PostgreSQL**  

---

## ⚙️ Setup & Installation (Local Development)

1. **Clone the repo**
```sh
gh repo clone harshit-002/Loan-Processing
cd loan-processing-be
```
2. Set the local database url, username, password in `application.properties` file.

*Note: set `spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
` if using MySql8*

3. **Build the project**
```sh
./mvnw clean install
# or if Maven is installed
mvn clean install
```
4. **Run the application**
```sh 
./mvnw spring-boot:run
# or
mvn spring-boot:run
```
Backend available at 👉 http://localhost:8080 🎉



## 🐳Run with Docker
1. Clone the repo 
```sh
gh repo clone harshit-002/Loan-Processing
cd loan-processing-be
```
2. Run docker compose
``` docker-compose up```

Backend available at 👉 http://localhost:8080 🎉
