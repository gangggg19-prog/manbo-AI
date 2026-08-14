# Manbo Baby Assistant

Manbo is a portfolio-stage baby-care assistant built with a Flutter client,
a Spring Boot business service, PostgreSQL persistence, and a FastAPI AI
service. The current version demonstrates care records, daily summaries,
growth timelines, Qwen-backed parenting assistance, reviewed knowledge
retrieval, family invitations, authenticated private chat, and WebSocket
message updates.

## Projects

- `baby_assistant_app`: Flutter Android/iOS client
- `baby_assistant_server`: Java 17, Spring Boot, MyBatis, Flyway, PostgreSQL
- `baby_assistant_ai`: Python, FastAPI, Qwen-compatible HTTP integration

## Local configuration

Real credentials are never stored in this repository. Configure them in your
terminal, IDE run configuration, CI secret store, or deployment secret manager.

Required for Java:

- `BABY_DATABASE_PASSWORD`

Recommended outside local development:

- `BABY_DATABASE_URL`
- `BABY_DATABASE_USERNAME`
- `BABY_JWT_SECRET` (use a long, randomly generated value)
- `BABY_AI_SERVICE_BASE_URL`

Optional for Python:

- `DASHSCOPE_API_KEY` (without it, the transparent local fallback is used)
- `QWEN_MODEL`

## Security status

This repository is a local demo and portfolio project, not a production-ready
medical product. Family chat and family invites require authenticated
membership. A future security milestone must add unified authentication and
ownership checks to baby profiles, care records, summaries, timelines, and AI
conversation endpoints before the backend is exposed to the public internet.

Do not use real family data in a public demo database. AI answers are parenting
references and do not replace professional medical diagnosis.

