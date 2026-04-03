import os
import json
from datetime import date
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from models.schemas import AnalyzeRequest, AnalyzeResponse, OverspendingFlag, SavingsRecommendation, SpendingPrediction

llm = ChatOpenAI(model="gpt-4o-mini", temperature=0)

categorize_prompt = ChatPromptTemplate.from_template("""
You are an expert financial transaction categorizer with deep knowledge of consumer spending patterns.
Your job is to accurately categorize financial transactions based on merchant names and descriptions.

Transactions to categorize:
{transactions}

Category definitions:
- Groceries: Supermarkets, food stores, wholesale clubs (Walmart, Costco, Whole Foods, Trader Joes)
- Dining: Restaurants, cafes, fast food, food delivery (Uber Eats, DoorDash, Grubhub)
- Entertainment: Movies, concerts, streaming, games, sports (Netflix, Spotify, AMC, Steam)
- Transportation: Gas, rideshare, parking, public transit, car maintenance (Uber, Lyft, Shell, BP)
- Utilities: Electric, water, gas, internet, phone bills
- Healthcare: Doctors, pharmacies, insurance, gym, mental health (CVS, Walgreens, gym memberships)
- Shopping: Clothing, electronics, home goods, online retail (Amazon, Target, Best Buy, Zara)
- Subscriptions: Recurring monthly or annual services (Adobe, Microsoft, iCloud, Apple One)
- Income: Direct deposits, transfers in, paychecks, freelance payments
- Transfer: Peer to peer payments, bank transfers (Venmo, Zelle, Cash App, PayPal)
- Education: Tuition, books, courses, student loans (Udemy, Coursera, textbooks)
- Travel: Hotels, flights, vacation rentals (Airbnb, Delta, Marriott, Expedia)
- Personal Care: Hair, beauty, spa, grooming (Ulta, Sephora, salons)
- Other: Anything that does not clearly fit above categories

Rules:
- If a merchant could fit multiple categories, choose the most specific one
- ATM withdrawals and cash are Transfer
- Amazon could be Shopping or Subscriptions — use context clues from description
- Be consistent — same merchant should always get same category

Return ONLY a valid JSON object mapping each transaction description to its category.
No explanation, no markdown, no backticks, no extra text whatsoever.
IMPORTANT: Every object MUST include ALL fields listed above. 
Never omit any field. Use null only for alert field in predictions.
Return the Transaction ID and it's Category.
Example: {{"(TransactionID)": "Subscriptions", "(TransactionID)": "Groceries"}}
""")

overspending_prompt = ChatPromptTemplate.from_template("""
You are a world class personal financial analyst specializing in spending behavior and budgeting.
Your role is to identify problematic spending patterns that are hurting the user's financial health.

Current month spending by category (in USD):
{current_spending}

Historical average monthly spending by category (in USD):
{historical_average}

Analysis rules:
- Warning: Current spend is 20-49% above the historical average for that category
- Critical: Current spend is 50% or more above the historical average
- If no historical average exists for a category, flag it as new spending and mark warning
- Do not flag Income or Transfer categories
- Focus on discretionary categories first (Dining, Entertainment, Shopping, Personal Care)

Return ONLY a valid JSON array of overspending flags. No explanation, no markdown, no backticks.
Each object must have exactly these fields:
- category: string
- currentSpend: number
- averageSpend: number
- severity: "warning" or "critical"
- insight: one sentence explaining why this is concerning

IMPORTANT: Every object MUST include ALL fields listed above. 
Never omit any field. Use null only for alert field in predictions.
If no overspending is detected return exactly: []
""")

recommendations_prompt = ChatPromptTemplate.from_template("""
You are one of the world's best personal financial advisors, combining the expertise of a CFP,
behavioral economist, and life coach. You give specific, actionable, compassionate advice that
actually changes people's financial lives — not generic tips they've heard before.

User's spending breakdown this month (USD):
{spending_breakdown}

Overspending flags:
{overspending_flags}

Rules for great recommendations:
- Be specific — reference actual dollar amounts from their data
- Be realistic — don't tell someone who spends $800 on groceries to spend $200
- Be actionable — give a concrete step they can take this week
- Address the highest impact areas first
- potentialSaving should be a realistic monthly estimate

IMPORTANT: Every object MUST include ALL fields listed above. 
Never omit any field. Use null only for alert field in predictions.

Return ONLY a valid JSON array of exactly 3 recommendations. No explanation, no markdown, no backticks.
Each object must have exactly these fields:
- category: string
- suggestion: string (2-3 sentences, specific and actionable)
- potentialSaving: number
- difficulty: "easy", "medium", or "hard"
- timeToImpact: "immediate", "this month", or "long term"
""")

predictions_prompt = ChatPromptTemplate.from_template("""
You are a quantitative financial forecasting expert who specializes in personal spending prediction.

Current spending this month by category (USD):
{current_spending}

Historical monthly averages by category (USD):
{historical_averages}

Days elapsed in current month: {days_elapsed}
Total days in this month: {total_days}
Percentage of month elapsed: {percent_elapsed}%

Prediction methodology:
- Base prediction: (current spend / percent elapsed) * 100
- onTrack is true if predicted amount is within 15% of historical average
- Flag categories where the user is on pace to significantly overspend                                                                                                                                                                                                                                    

IMPORTANT: Every object MUST include ALL fields listed above. 
Never omit any field. Use null only for alert field in predictions.

Return ONLY a valid JSON array of predictions for each category. No explanation, no markdown, no backticks.
Each object must have exactly these fields:
- category: string
- currentSpend: number
- predictedAmount: number
- historicalAverage: number
- onTrack: boolean
- variance: number
- alert: string or null
""")


def calculate_spending_by_category(transactions, categories):
    spending = {}
    for t in transactions:
        category = categories.get(t.description, "Other")
        spending[category] = spending.get(category, 0) + t.amount
    return spending


def get_days_info():
    today = date.today()
    days_elapsed = today.day
    import calendar
    total_days = calendar.monthrange(today.year, today.month)[1]
    percent_elapsed = round((days_elapsed / total_days) * 100, 1)
    return days_elapsed, total_days, percent_elapsed


def safe_parse_json(content, fallback):
    try:
        cleaned = content.strip().replace("```json", "").replace("```", "")
        return json.loads(cleaned)
    except Exception:
        return fallback


async def analyze_transactions(request: AnalyzeRequest) -> AnalyzeResponse:
    # Format transactions for prompt
    transactions_text = "\n".join([
        f"{t.id}|{t.date} | {t.description} | ${t.amount}"
        for t in request.currentTransactions
    ])



    # Step 1 — Categorize
    categorize_chain = categorize_prompt | llm
    categorize_result = categorize_chain.invoke({"transactions": transactions_text})
    categories = safe_parse_json(categorize_result.content, {})

    # Step 2 — Calculate spending totals per category
    spending_by_category = calculate_spending_by_category(request.currentTransactions, categories)
    spending_text = "\n".join([f"{k}: ${v:.2f}" for k, v in spending_by_category.items()])

    # For now use current spending as historical average placeholder
    # Replace with real historical data once DB queries are added
    historical_average = calculate_historical_averages(request.historicalTransactions, categories)
    historical_text = "\n".join([f"{k}: ${v:.2f}" for k, v in historical_average.items()])

    # Step 3 — Detect overspending
    overspending_chain = overspending_prompt | llm
    overspending_result = overspending_chain.invoke({
        "current_spending": spending_text,
        "historical_average": historical_text
    })
    overspending_data = safe_parse_json(overspending_result.content, [])
    overspending = [OverspendingFlag(**item) for item in overspending_data]

    # Step 4 — Generate recommendations
    recommendations_chain = recommendations_prompt | llm
    recommendations_result = recommendations_chain.invoke({
        "spending_breakdown": spending_text,
        "overspending_flags": json.dumps(overspending_data)
    })
    recommendations_data = safe_parse_json(recommendations_result.content, [])
    recommendations = [SavingsRecommendation(**item) for item in recommendations_data]

    # Step 5 — Predict future spending
    days_elapsed, total_days, percent_elapsed = get_days_info()
    predictions_chain = predictions_prompt | llm
    predictions_result = predictions_chain.invoke({
        "current_spending": spending_text,
        "historical_averages": historical_text,
        "days_elapsed": days_elapsed,
        "total_days": total_days,
        "percent_elapsed": percent_elapsed
    })
    predictions_data = safe_parse_json(predictions_result.content, [])
    predictions = [SpendingPrediction(**item) for item in predictions_data]

    return AnalyzeResponse(
        categories=categories,
        overspending=overspending,
        recommendations=recommendations,
        predictions=predictions
    )


def calculate_historical_averages(historical_transactions, categories):
    # Group by month first
    monthly_spending = {}
    for t in historical_transactions:
        month = str(t.date)[:7]  # "2026-01"
        category = categories.get(t.description, "Other")
        if month not in monthly_spending:
            monthly_spending[month] = {}
        monthly_spending[month][category] = monthly_spending[month].get(category, 0) + t.amount

    # Average across months
    averages = {}
    all_categories = set(cat for month in monthly_spending.values() for cat in month)
    for category in all_categories:
        monthly_totals = [monthly_spending[m].get(category, 0) for m in monthly_spending]
        averages[category] = sum(monthly_totals) / len(monthly_totals)

    return averages