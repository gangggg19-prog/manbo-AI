# Security policy

## Secrets

Never commit API keys, access keys, passwords, JWT secrets, signing keys,
database exports, `.env` files, IDE run configurations, or saved HTTP response
files. The checked-in `.env.example` files contain placeholders only.

If a secret is ever pasted into a chat, screenshot, issue, commit, or log, treat
it as compromised and rotate it at the provider even if it does not appear in
the current source tree.

## Personal data

Do not open an issue containing baby names, dates of birth, care records,
family messages, access tokens, invitation codes, or AI conversation history.
Use synthetic fixtures when reporting bugs.

## Current scope

This is a portfolio-stage local demo. Do not expose the services to the public
internet until every user-data endpoint has authentication, authorization,
rate limiting, HTTPS, secure mobile token storage, and production monitoring.

