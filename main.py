from fastapi import FastAPI
from dotenv import load_dotenv
from routes import analyze

load_dotenv()

app = FastAPI()

app.include_router(analyze.router, prefix="/api")

@app.get("/health")
async def health():
    return {"status": "ok"}