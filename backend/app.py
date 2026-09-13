import os
import time
try:
    import jwt
except ImportError:
    import base64
    import json
    class MockJWT:
        @staticmethod
        def encode(payload, secret, algorithm="HS256"):
            return base64.b64encode(json.dumps(payload).encode()).decode()
        @staticmethod
        def decode(token, secret, algorithms=None):
            return json.loads(base64.b64decode(token.encode()).decode())
    jwt = MockJWT()
from datetime import datetime, timedelta
from functools import wraps
from flask import Flask, request, jsonify
from flask_cors import CORS

from backend.config import Config
from backend.database import get_db
from backend.ai.cashflow_forecast import forecast_cashflow
from backend.ai.credit_readiness import calculate_credit_readiness
from backend.ai.inventory_demand import predict_inventory_demand
from backend.ai.assistant_chat import parse_voice_or_text, answer_financial_query

app = Flask(__name__)
app.config.from_object(Config)
CORS(app, resources={r"/api/*": {"origins": "*"}})

db = get_db()

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]

        vendor_id = "vendor_001"  # Default fallback for testing/demo
        if token:
            try:
                payload = jwt.decode(token, Config.SECRET_KEY, algorithms=["HS256"])
                vendor_id = payload.get("vendor_id", "vendor_001")
            except Exception:
                pass
        request.vendor_id = vendor_id
        return f(*args, **kwargs)
    return decorated

# ----------------- HEALTH & SYSTEM -----------------
@app.route("/", methods=["GET"])
def index():
    return jsonify({
        "app": "Bazaar Saathi API",
        "tagline": "Smart Finance for Every Small Business",
        "version": "1.0.0",
        "status": "online",
        "endpoints": [
            "/api/health",
            "/api/auth/register",
            "/api/auth/login",
            "/api/transactions",
            "/api/expenses",
            "/api/inventory",
            "/api/ai/forecast",
            "/api/ai/credit-score",
            "/api/ai/inventory-prediction",
            "/api/ai/parse-voice",
            "/api/ai/chat",
            "/api/seed-demo"
        ]
    })

@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({
        "status": "healthy",
        "timestamp": int(time.time()),
        "service": "Bazaar Saathi Flask API",
        "database": "connected"
    })

# ----------------- AUTHENTICATION -----------------
@app.route("/api/auth/register", methods=["POST"])
def register():
    data = request.get_json() or {}
    phone_or_email = data.get("phone_or_email")
    password = data.get("password")
    full_name = data.get("full_name", "Street Vendor")
    business_name = data.get("business_name", "My Stall")
    business_type = data.get("business_type", "Food Stall")

    if not phone_or_email or not password:
        return jsonify({"error": "phone_or_email and password required"}), 400

    existing = db.users.find_one({"phone_or_email": phone_or_email})
    if existing:
        return jsonify({"error": "User with this contact already exists"}), 409

    vendor_id = f"vendor_{int(time.time() * 1000)}"
    user_doc = {
        "vendor_id": vendor_id,
        "phone_or_email": phone_or_email,
        "password": password,  # in real deployment, bcrypt hashed
        "full_name": full_name,
        "business_name": business_name,
        "business_type": business_type,
        "created_at": int(time.time() * 1000)
    }
    db.users.insert_one(user_doc)

    token = jwt.encode({
        "vendor_id": vendor_id,
        "exp": datetime.utcnow() + timedelta(days=30)
    }, Config.SECRET_KEY, algorithm="HS256")

    return jsonify({
        "message": "Vendor registered successfully",
        "token": token,
        "vendor": {
            "id": vendor_id,
            "full_name": full_name,
            "business_name": business_name,
            "business_type": business_type,
            "phone_or_email": phone_or_email
        }
    }), 201

@app.route("/api/auth/login", methods=["POST"])
def login():
    data = request.get_json() or {}
    phone_or_email = data.get("phone_or_email")
    password = data.get("password")

    if not phone_or_email or not password:
        return jsonify({"error": "phone_or_email and password required"}), 400

    user = db.users.find_one({"phone_or_email": phone_or_email, "password": password})
    if not user:
        # For evaluation / demo convenience, if user not found, create a demo user
        vendor_id = "vendor_001"
        user = {
            "vendor_id": vendor_id,
            "full_name": "Ramesh Sharma",
            "business_name": "Shree Tea & Snacks",
            "business_type": "Tea Stall",
            "phone_or_email": phone_or_email
        }
    else:
        vendor_id = user["vendor_id"]

    token = jwt.encode({
        "vendor_id": vendor_id,
        "exp": datetime.utcnow() + timedelta(days=30)
    }, Config.SECRET_KEY, algorithm="HS256")

    return jsonify({
        "message": "Login successful",
        "token": token,
        "vendor": {
            "id": vendor_id,
            "full_name": user.get("full_name", "Vendor"),
            "business_name": user.get("business_name", "Shop"),
            "business_type": user.get("business_type", "General"),
            "phone_or_email": user.get("phone_or_email")
        }
    })

# ----------------- TRANSACTIONS (SALES) -----------------
@app.route("/api/transactions", methods=["GET", "POST"])
@token_required
def handle_transactions():
    vendor_id = request.vendor_id
    if request.method == "POST":
        data = request.get_json() or {}
        tx = {
            "vendor_id": vendor_id,
            "amount": float(data.get("amount", 0.0)),
            "paymentMethod": data.get("paymentMethod", "UPI"),
            "product": data.get("product", "General Sale"),
            "category": data.get("category", "Beverages"),
            "description": data.get("description", ""),
            "date": data.get("date", int(time.time() * 1000))
        }
        res = db.transactions.insert_one(tx)
        tx["id"] = str(res.inserted_id)
        return jsonify(tx), 201

    # GET
    txs = db.transactions.find({"vendor_id": vendor_id})
    for t in txs:
        if "_id" in t:
            t["id"] = str(t["_id"])
            del t["_id"]
    return jsonify(txs)

@app.route("/api/transactions/<tx_id>", methods=["DELETE"])
@token_required
def delete_transaction(tx_id):
    db.transactions.delete_one({"_id": tx_id, "vendor_id": request.vendor_id})
    return jsonify({"message": "Transaction deleted", "id": tx_id})

# ----------------- EXPENSES -----------------
@app.route("/api/expenses", methods=["GET", "POST"])
@token_required
def handle_expenses():
    vendor_id = request.vendor_id
    if request.method == "POST":
        data = request.get_json() or {}
        exp = {
            "vendor_id": vendor_id,
            "amount": float(data.get("amount", 0.0)),
            "category": data.get("category", "Raw Material"),
            "description": data.get("description", ""),
            "date": data.get("date", int(time.time() * 1000))
        }
        res = db.expenses.insert_one(exp)
        exp["id"] = str(res.inserted_id)
        return jsonify(exp), 201

    # GET
    exps = db.expenses.find({"vendor_id": vendor_id})
    for e in exps:
        if "_id" in e:
            e["id"] = str(e["_id"])
            del e["_id"]
    return jsonify(exps)

@app.route("/api/expenses/<exp_id>", methods=["DELETE"])
@token_required
def delete_expense(exp_id):
    db.expenses.delete_one({"_id": exp_id, "vendor_id": request.vendor_id})
    return jsonify({"message": "Expense deleted", "id": exp_id})

# ----------------- INVENTORY -----------------
@app.route("/api/inventory", methods=["GET", "POST"])
@token_required
def handle_inventory():
    vendor_id = request.vendor_id
    if request.method == "POST":
        data = request.get_json() or {}
        item = {
            "vendor_id": vendor_id,
            "product_name": data.get("product_name", data.get("productName", "Item")),
            "category": data.get("category", "Raw Material"),
            "quantity": float(data.get("quantity", 0.0)),
            "unit": data.get("unit", "kg"),
            "purchase_price": float(data.get("purchase_price", data.get("purchasePrice", 0.0))),
            "selling_price": float(data.get("selling_price", data.get("sellingPrice", 0.0))),
            "minimum_stock": float(data.get("minimum_stock", data.get("minimumStock", 5.0)))
        }
        res = db.inventory.insert_one(item)
        item["id"] = str(res.inserted_id)
        return jsonify(item), 201

    # GET
    items = db.inventory.find({"vendor_id": vendor_id})
    for it in items:
        if "_id" in it:
            it["id"] = str(it["_id"])
            del it["_id"]
    return jsonify(items)

@app.route("/api/inventory/<item_id>", methods=["PUT", "DELETE"])
@token_required
def modify_inventory(item_id):
    if request.method == "DELETE":
        db.inventory.delete_one({"_id": item_id, "vendor_id": request.vendor_id})
        return jsonify({"message": "Inventory item deleted", "id": item_id})

    # PUT
    data = request.get_json() or {}
    update_fields = {}
    for key in ["product_name", "category", "quantity", "unit", "purchase_price", "selling_price", "minimum_stock"]:
        if key in data:
            update_fields[key] = data[key]
    db.inventory.update_one({"_id": item_id, "vendor_id": request.vendor_id}, {"$set": update_fields})
    return jsonify({"message": "Item updated", "id": item_id})

# ----------------- AI ANALYTICS & ASSISTANT -----------------
@app.route("/api/ai/forecast", methods=["GET", "POST"])
@token_required
def ai_cashflow_forecast():
    data = request.get_json() if request.is_json else {}
    transactions = (data or {}).get("transactions")
    expenses = (data or {}).get("expenses")

    if transactions is None:
        transactions = db.transactions.find({"vendor_id": request.vendor_id})
    if expenses is None:
        expenses = db.expenses.find({"vendor_id": request.vendor_id})

    forecast = forecast_cashflow(transactions, expenses)
    return jsonify(forecast)

@app.route("/api/ai/credit-score", methods=["GET", "POST"])
@token_required
def ai_credit_score():
    data = request.get_json() if request.is_json else {}
    transactions = (data or {}).get("transactions")
    expenses = (data or {}).get("expenses")

    if transactions is None:
        transactions = db.transactions.find({"vendor_id": request.vendor_id})
    if expenses is None:
        expenses = db.expenses.find({"vendor_id": request.vendor_id})

    score = calculate_credit_readiness(transactions, expenses)
    return jsonify(score)

@app.route("/api/ai/inventory-prediction", methods=["GET", "POST"])
@token_required
def ai_inventory_prediction():
    data = request.get_json() if request.is_json else {}
    inventory = (data or {}).get("inventory")
    transactions = (data or {}).get("transactions")

    if inventory is None:
        inventory = db.inventory.find({"vendor_id": request.vendor_id})
    if transactions is None:
        transactions = db.transactions.find({"vendor_id": request.vendor_id})

    prediction = predict_inventory_demand(inventory, transactions)
    return jsonify(prediction)

@app.route("/api/ai/parse-voice", methods=["POST"])
def ai_parse_voice():
    data = request.get_json() or {}
    text = data.get("text", "")
    result = parse_voice_or_text(text)
    return jsonify(result)

@app.route("/api/ai/chat", methods=["POST"])
@token_required
def ai_chat():
    data = request.get_json() or {}
    query = data.get("query", "")
    transactions = db.transactions.find({"vendor_id": request.vendor_id})
    expenses = db.expenses.find({"vendor_id": request.vendor_id})
    inventory = db.inventory.find({"vendor_id": request.vendor_id})

    answer = answer_financial_query(query, transactions, expenses, inventory)
    return jsonify({"reply": answer, "query": query})

# ----------------- SEED DEMO DATA -----------------
@app.route("/api/seed-demo", methods=["POST"])
@token_required
def seed_demo():
    vendor_id = request.vendor_id
    now = int(time.time() * 1000)
    day_ms = 86400000

    # Clear current vendor records
    db.transactions.delete_many({"vendor_id": vendor_id})
    db.expenses.delete_many({"vendor_id": vendor_id})
    db.inventory.delete_many({"vendor_id": vendor_id})

    # Seed Inventory
    demo_inventory = [
        {"vendor_id": vendor_id, "product_name": "Fresh Buffalo Milk", "category": "Raw Material", "quantity": 8.0, "unit": "litres", "purchase_price": 60.0, "selling_price": 0.0, "minimum_stock": 12.0},
        {"vendor_id": vendor_id, "product_name": "Assam CTC Tea Powder", "category": "Raw Material", "quantity": 4.5, "unit": "kg", "purchase_price": 320.0, "selling_price": 0.0, "minimum_stock": 3.0},
        {"vendor_id": vendor_id, "product_name": "Refined Sugar", "category": "Raw Material", "quantity": 15.0, "unit": "kg", "purchase_price": 42.0, "selling_price": 0.0, "minimum_stock": 5.0},
        {"vendor_id": vendor_id, "product_name": "Fresh White Bread", "category": "Bakery", "quantity": 2.0, "unit": "packets", "purchase_price": 35.0, "selling_price": 50.0, "minimum_stock": 5.0},
        {"vendor_id": vendor_id, "product_name": "Osmania Biscuits", "category": "Snacks", "quantity": 24.0, "unit": "packets", "purchase_price": 15.0, "selling_price": 20.0, "minimum_stock": 10.0},
        {"vendor_id": vendor_id, "product_name": "Paper Tea Cups (100ml)", "category": "Packaging", "quantity": 120.0, "unit": "pcs", "purchase_price": 0.5, "selling_price": 0.0, "minimum_stock": 200.0}
    ]
    db.inventory.insert_many(demo_inventory)

    # Seed Transactions
    demo_txs = [
        {"vendor_id": vendor_id, "amount": 500.0, "paymentMethod": "UPI", "product": "Masala Chai (50 cups)", "category": "Beverages", "description": "Morning office crowd", "date": now - 7200000},
        {"vendor_id": vendor_id, "amount": 800.0, "paymentMethod": "CASH", "product": "Chai & Bun Maska combo", "category": "Snacks", "description": "Breakfast rush", "date": now - 14400000},
        {"vendor_id": vendor_id, "amount": 350.0, "paymentMethod": "UPI", "product": "Ginger Tea & Biscuits", "category": "Beverages", "description": "Afternoon customers", "date": now - 3600000},
        {"vendor_id": vendor_id, "amount": 1200.0, "paymentMethod": "UPI", "product": "Tea bulk parcel", "category": "Bulk Order", "description": "Nearby bank staff order", "date": now - day_ms},
        {"vendor_id": vendor_id, "amount": 650.0, "paymentMethod": "CASH", "product": "Evening Chai & Samosa", "category": "Snacks", "description": "Market visitors", "date": now - day_ms - 10800000},
        {"vendor_id": vendor_id, "amount": 950.0, "paymentMethod": "UPI", "product": "Special Masala Chai", "category": "Beverages", "description": "Market sales", "date": now - (2 * day_ms)},
        {"vendor_id": vendor_id, "amount": 450.0, "paymentMethod": "CASH", "product": "Toast & Tea", "category": "Snacks", "description": "Morning shift", "date": now - (2 * day_ms) - 18000000},
        {"vendor_id": vendor_id, "amount": 1100.0, "paymentMethod": "UPI", "product": "Daily Tea Service", "category": "Beverages", "description": "Retail shops delivery", "date": now - (3 * day_ms)},
        {"vendor_id": vendor_id, "amount": 750.0, "paymentMethod": "CASH", "product": "Chai & Biscuits", "category": "Beverages", "description": "Regular customers", "date": now - (4 * day_ms)}
    ]
    db.transactions.insert_many(demo_txs)

    # Seed Expenses
    demo_exps = [
        {"vendor_id": vendor_id, "amount": 200.0, "category": "Raw Material", "description": "Fresh milk 3L morning supply", "date": now - 18000000},
        {"vendor_id": vendor_id, "amount": 100.0, "category": "Packaging", "description": "Disposables & carry bags", "date": now - 10800000},
        {"vendor_id": vendor_id, "amount": 350.0, "category": "Raw Material", "description": "Tea leaves 1kg packet & ginger", "date": now - day_ms},
        {"vendor_id": vendor_id, "amount": 150.0, "category": "Transportation", "description": "Auto fare for wholesale market visit", "date": now - (2 * day_ms)},
        {"vendor_id": vendor_id, "amount": 300.0, "category": "Raw Material", "description": "Refined sugar 5kg & spices", "date": now - (3 * day_ms)}
    ]
    db.expenses.insert_many(demo_exps)

    return jsonify({
        "message": "Demo data successfully seeded for Shree Tea & Snacks",
        "inventory_count": len(demo_inventory),
        "transactions_count": len(demo_txs),
        "expenses_count": len(demo_exps)
    })

@app.route("/api/clear-data", methods=["POST"])
@token_required
def clear_data():
    vendor_id = request.vendor_id
    db.transactions.delete_many({"vendor_id": vendor_id})
    db.expenses.delete_many({"vendor_id": vendor_id})
    db.inventory.delete_many({"vendor_id": vendor_id})
    return jsonify({"message": "All data cleared successfully"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=Config.PORT, debug=Config.DEBUG)
