import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    SECRET_KEY = os.getenv("JWT_SECRET", "bazaar_saathi_production_jwt_secret_key_2026")
    MONGO_URI = os.getenv("MONGO_URI", "mongodb+srv://bazaarsaathi:ProductionPass2026@cluster0.mongodb.net/bazaar_saathi?retryWrites=true&w=majority")
    DB_NAME = os.getenv("DB_NAME", "bazaar_saathi")
    PORT = int(os.getenv("PORT", 5000))
    DEBUG = os.getenv("FLASK_ENV", "production") != "production"
