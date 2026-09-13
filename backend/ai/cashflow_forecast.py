from datetime import datetime, timedelta

def _mean(values):
    return sum(values) / len(values) if values else 0.0

def _linear_slope(y_values):
    n = len(y_values)
    if n < 2:
        return 0.0
    x_mean = (n - 1) / 2.0
    y_mean = sum(y_values) / n
    numerator = sum((i - x_mean) * (y - y_mean) for i, y in enumerate(y_values))
    denominator = sum((i - x_mean) ** 2 for i in range(n))
    return (numerator / denominator) if denominator != 0 else 0.0

def forecast_cashflow(transactions, expenses):
    """
    Moving Average + Trend Cash-Flow Prediction Model.
    Calculates next day and next 7 days net cashflow with explainability.
    Works with pure standard library or NumPy.
    """
    if not transactions and not expenses:
        return {
            "has_enough_data": False,
            "next_day_forecast": 0.0,
            "next_7_days_forecast": 0.0,
            "daily_breakdown": [],
            "explanation": "Not enough historical data to generate a reliable prediction.",
            "confidence": 0
        }

    # Group revenue by date
    daily_rev = {}
    for tx in transactions:
        d = tx.get("date")
        if isinstance(d, (int, float)):
            d_str = datetime.fromtimestamp(d / 1000.0).strftime("%Y-%m-%d")
        else:
            d_str = str(d)[:10]
        daily_rev[d_str] = daily_rev.get(d_str, 0.0) + float(tx.get("amount", 0.0))

    # Group expenses by date
    daily_exp = {}
    for exp in expenses:
        d = exp.get("date")
        if isinstance(d, (int, float)):
            d_str = datetime.fromtimestamp(d / 1000.0).strftime("%Y-%m-%d")
        else:
            d_str = str(d)[:10]
        daily_exp[d_str] = daily_exp.get(d_str, 0.0) + float(exp.get("amount", 0.0))

    all_dates = sorted(list(set(list(daily_rev.keys()) + list(daily_exp.keys()))))

    if len(all_dates) < 2:
        return {
            "has_enough_data": False,
            "next_day_forecast": 0.0,
            "next_7_days_forecast": 0.0,
            "daily_breakdown": [],
            "explanation": "At least 2 distinct days of business data are required to predict trends.",
            "confidence": 15
        }

    rev_series = [daily_rev.get(d, 0.0) for d in all_dates]
    exp_series = [daily_exp.get(d, 0.0) for d in all_dates]

    window = min(7, len(rev_series))
    recent_rev = rev_series[-window:]
    recent_exp = exp_series[-window:]

    avg_rev = float(_mean(recent_rev))
    avg_exp = float(_mean(recent_exp))

    # Calculate linear trend slope over window
    slope_rev = float(_linear_slope(recent_rev)) if window >= 3 else 0.0

    forecast_points = []
    total_7d_net = 0.0

    today = datetime.now()
    for i in range(1, 8):
        future_date = today + timedelta(days=i)
        day_name = future_date.strftime("%a")
        is_weekend = future_date.weekday() in [4, 5, 6]  # Fri, Sat, Sun
        factor = 1.15 if is_weekend else 0.95

        pred_r = max(0.0, (avg_rev + (slope_rev * i)) * factor)
        pred_e = max(0.0, avg_exp * (1.08 if is_weekend else 0.98))
        net = pred_r - pred_e
        total_7d_net += net

        forecast_points.append({
            "day": day_name,
            "date": future_date.strftime("%Y-%m-%d"),
            "expected_revenue": round(pred_r, 2),
            "expected_expense": round(pred_e, 2),
            "net_cashflow": round(net, 2)
        })

    next_day_net = forecast_points[0]["net_cashflow"] if forecast_points else (avg_rev - avg_exp)

    explanation = (
        f"Forecast derived from {window}-day moving average of ₹{int(avg_rev)} daily revenue "
        f"and ₹{int(avg_exp)} daily spend. Incorporates weekend street food uplift (+15%) and recent trajectory."
    )

    return {
        "has_enough_data": True,
        "next_day_forecast": round(next_day_net, 2),
        "next_7_days_forecast": round(total_7d_net, 2),
        "daily_breakdown": forecast_points,
        "explanation": explanation,
        "confidence": min(92, 50 + (len(all_dates) * 7))
    }
