from backend.ai.cashflow_forecast import forecast_cashflow
from backend.ai.credit_readiness import calculate_credit_readiness
from backend.ai.inventory_demand import predict_inventory_demand
from backend.ai.assistant_chat import parse_voice_or_text, answer_financial_query

def run_tests():
    sample_txs = [
        {"amount": 500, "paymentMethod": "UPI", "product": "Masala Chai", "date": 1715000000000},
        {"amount": 800, "paymentMethod": "CASH", "product": "Bun Maska", "date": 1715000000000},
        {"amount": 600, "paymentMethod": "UPI", "product": "Chai & Samosa", "date": 1715086400000},
        {"amount": 950, "paymentMethod": "UPI", "product": "Bulk Tea", "date": 1715172800000}
    ]
    sample_exps = [
        {"amount": 200, "category": "Raw Material", "date": 1715000000000},
        {"amount": 150, "category": "Packaging", "date": 1715086400000}
    ]
    sample_inv = [
        {"product_name": "Buffalo Milk", "quantity": 3.0, "minimum_stock": 10.0, "purchase_price": 60.0},
        {"product_name": "Tea Powder", "quantity": 12.0, "minimum_stock": 5.0, "purchase_price": 320.0}
    ]

    print("Testing Cashflow Forecast...")
    forecast = forecast_cashflow(sample_txs, sample_exps)
    assert forecast["has_enough_data"] is True
    assert len(forecast["daily_breakdown"]) == 7
    print("  -> Forecast OK:", forecast["next_7_days_forecast"])

    print("Testing Credit Readiness...")
    credit = calculate_credit_readiness(sample_txs, sample_exps)
    assert credit["score"] >= 300
    print("  -> Credit Score OK:", credit["score"], credit["tier"])

    print("Testing Inventory Demand...")
    inv_pred = predict_inventory_demand(sample_inv, sample_txs)
    assert len(inv_pred["items"]) == 2
    print("  -> Inventory Demand OK:", inv_pred["summary"])

    print("Testing Voice Intent Parsing...")
    v1 = parse_voice_or_text("200 rupaye chai bechi UPI pe")
    assert v1["intent"] == "ADD_SALE"
    assert v1["amount"] == 200.0
    assert v1["payment_method"] == "UPI"

    v2 = parse_voice_or_text("500 rupaye doodh kharch hua")
    assert v2["intent"] == "ADD_EXPENSE"
    assert v2["amount"] == 500.0

    print("  -> Voice Parsing OK")

    print("Testing Assistant Chat...")
    reply = answer_financial_query("aaj ka munafa kitna hai", sample_txs, sample_exps, sample_inv)
    assert "profit" in reply.lower()
    print("  -> Assistant Q&A OK:", reply)

    print("\nALL AI MODULE TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    run_tests()
