from fastapi import APIRouter
from models.schemas import AnalyzeRequest, AnalyzeResponse
from services.ai_service import analyze_transactions

router = APIRouter()

@router.post("/analyze")
async def analyze(request: AnalyzeRequest) -> AnalyzeResponse:
    return await analyze_transactions(request)