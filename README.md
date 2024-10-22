# Application Overview

It is a simple TODO List with manual authorization. App has SPA architecture. There are such services:

## Frontend:

The frontend is written with React.js and it has such pages:

1. Manual registration 
2. Email confirmation
3. Manual login
4. Password change
5. Todo list page

All user's requests which start with `/api/**` go through the frontend proxy and then to the backend.
Client side stores authentication keys (Access & Refresh token) in cookie storage.

## Backend

The backend is written with Spring Boot and has two REST controllers:
1. `/api/task`
2. `/api/user`

* The `/api/task` controller provides simple CRUD for tasks.
* The `/api/user` controller is responsible for: registration, login, email submission, password change, JWT refresh.

For the data storing backend uses PostgreSQL and Redis for caching. The JWT exchange is in Http Headers. Authentication of requests is made with access token.

## How to run app?

1. ```bash 
   git clone https://github.com/tvijas/docker_university
   
or just download a zip.
2. Run Docker Desktop (Windows) or Docker Driver (Linux)
3. Open `.env` file and paste your envs in such fields 
* EMAIL=;
* EMAIL_PASSWORD=;

### `^-^-^ Make sure you did it because backend will not start up ^-^-^` 
4. Run docker compose up in `docker_university` directory;
   ```bash
   docker compose --env-file .env up --build
5. Wait a long time
6. Wait ...
7. ...
8. ...
9. ...
10. ZZZzzzZzZzz
11. I hope all containers are running
12. Now you can enter frontend home page by next link: http://localhost:5173 and test it out!!!