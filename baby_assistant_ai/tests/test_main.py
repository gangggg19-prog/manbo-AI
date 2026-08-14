import json
import os
import unittest
from unittest.mock import MagicMock, patch

from app.main import ChatRequest, chat


class QwenAdapterTest(unittest.TestCase):
    def test_uses_qwen_when_a_key_and_provider_reply_are_available(self):
        request = ChatRequest(
            date="2026-08-01",
            message="今天睡得怎么样？",
            daily_summary={
                "feedingMl": 120,
                "diaperCount": 1,
                "sleepMinutes": 300,
                "sleepInProgress": False,
                "insight": "DAILY_RECORDS_READY",
            },
        )
        provider_response = MagicMock()
        provider_response.read.return_value = json.dumps(
            {"choices": [{"message": {"content": "这是千问回复。"}}]}
        ).encode("utf-8")
        provider_call = MagicMock()
        provider_call.__enter__.return_value = provider_response

        with patch.dict(os.environ, {"DASHSCOPE_API_KEY": "test-key"}), patch(
            "app.main.urlopen", return_value=provider_call
        ):
            response = chat(request)

        self.assertEqual("qwen", response.source)
        self.assertEqual("这是千问回复。", response.reply)


if __name__ == "__main__":
    unittest.main()
