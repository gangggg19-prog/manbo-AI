"""AI service for Manbo.

Java owns business context and sends only the current baby's necessary daily
summary, short conversation history, and reviewed knowledge snippets. This
service uses Qwen when configured, with a transparent local fallback for demos.
"""

import json
import os
from typing import Literal
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from fastapi import FastAPI
from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

app = FastAPI(title="Manbo AI Service", version="0.3.0")

QWEN_CHAT_URL = os.getenv(
    "QWEN_CHAT_URL",
    "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
)
QWEN_MODEL = os.getenv("QWEN_MODEL", "qwen-plus")
SAFETY_NOTICE = (
    "这是日常育儿参考，不替代医生诊断；如出现高热、呼吸困难、"
    "持续呕吐、精神状态明显异常等情况，请及时就医。"
)


class _CamelCaseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class DailySummary(_CamelCaseModel):
    feeding_ml: int = Field(ge=0)
    diaper_count: int = Field(ge=0)
    sleep_minutes: int = Field(ge=0)
    sleep_in_progress: bool
    insight: str


class ChatTurn(_CamelCaseModel):
    role: Literal["user", "assistant"]
    content: str = Field(min_length=1, max_length=1200)


class KnowledgeSnippet(_CamelCaseModel):
    title: str = Field(min_length=1, max_length=180)
    content: str = Field(min_length=1, max_length=2000)
    source_name: str = Field(min_length=1, max_length=160)
    source_url: str = Field(min_length=1, max_length=1000)


class KnowledgeReference(_CamelCaseModel):
    title: str
    source_name: str
    source_url: str


class ChatRequest(_CamelCaseModel):
    baby_age_months: int = Field(default=0, ge=0, le=240)
    date: str
    message: str = Field(min_length=1, max_length=600)
    daily_summary: DailySummary
    history: list[ChatTurn] = Field(default_factory=list, max_length=20)
    knowledge: list[KnowledgeSnippet] = Field(default_factory=list, max_length=3)


class ChatResponse(_CamelCaseModel):
    reply: str
    safety_notice: str
    source: Literal["qwen", "python-local-rules"]
    suggested_actions: list[str]
    references: list[KnowledgeReference] = Field(default_factory=list)


@app.get("/health")
def health() -> dict[str, str]:
    """Lets Java or a browser verify that the Python process is running."""
    return {"service": "manbo-ai", "status": "ok"}


@app.post("/v1/chat", response_model=ChatResponse)
def chat(request: ChatRequest) -> ChatResponse:
    """Try Qwen first; retain a grounded local answer if the provider is unavailable."""
    references = to_references(request.knowledge)
    qwen_reply = ask_qwen(request)
    if qwen_reply is not None:
        return ChatResponse(
            reply=qwen_reply,
            safety_notice=SAFETY_NOTICE,
            source="qwen",
            suggested_actions=["查看今日简报", "继续记录今天"],
            references=references,
        )

    reply, actions = build_local_reply(
        request.message.strip(),
        request.daily_summary,
        request.knowledge,
    )
    return ChatResponse(
        reply=reply,
        safety_notice=SAFETY_NOTICE,
        source="python-local-rules",
        suggested_actions=actions,
        references=references,
    )


def ask_qwen(request: ChatRequest) -> str | None:
    """Call Qwen through its OpenAI-compatible HTTP endpoint without logging keys."""
    # Read provider credentials only from the process environment. Never add a
    # real key to source code, examples, tests, logs, or HTTP responses.
    api_key = os.getenv("DASHSCOPE_API_KEY", "").strip()
    if not api_key:
        return None

    summary = request.daily_summary
    knowledge_context = "\n\n".join(
        f"资料标题：{item.title}\n来源：{item.source_name}\n内容：{item.content}"
        for item in request.knowledge
    ) or "本次没有检索到与问题直接相关的知识资料。"
    system_prompt = f"""你是 Manbo AI 育儿助理。
宝宝当前约 {request.baby_age_months} 月龄。今天是 {request.date}，已知记录：喂养 {summary.feeding_ml} ml、尿布 {summary.diaper_count} 次、睡眠 {summary.sleep_minutes} 分钟、睡眠进行中：{summary.sleep_in_progress}。

当用户询问身份、模型或能力时，说明你是 Manbo AI 育儿助理，会结合宝宝记录和经过审核的育儿资料提供日常参考；不要声称自己不使用大模型或不调用 AI 服务。
请用简洁温和的中文回答。若用户问题明确提到的月龄与档案月龄不同，优先按用户问题中的月龄解释资料。优先依据下方“可参考资料”；资料不足时明确说明需要补充的观察信息。不要作诊断、不要给出药物剂量、不要编造未提供的数据。若涉及高热、呼吸困难、持续呕吐、精神状态明显异常或其他紧急症状，明确建议及时就医。

可参考资料：
{knowledge_context}"""
    payload = {
        "model": QWEN_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            *[{"role": turn.role, "content": turn.content} for turn in request.history],
            {"role": "user", "content": request.message.strip()},
        ],
        "temperature": 0.35,
        "max_tokens": 500,
        "stream": False,
    }
    http_request = Request(
        QWEN_CHAT_URL,
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        },
        method="POST",
    )
    try:
        with urlopen(http_request, timeout=20) as response:
            body = json.loads(response.read().decode("utf-8"))
        content = body["choices"][0]["message"]["content"]
        return content.strip() if isinstance(content, str) and content.strip() else None
    except (HTTPError, URLError, TimeoutError, KeyError, IndexError, TypeError, ValueError):
        # Do not expose provider internals or credentials to parents.
        return None


def to_references(knowledge: list[KnowledgeSnippet]) -> list[KnowledgeReference]:
    return [
        KnowledgeReference(
            title=item.title,
            source_name=item.source_name,
            source_url=item.source_url,
        )
        for item in knowledge
    ]


def build_local_reply(
    message: str,
    summary: DailySummary,
    knowledge: list[KnowledgeSnippet],
) -> tuple[str, list[str]]:
    """Deterministic fallback used when no Qwen key is configured or Qwen is unavailable."""
    context = (
        f"我已读取到宝宝今天的记录：喂养 {summary.feeding_ml} ml、"
        f"尿布 {summary.diaper_count} 次、睡眠 {summary.sleep_minutes} 分钟。"
    )
    lower_message = message.lower()

    if any(word in message or word in lower_message for word in ("你是", "模型", "大模型", "身份")):
        return (
            "我是 Manbo AI 育儿助理，会结合宝宝的记录与经过审核的育儿资料，"
            "提供日常养育参考；我不能替代医生诊断。",
            ["查看今日简报", "继续记录今天"],
        )

    if knowledge:
        article = knowledge[0]
        return (
            f"{context} 关于“{message}”，匹配到的资料“{article.title}”提示：{article.content}",
            ["查看参考资料", "继续记录今天"],
        )

    if any(word in message for word in ("睡", "作息", "夜醒")):
        current = "目前有一段睡眠仍在进行中。" if summary.sleep_in_progress else "目前没有进行中的睡眠记录。"
        return (
            f"{context}{current} 你可以继续记录每次入睡和醒来时间，先观察连续几天的节律；"
            "如果担心异常，请描述宝宝月龄、精神状态和具体表现。",
            ["继续记录睡眠", "查看今日简报"],
        )

    if any(word in message or word in lower_message for word in ("奶", "喂", "feeding")):
        return (
            f"{context} 关于喂养节奏，单日总量只是一个参考，更值得看宝宝的精神状态、"
            "尿量和连续趋势。你可以补充这次喂养的时间、方式和宝宝反应。",
            ["记录一顿奶", "查看近 7 天趋势"],
        )

    if any(word in message for word in ("尿", "便", "拉")):
        return (
            f"{context} 排泄记录建议看次数、颜色和宝宝状态的组合变化。"
            "若出现明显异常颜色、血便、持续腹泻或脱水迹象，请咨询医生。",
            ["记录尿布", "查看今日简报"],
        )

    return (
        f"{context} 你问的是“{message}”。我可以先结合今天的记录给出日常参考；"
        "为了更贴近情况，可以继续补充宝宝月龄、发生时间和观察到的具体表现。",
        ["查看今日简报", "继续记录今天"],
    )
