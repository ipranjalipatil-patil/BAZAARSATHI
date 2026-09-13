import re
from datetime import datetime

def parse_voice_or_text(text):
    """
    Parses natural language speech or text input in English, Hindi, Marathi, or Hinglish.
    Identifies intents: ADD_SALE, ADD_EXPENSE, CHECK_PROFIT, CHECK_REVENUE, CHECK_INVENTORY.
    Extracts numerical amounts, products/categories, and payment methods.
    """
    cleaned = text.lower().strip()

    # Extract numerical amount
    amount_match = re.search(r'(?:₹|rs\.?|rupees|rupaye|rupya)?\s*(\d+(?:\.\d{1,2})?)\s*(?:₹|rs\.?|rupees|rupaye|rupya)?', cleaned)
    amount = float(amount_match.group(1)) if amount_match else None

    # Detect Payment Method
    is_upi = any(k in cleaned for k in ["upi", "gpay", "phonepe", "paytm", "online", "qr"])
    payment_method = "UPI" if is_upi else "CASH"

    # Intent Detection
    # 1. Check Profit
    if any(k in cleaned for k in ["profit", "munafa", "fayda", "kamai", "fayda kay zala", "nafa"]):
        return {
            "intent": "CHECK_PROFIT",
            "amount": None,
            "payment_method": payment_method,
            "product": None,
            "raw_text": text,
            "reply": "Checking your net profit for today."
        }

    # 2. Check Revenue / Sales
    if any(k in cleaned for k in ["total sale", "total revenue", "aaj kitna bika", "kitni bikri hui", "aaj chi bikri"]):
        return {
            "intent": "CHECK_REVENUE",
            "amount": None,
            "payment_method": payment_method,
            "product": None,
            "raw_text": text,
            "reply": "Calculating your total sales revenue for today."
        }

    # 3. Check Inventory / Stock
    if any(k in cleaned for k in ["stock", "inventory", "maal", "saman", "doodh bacha", "khatam"]):
        return {
            "intent": "CHECK_INVENTORY",
            "amount": None,
            "payment_method": payment_method,
            "product": None,
            "raw_text": text,
            "reply": "Reviewing your inventory stock levels."
        }

    # 4. Add Expense
    if any(k in cleaned for k in ["kharcha", "expense", "kharch", "bought", "khareeda", "kharidi", "bill diya"]):
        category = "Raw Material"
        if any(k in cleaned for k in ["cylinder", "gas", "bijli", "electricity", "kiraya", "rent"]):
            category = "Utilities & Rent"
        elif any(k in cleaned for k in ["tempo", "auto", "petrol", "bhada", "transport"]):
            category = "Transportation"
        elif any(k in cleaned for k in ["cup", "packet", "dabba", "packaging"]):
            category = "Packaging"

        return {
            "intent": "ADD_EXPENSE",
            "amount": amount or 100.0,
            "payment_method": payment_method,
            "category": category,
            "product": cleaned,
            "raw_text": text,
            "reply": f"Logged expense of ₹{int(amount or 100)} under {category}."
        }

    # 5. Add Sale (Default if amount found and mentions chai, coffee, becha, bikri, sale, etc.)
    if amount is not None or any(k in cleaned for k in ["becha", "bika", "sold", "sale", "order", "chai", "snack"]):
        prod = "Tea & Snacks"
        if "chai" in cleaned or "tea" in cleaned:
            prod = "Masala Chai"
        elif "samosa" in cleaned:
            prod = "Samosa"
        elif "bun maska" in cleaned:
            prod = "Bun Maska"
        elif "biscuit" in cleaned:
            prod = "Biscuits"

        return {
            "intent": "ADD_SALE",
            "amount": amount or 50.0,
            "payment_method": payment_method,
            "category": "Food & Beverage",
            "product": prod,
            "raw_text": text,
            "reply": f"Logged sale of ₹{int(amount or 50)} for {prod} via {payment_method}."
        }

    return {
        "intent": "UNKNOWN",
        "amount": None,
        "payment_method": "CASH",
        "product": None,
        "raw_text": text,
        "reply": "I couldn't identify the financial transaction. Please say something like '₹150 chai bechi cash me' or '₹200 doodh kharcha'."
    }

def answer_financial_query(query, transactions, expenses, inventory):
    """
    Answers vendor business queries in simple, street-vendor friendly language.
    """
    q = query.lower()
    total_rev = sum(float(t.get("amount", 0.0)) for t in transactions)
    total_exp = sum(float(e.get("amount", 0.0)) for e in expenses)
    net_profit = total_rev - total_exp

    if any(k in q for k in ["profit", "munafa", "fayda", "kamai", "nafa"]):
        margin = (net_profit / total_rev * 100.0) if total_rev > 0 else 0.0
        return (
            f"Your total net profit across logged records is ₹{int(net_profit):,} "
            f"with an operating profit margin of {round(margin, 1)}%."
        )

    if any(k in q for k in ["sale", "revenue", "bikri", "aavak"]):
        upi_rev = sum(float(t.get("amount", 0.0)) for t in transactions if str(t.get("paymentMethod", "")).upper() == "UPI")
        cash_rev = total_rev - upi_rev
        return (
            f"Your total sales revenue is ₹{int(total_rev):,}. "
            f"₹{int(upi_rev):,} ({round(upi_rev/max(1, total_rev)*100)}%) via UPI, "
            f"and ₹{int(cash_rev):,} via Cash."
        )

    if any(k in q for k in ["kharch", "expense", "spend"]):
        return f"Total recorded business expenses are ₹{int(total_exp):,} across raw materials and shop utilities."

    if any(k in q for k in ["stock", "inventory", "maal", "saman"]):
        low_items = [
            i.get("product_name", "Item")
            for i in inventory
            if float(i.get("quantity", 0)) <= float(i.get("minimum_stock", i.get("minimumStock", 5)))
        ]
        if low_items:
            return f"Low stock alert on {len(low_items)} items: {', '.join(low_items[:3])}. Please restock soon."
        return "All your inventory items are currently above their safety thresholds."

    if any(k in q for k in ["loan", "svanidhi", "karz", "mudra", "credit"]):
        return (
            "Under PM SVANidhi, street vendors start with a ₹10,000 collateral-free loan with a 7% interest subsidy. "
            "On timely digital repayment, you unlock Tier 2 (₹20,000) and Tier 3 (₹50,000)."
        )

    return (
        f"Namaste! You have recorded ₹{int(total_rev):,} in sales and ₹{int(total_exp):,} in expenses, "
        f"leaving ₹{int(net_profit):,} in net profit. How can I help you further?"
    )
