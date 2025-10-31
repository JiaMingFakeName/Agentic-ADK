"""Runtime configuration powered by pydantic settings models."""

from __future__ import annotations

from functools import lru_cache
from typing import Optional

from pydantic import AliasChoices, BaseModel, Field, SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict


class DashScopeSettings(BaseModel):
    """Configuration for DashScope compatible services."""

    api_key: SecretStr
    base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    default_model: str = "qwen-plus"
    app_id: Optional[str] = None

    @property
    def api_key_value(self) -> str:
        return self.api_key.get_secret_value()


class BailianSettings(BaseModel):
    """Configuration for the Bailian application tooling."""

    api_key: Optional[SecretStr] = None
    app_id: Optional[str] = None

    @property
    def api_key_value(self) -> Optional[str]:
        return self.api_key.get_secret_value() if self.api_key else None


class OpenAISettings(BaseModel):
    """Configuration for OpenAI compatible services."""

    api_key: SecretStr
    base_url: Optional[str] = None
    embedding_model: str = "text-embedding-3-small"
    chat_model: Optional[str] = None
    user: Optional[str] = None

    @property
    def api_key_value(self) -> str:
        return self.api_key.get_secret_value()


class RuntimeSettings(BaseSettings):
    """Top level settings aggregated from the environment or `.env` files."""

    dashscope_api_key: Optional[SecretStr] = None
    dashscope_base_url: Optional[str] = Field(default=None)
    dashscope_app_id: Optional[str] = None
    dashscope_default_model: str = Field(default=DashScopeSettings.model_fields["default_model"].default)

    bailian_api_key: Optional[SecretStr] = Field(
        default=None,
        validation_alias=AliasChoices("BAILIAN_API_KEY", "AK"),
    )
    bailian_app_id: Optional[str] = None

    openai_api_key: Optional[SecretStr] = None
    openai_base_url: Optional[str] = None
    openai_embedding_model: str = Field(default=OpenAISettings.model_fields["embedding_model"].default)
    openai_chat_model: Optional[str] = None
    openai_user: Optional[str] = None

    model_config = SettingsConfigDict(
        env_file=(".env", ".env.local"),
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=False,
    )

    def dashscope(self) -> Optional[DashScopeSettings]:
        if not self.dashscope_api_key:
            return None
        return DashScopeSettings(
            api_key=self.dashscope_api_key,
            base_url=self.dashscope_base_url or DashScopeSettings.model_fields["base_url"].default,
            default_model=self.dashscope_default_model,
            app_id=self.dashscope_app_id,
        )

    def bailian(self) -> Optional[BailianSettings]:
        if not self.bailian_api_key:
            return BailianSettings(app_id=self.bailian_app_id)
        return BailianSettings(api_key=self.bailian_api_key, app_id=self.bailian_app_id)

    def openai(self) -> Optional[OpenAISettings]:
        if not self.openai_api_key:
            return None
        return OpenAISettings(
            api_key=self.openai_api_key,
            base_url=self.openai_base_url,
            embedding_model=self.openai_embedding_model,
            chat_model=self.openai_chat_model,
            user=self.openai_user,
        )


@lru_cache
def get_runtime_settings() -> RuntimeSettings:
    """Load runtime settings with caching to avoid repeated disk reads."""

    return RuntimeSettings()
