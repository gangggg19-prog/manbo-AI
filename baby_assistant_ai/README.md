# Manbo AI service

Python AI service for the baby assistant.

## Start

```powershell
cd baby_assistant_ai
$env:DASHSCOPE_API_KEY = "your-new-key"
.\.venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000
```

`DASHSCOPE_API_KEY` is optional for local development: without it, the service returns the transparent `python-local-rules` fallback. With it, the service calls Qwen through Alibaba Cloud Model Studio's OpenAI-compatible endpoint and returns `source: qwen`.

- Health check: `http://localhost:8000/health`
- API documentation: `http://localhost:8000/docs`

Never commit a real key to this project. Store it in the IDE run configuration,
the current terminal environment, or a deployment secret manager. `.env` is
ignored and `.env.example` contains placeholders only.

The Java service owns the database context; Python receives only the current
question, short conversation history, relevant reviewed knowledge snippets,
and the minimum baby summary it needs.
