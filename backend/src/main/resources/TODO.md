# TODO

## 0. Setup
- Implement Docker + PostgreSQL
- Configure environments (.env, dev/prod profiles, secrets management)
- Decide folder structure and package boundaries for controller/service/repository/domain
- Decide file storage strategy (local first, then S3/MinIO)
- Decide API style for frontend/backend and backend/AI (REST first, async later if needed)

## 1. Domain Model / Core Entities
### User
- Create user entity - UUID id, firstName, lastName, email, createdAt, updatedAt
- Add optional profile fields later if needed (LinkedIn, graduation, title, bio)
- User should not hold a single CV field; one user can have multiple CVs

### Auth Account
- Create oauth account entity - provider, providerUserId, userId, createdAt, lastLoginAt
- Support multiple providers for the same user if needed

### Credential
- Create optional credential entity - userId, passwordHash, enabled, lastLoginAt, passwordUpdatedAt
- Use this only if normal email/password login will be enabled

### CV
- Create cv entity - id, userId, fileName, filePath, fileType, fileSize, uploadedAt, updatedAt
- Add optional fields later if needed - title, isDefault, parsedText, language, version
- Keep CV as a separate entity because user can upload more than one CV

### Job Posting / Job Target
- Create job posting entity - id, userId, jobLink, normalizedJobLink, companyName, positionName, notes, createdAt
- This should represent the job being analyzed, not the analysis itself

### Analysis Request
- Create analysis request entity - id, userId, cvId, jobPostingId, status, requestId/correlationId, createdAt, startedAt, finishedAt, errorMessage
- Status examples - queued, running, done, failed
- This entity should track the lifecycle of one analysis attempt

### Analysis Result
- Create analysis result entity/model - analysisRequestId, matchScore, summary, strengths, gaps, actionItems, rawResponse, createdAt
- Add optional fields later - atsScore, keywordCoverage, sectionFeedback, modelVersion, promptVersion

### Analysis Artifact / Snapshot
- Create optional snapshot entity - analysisRequestId, cvSnapshot, jobSnapshot, inputPayload, outputPayload
- Use this if we want replay/debug/history later

## 2. Auth and User Management
- Implement Google OAuth 2
- Implement optional normal auth (email/password), decide oauth-first or hybrid register flow
- Map oauth account to local user profile
- Implement login/logout flow and current user endpoint
- Implement profile update flow for basic user data
- Add account linking/unlinking logic if multiple login methods will be supported

## 3. CV Flow
- Implement CV upload flow (pdf/docx)
- Validate CV file type, size, filename rules, and duplicate upload behavior
- Create endpoint to list user CVs
- Create endpoint to select a CV for analysis
- Decide if the user can mark one CV as default

## 4. Job Analysis Flow
- Create job link input flow (job link + optional notes)
- Validate and normalize URL
- Decide if job metadata will be extracted from the page or entered manually
- Create analysis start flow - user selects existing CV or uploads new one, then submits job link
- Keep analysis request history per user

## 5. AI Integration
- Implement backend -> AI API client (timeout, retry, auth token, error handling)
- Implement async analysis flow (submit analysis, get status, fetch result)
- Add request correlation id between backend and AI service
- Add logging/tracing for AI requests (latency, fail reason, correlation id)
- Implement basic rate limit and quota rules for AI usage
- Decide sync vs async behavior for first version

## 6. API Contract and Frontend Integration
- Implement frontend contract endpoints for auth, CV list/upload, job analysis start, and analysis result retrieval
- Create analysis status endpoint and analysis result endpoint
- Create profile endpoints for frontend usage
- Define request/response DTOs and error codes clearly
- Decide and document v1 scope - job-link based analysis first, CV improvement and ATS scoring later if needed

## 7. Testing
- Add tests - auth, upload, AI integration (mock), e2e happy path
- Add unit tests for entities, validation, and service layer
- Add integration tests for AI client and database flow

## 8. Docs
- Create API docs (OpenAPI/Swagger) + simple architecture diagram
- Document backend <-> AI service contract
- Document frontend <-> backend contract
- Document v1 scope and future features
