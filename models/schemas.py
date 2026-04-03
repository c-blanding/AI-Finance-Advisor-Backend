
from pydantic import BaseModel
from typing import Optional, List
from datetime import date

# Individual transaction coming from Spring Boot
class TransactionItem(BaseModel):
    id: str
    amount: float
    description: str
    date: date
    category: Optional[str] = None
    source: str

# What Spring Boot sends to this service
class AnalyzeRequest(BaseModel):
    userId: str
    currentTransactions: List[TransactionItem]
    historicalTransactions: List[TransactionItem]

# Overspending flag for a single category
class OverspendingFlag(BaseModel):
    category: str
    currentSpend: float
    averageSpend: float
    severity: str  # "warning" or "critical"

# Single savings recommendation
class SavingsRecommendation(BaseModel):
    category: str
    suggestion: str
    potentialSaving: float

# Spending prediction for a category
class SpendingPrediction(BaseModel):
    category: str
    currentSpend: Optional[float] = None
    historicalAverage: Optional[float] = None
    predictedAmount: float
    onTrack: bool
    variance: Optional[float] = None
    alert: Optional[str] = None

# Full response back to Spring Boot
class AnalyzeResponse(BaseModel):
    categories: dict
    overspending: List[OverspendingFlag]
    recommendations: List[SavingsRecommendation]
    predictions: List[SpendingPrediction]