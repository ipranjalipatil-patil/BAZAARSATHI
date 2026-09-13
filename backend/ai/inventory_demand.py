def predict_inventory_demand(inventory_items, transactions):
    """
    Inventory Stockout Predictor & Automated Reorder Recommendation Model.
    Calculates estimated run-out velocity and days of stock remaining.
    """
    results = []

    # Map transactions to count sales per product or general item
    product_sales_freq = {}
    for t in transactions:
        prod = str(t.get("product", "")).lower()
        if prod:
            product_sales_freq[prod] = product_sales_freq.get(prod, 0) + 1

    for item in inventory_items:
        name = item.get("product_name", item.get("name", "Unknown Item"))
        name_lower = name.lower()
        qty = float(item.get("quantity", 0.0))
        min_stock = float(item.get("minimum_stock", item.get("minimumStock", 5.0)))
        unit = item.get("unit", "units")
        purchase_price = float(item.get("purchase_price", item.get("purchasePrice", 0.0)))

        # Find matching sales velocity
        matched_mentions = sum(
            freq for p, freq in product_sales_freq.items()
            if any(k in p for k in name_lower.split())
        )

        daily_burn_rate = max(0.5, (matched_mentions / 5.0) if matched_mentions > 0 else 1.0)
        days_remaining = round(qty / daily_burn_rate, 1) if daily_burn_rate > 0 else 99.0

        is_critical = qty <= 0 or qty <= (min_stock * 0.5)
        is_low = qty <= min_stock and not is_critical

        if is_critical:
            status = "CRITICAL_STOCKOUT"
            alert_msg = f"{name} is critically low ({qty} {unit}). Immediate replenishment needed!"
            suggested_reorder = max(10.0, min_stock * 2 - qty)
        elif is_low:
            status = "LOW_STOCK"
            alert_msg = f"{name} stock ({qty} {unit}) is below minimum buffer ({min_stock} {unit}). Stockout in ~{days_remaining} days."
            suggested_reorder = max(5.0, min_stock * 1.5 - qty)
        else:
            status = "OPTIMAL"
            alert_msg = f"{name} stock is healthy. Approx {days_remaining} days remaining."
            suggested_reorder = 0.0

        reorder_cost = round(suggested_reorder * purchase_price, 2)

        results.append({
            "product_name": name,
            "category": item.get("category", "General"),
            "current_quantity": qty,
            "minimum_stock": min_stock,
            "unit": unit,
            "status": status,
            "days_remaining": days_remaining,
            "suggested_reorder_qty": round(suggested_reorder, 1),
            "estimated_reorder_cost": reorder_cost,
            "alert_message": alert_msg
        })

    # Sort so critical and low stock appear first
    results.sort(key=lambda x: (0 if x["status"] == "CRITICAL_STOCKOUT" else 1 if x["status"] == "LOW_STOCK" else 2, x["days_remaining"]))

    low_stock_count = sum(1 for r in results if r["status"] in ["CRITICAL_STOCKOUT", "LOW_STOCK"])

    summary = (
        f"{low_stock_count} item(s) require reordering soon."
        if low_stock_count > 0
        else "All inventory items are currently well-stocked."
    )

    return {
        "items": results,
        "low_stock_count": low_stock_count,
        "summary": summary
    }
