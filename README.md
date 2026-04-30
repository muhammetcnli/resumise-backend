# Resumise Backend API

Resumise is a comprehensive AI-powered platform that allows users to upload their resumes (CVs) and analyze how well they match specific job postings. The system identifies strengths, gaps, and areas for improvement, and provides actionable recommendations backed by artificial intelligence.

This repository contains the **Java Spring Boot** backend service that forms the core of the Resumise platform.

[![GitHub Repository](https://img.shields.io/badge/GitHub-Repository-blue?logo=github)](https://github.com/muhammetcnli/resumise-backend)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3.1-85EA2D?logo=swagger&logoColor=black)
![OAuth2](https://img.shields.io/badge/OAuth2-Google-4285F4?logo=google&logoColor=white)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [Main Features](#main-features)
- [API Documentation](#api-documentation)
- [Implementation Notes](#implementation-notes)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Deployment](#deployment)
- [Achievements](#achievements)

---

## Project Overview

Resumise is a full-stack AI-powered resume analysis platform split into three independent, modular services:

| Service | Technology | Role |
|---|---|---|
| Backend | Spring Boot | REST API, auth, CV management, orchestration |
| AI Service | Python + Gemini | CV and job description analysis |
| Frontend | React | User interface |

This separation keeps the system easier to maintain and allows each layer to be scaled independently.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Java 21, Spring Boot 3, Spring MVC |
| Database | PostgreSQL 14+ |
| Security & Authorization | Spring Security, OAuth2 (Google Login) |
| API Documentation | Swagger (OpenAPI 3.1) |
| Containerization | Docker, GitHub Actions, GHCR |
| AI Integration | Python-based AI Service (Gemini-powered) |
| Frontend | React |

---

## System Architecture

The application follows a service-based architecture where each layer has a single, well-defined responsibility.

- **Frontend → Backend**: The React app communicates exclusively with the Spring Boot backend via REST
- **Backend → AI Service**: The backend forwards CV and job data to the Python AI microservice
- **Backend → Database**: PostgreSQL stores users, CV metadata, and analysis records
- **Backend → File Storage**: Uploaded CV files are stored on disk and mounted as a persistent volume in Docker

This approach keeps the AI layer isolated from the frontend and positions the backend as the single source of truth.

![System Architecture Diagram](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/flow.png)

---

## Database Schema

The relational database is structured on PostgreSQL and optimized for user management, CV storage, job posting tracking, and persisting AI analysis results.

![ER Diagram](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/diagram.png)

---

## Main Features

- Google OAuth2 login and local email/password authentication
- Session-cookie based auth flow
- CV upload, listing, default CV selection, and deletion
- AI-driven CV and job description analysis
- Analysis history and detail views
- Persistent file storage for uploaded CVs
- Dockerized backend and AI service integration

---

## API Documentation

All API endpoints are documented with Swagger (OpenAPI 3.1). The system is organized into modular controllers covering authorization, profile management, CV operations, and AI analysis workflows.

### Controller Overview

| Controller | Responsibility |
|---|---|
| `auth-controller` | Registration, login, and token management |
| `cv-controller` | CV upload, retrieval, and deletion |
| `analysis-controller` | AI-powered CV-to-job matching |
| `profile-controller` | User profile management |
| `dashboard-controller` | Usage statistics and history |

![Swagger Overview](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/swagger1.png)

![Swagger Endpoint Detail](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/swagger2.png)

### Google OAuth2 Integration

To streamline the user experience and strengthen security, the platform supports sign-in and registration via Google OAuth2.

![Google OAuth2 Login Flow](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/googleOauth2.png)

### AI CV Analysis — `POST /api/v1/analyses`

This is the core endpoint of the platform. It accepts a user's CV alongside a target job posting and performs a deep AI-driven comparison. A successful response (`201 Created`) returns a structured JSON object containing:

- **Match Score** — A numerical compatibility score between the CV and the job posting
- **Summary** — A high-level overview of the candidate's fit
- **Strengths** — Areas where the CV aligns well with the job requirements
- **Gaps** — Skills or experiences that are missing or underdeveloped relative to the role
- **Action Items** — Concrete steps the candidate can take to improve their profile
- **Interview Preparation** — Targeted advice based on the job description

The screenshot below shows a real Swagger response for a successful analysis performed against a Spring Boot developer position.

![AI Analysis Response](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/ResponseSwagger.png)

---

## Implementation Notes

These are key design decisions made during development that are worth understanding before contributing or extending the system.

**Service separation is intentional.** The backend and AI service are kept as separate deployable units. The frontend should never communicate directly with the AI service — all requests go through the backend.

**CV files are stored on disk, not in the database.** Uploaded files are written to a directory that is mounted as a persistent Docker volume. Only metadata (filename, path, upload date) is stored in PostgreSQL. This keeps the database lean and uploads manageable.

**Analysis records are persisted for history.** Every analysis result is saved to the database, allowing users to revisit past analyses without re-running them.

**The backend is published as a Docker image via GitHub Actions.** On every push to `main`, the CI pipeline builds and pushes the image to GitHub Container Registry (GHCR), tagged as both `latest` and the commit SHA.

**Session-cookie auth is used instead of JWT.** This simplifies the auth flow for browser clients and avoids token management complexity on the frontend side.

---

## Getting Started

### Prerequisites

- Docker and Docker Compose
- A Google OAuth2 Client ID and Secret
- Access to the companion Python AI service

### 1. Clone the repository

```bash
git clone https://github.com/muhammetcnli/resumise-backend
cd resumise
```

### 2. Configure environment variables

Create a `.env` file in the project root (see [Environment Variables](#environment-variables) below) and fill in the required values.

### 3. Start all services

```bash
docker compose up -d
```

This starts PostgreSQL, the backend, and the AI service together.

### 4. Access the API documentation

```
http://localhost:8080/swagger-ui/index.html
```

### 5. Authenticate and use the platform

Once running, users can register or log in, upload a CV, select it for analysis, submit a job description, and receive AI-generated feedback.

---

## Environment Variables

The following environment variables must be set for the backend to run correctly.

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC connection URL for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `AI_BASE_URL` | Base URL of the Python AI microservice |
| `FRONTEND_URL` | Origin URL of the React frontend (for CORS) |
| `SESSION_COOKIE_SAME_SITE` | SameSite policy for session cookies |
| `SESSION_COOKIE_SECURE` | Whether to require HTTPS for session cookies |

### Example Docker Compose service block

```yaml
backend:
  image: ghcr.io/muhammetcnli/resumise-backend:latest
  container_name: resumise-backend
  restart: unless-stopped
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/resumise-db
    SPRING_DATASOURCE_USERNAME: resumise-user
    SPRING_DATASOURCE_PASSWORD: resumise-password
    AI_BASE_URL: http://ai-api:8000
    FRONTEND_URL: http://localhost:5173
  ports:
    - "8080:8080"
```

---

## Deployment

The backend is built and published as a Docker image using **GitHub Actions** and pushed to **GitHub Container Registry (GHCR)**.

On every push to the `main` branch, the pipeline:

1. Checks out the repository
2. Builds the Docker image from `backend/Dockerfile`
3. Tags it as `latest` and with the commit SHA
4. Pushes it to GHCR

### Pull the latest image

```bash
docker login ghcr.io
docker pull ghcr.io/muhammetcnli/resumise-backend:latest
```

### Production checklist

- Use a stable, SHA-tagged image from GHCR rather than `latest`
- Mount uploaded CV files to a persistent volume
- Set `AI_BASE_URL` to the AI service container name within the Docker network
- Configure OAuth redirect URLs to match the deployed backend domain
- Enable HTTPS and set `SESSION_COOKIE_SECURE=true` for browser auth flows

---

## Achievements

**Bolu Abant Izzet Baysal University — Ar-Ge Pazari 2026**

Resumise was exhibited at the Bolu Duzce Ar-Ge Pazari under the **"Ali Kuscu Young Innovators"** category, contributing to the regional innovation and entrepreneurship ecosystem.

![Event Stand](https://github.com/muhammetcnli/resumise-backend/blob/main/uploads/photos/event.jpg)

---

## License

This project is currently unlicensed. All rights reserved by the project authors.
