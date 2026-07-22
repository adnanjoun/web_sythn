import os
from contextlib import asynccontextmanager
from enum import Enum
from pathlib import Path

import torch
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM

MAX_INPUT_CHARS = 8000
MAX_NEW_TOKENS = 512
ASSETS_DIR = Path(__file__).parent / "assets"


class SupportedModel(str, Enum):
    MEDGEMMA = "MEDGEMMA"


def _require_env(key: str) -> str:
    value = os.getenv(key)
    if not value:
        raise RuntimeError(f"Required env var '{key}' is not set. (add to .env)")
    return value


MODEL_REGISTRY: dict[SupportedModel, str] = {
    SupportedModel.MEDGEMMA: _require_env("MEDGEMMA_MODEL_PATH"),
}

PROMPT_REGISTRY: dict[SupportedModel, str] = {
    SupportedModel.MEDGEMMA: "summarize-prompt",
}

_model_cache: dict[SupportedModel, tuple] = {}


def load_prompt(name: str) -> str:
    return (ASSETS_DIR / f"{name}.txt").read_text(encoding="utf-8").strip()


def get_model(model_enum: SupportedModel) -> tuple:
    if model_enum not in _model_cache:
        model_path = MODEL_REGISTRY[model_enum]
        # dtype = torch.bfloat16 if torch.cuda.is_available() else torch.float32
        dtype = torch.bfloat16
        tokenizer = AutoTokenizer.from_pretrained(model_path)
        model = AutoModelForCausalLM.from_pretrained(
            model_path,
            torch_dtype=dtype,
            device_map="auto",
        )
        model.eval()
        _model_cache[model_enum] = (tokenizer, model)
    return _model_cache[model_enum]


@asynccontextmanager
async def lifespan(app: FastAPI):
    get_model(SupportedModel.MEDGEMMA)
    yield


app = FastAPI(title="AI Service", lifespan=lifespan)


class SummarizeRequest(BaseModel):
    patient_data: str
    model: SupportedModel = SupportedModel.MEDGEMMA


class SummarizeResponse(BaseModel):
    summary: str


@app.get("/health")
def health():
    return {"status": "ok", "loaded_models": list(_model_cache.keys())}


@app.post("/summarize", response_model=SummarizeResponse)
def summarize(request: SummarizeRequest) -> SummarizeResponse:
    if not request.patient_data:
        raise HTTPException(status_code=400, detail="patient_data must not be empty")

    tokenizer, model = get_model(request.model)
    prompt_name = PROMPT_REGISTRY[request.model]
    prompt_template = load_prompt(prompt_name)
    truncated_patient_data = request.patient_data[-MAX_INPUT_CHARS:]
    content = prompt_template.format(patient_data=truncated_patient_data)

    messages = [{"role": "user", "content": content}]

    inputs = tokenizer.apply_chat_template(
        messages,
        return_tensors="pt",  # for gemma because pytorch (adjust in case other models)
        return_dict=True,
        add_generation_prompt=True,
    ).to(model.device)

    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,
            # In case we wan more "creative responses" -> true
            # we also can play around with temp / top
            do_sample=False,
            temperature=1.0,
            top_p=1.0,
        )

    input_len = inputs["input_ids"].shape[-1]
    summary = tokenizer.decode(outputs[0][input_len:], skip_special_tokens=True)
    return SummarizeResponse(summary=summary.strip())
