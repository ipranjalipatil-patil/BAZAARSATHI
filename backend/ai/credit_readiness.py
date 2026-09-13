from datetime import datetime

def calculate_credit_readiness(transactions, expenses):
    """
    Alternative Credit Readiness & Underwriting Engine for Street Vendors.
    Generates a score between 300 and 850, tailored for Micro-Credit
    such as PM SVANidhi and MUDRA Shishu loans.
    """
    if not transactions:
        return {
            "score": 350,
            "category": "Incomplete Data",
            "tier": "Not Eligible Yet",
            "max_loan_limit": 0,
            "factors": {
                "consistency": 10,
                "revenue_volume": 10,
                "profit_margin": 10,
                "digital_adoption": 0
            },
            "eligible_schemes": [],
            "recommendations": [
                "Log at least 7 days of sales transactions.",
                "Accept UPI payments to build a verifiable digital paper trail."
            ]
        }

    total_revenue = sum(float(t.get("amount", 0.0)) for t in transactions)
    total_expense = sum(float(e.get("amount", 0.0)) for e in expenses)
    net_profit = max(0.0, total_revenue - total_expense)
    profit_margin = (net_profit / total_revenue * 100.0) if total_revenue > 0 else 0.0

    # Digital UPI ratio
    upi_sales = sum(
        float(t.get("amount", 0.0))
        for t in transactions
        if str(t.get("paymentMethod", "")).upper() == "UPI"
    )
    digital_ratio = (upi_sales / total_revenue) if total_revenue > 0 else 0.0

    # Distinct active business days
    dates = set()
    for t in transactions:
        d = t.get("date")
        if isinstance(d, (int, float)):
            d_str = datetime.fromtimestamp(d / 1000.0).strftime("%Y-%m-%d")
        else:
            d_str = str(d)[:10]
        dates.add(d_str)

    active_days = len(dates)

    # Scoring weights (Scale: 300 to 850)
    # 1. Consistency (Active Days): up to 150 pts
    consistency_pts = min(150, active_days * 30)

    # 2. Digital UPI adoption: up to 130 pts
    digital_pts = int(digital_ratio * 130)

    # 3. Revenue Volume: up to 140 pts
    rev_pts = min(140, int((total_revenue / 10000.0) * 140))

    # 4. Profit Margin: up to 130 pts
    margin_pts = min(130, int((min(profit_margin, 50.0) / 50.0) * 130))

    raw_score = 300 + consistency_pts + digital_pts + rev_pts + margin_pts
    final_score = int(min(850, max(300, raw_score)))

    # Classification & Loan Limits
    if final_score >= 720:
        category = "Excellent"
        tier = "Prime Micro-Borrower"
        max_loan = 50000
        schemes = [
            {"name": "PM SVANidhi Tier 3", "amount": 50000, "interest_subsidy": "7% p.a."},
            {"name": "MUDRA Shishu Loan", "amount": 50000, "interest_subsidy": "Competitive NBFC"}
        ]
    elif final_score >= 620:
        category = "Good"
        tier = "Credit Ready"
        max_loan = 20000
        schemes = [
            {"name": "PM SVANidhi Tier 2", "amount": 20000, "interest_subsidy": "7% p.a."},
            {"name": "Local Cooperative Micro-Loan", "amount": 20000, "interest_subsidy": "Standard"}
        ]
    elif final_score >= 500:
        category = "Moderate"
        tier = "Starter Micro-Credit"
        max_loan = 10000
        schemes = [
            {"name": "PM SVANidhi Tier 1", "amount": 10000, "interest_subsidy": "7% p.a. on on-time repayment"}
        ]
    else:
        category = "Needs Improvement"
        tier = "High Risk / Building Profile"
        max_loan = 0
        schemes = []

    recommendations = []
    if digital_ratio < 0.5:
        recommendations.append("Increase UPI/QR code transactions to > 60% of total revenue.")
    if active_days < 7:
        recommendations.append("Maintain continuous daily transaction logs for at least 2 consecutive weeks.")
    if profit_margin < 25:
        recommendations.append("Reduce bulk raw material procurement costs to elevate operating margin above 25%.")
    if not recommendations:
        recommendations.append("Maintain regular digital bookkeeping to qualify for top-tier interest subsidies.")

    return {
        "score": final_score,
        "category": category,
        "tier": tier,
        "max_loan_limit": max_loan,
        "digital_adoption_pct": round(digital_ratio * 100, 1),
        "active_days": active_days,
        "profit_margin_pct": round(profit_margin, 1),
        "factors": {
            "consistency": consistency_pts,
            "digital_adoption": digital_pts,
            "revenue_volume": rev_pts,
            "profit_margin": margin_pts
        },
        "eligible_schemes": schemes,
        "recommendations": recommendations
    }
