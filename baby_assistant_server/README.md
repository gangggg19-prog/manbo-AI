# Manbo Java service

Spring Boot business service for baby profiles, care records, summaries,
timelines, AI orchestration, authentication, family invitations, private chat,
and WebSocket broadcasts.

## Run locally

1. Start PostgreSQL and create the local database/user described in
   `.env.example`.
2. Configure `BABY_DATABASE_PASSWORD` in the IDE run configuration or current
   terminal. Do not put the real password in `application.yml`.
3. Optionally set a long random `BABY_JWT_SECRET`. When omitted, local startup
   generates an in-memory key and existing login tokens expire on restart.
4. Start `BabyAssistantServerApplication`.
5. Verify `GET http://localhost:8080/api/v1/health`.

`api.http` contains placeholder accounts and tokens only.

## Public deployment warning

The family chat and invitation endpoints enforce authenticated membership.
The remaining baby-data endpoints still need unified authentication and
ownership checks before this service can be exposed to the public internet.

