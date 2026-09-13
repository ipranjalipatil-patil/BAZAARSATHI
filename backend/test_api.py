import json
import unittest
from backend.app import app

class TestBazaarSaathiAPI(unittest.TestCase):
    def setUp(self):
        app.config["TESTING"] = True
        self.client = app.test_client()

    def test_health(self):
        rv = self.client.get("/api/health")
        self.assertEqual(rv.status_code, 200)
        data = json.loads(rv.data)
        self.assertEqual(data["status"], "healthy")

    def test_seed_and_insights(self):
        # Seed demo data
        rv = self.client.post("/api/seed-demo")
        self.assertEqual(rv.status_code, 200)

        # Get transactions
        rv = self.client.get("/api/transactions")
        self.assertEqual(rv.status_code, 200)
        txs = json.loads(rv.data)
        self.assertTrue(len(txs) > 0)

        # Get expenses
        rv = self.client.get("/api/expenses")
        self.assertEqual(rv.status_code, 200)
        exps = json.loads(rv.data)
        self.assertTrue(len(exps) > 0)

        # Get inventory
        rv = self.client.get("/api/inventory")
        self.assertEqual(rv.status_code, 200)
        inv = json.loads(rv.data)
        self.assertTrue(len(inv) > 0)

        # Forecast AI
        rv = self.client.post("/api/ai/forecast")
        self.assertEqual(rv.status_code, 200)
        forecast = json.loads(rv.data)
        self.assertIn("next_7_days_forecast", forecast)

        # Credit score AI
        rv = self.client.post("/api/ai/credit-score")
        self.assertEqual(rv.status_code, 200)
        score = json.loads(rv.data)
        self.assertIn("score", score)
        self.assertGreaterEqual(score["score"], 300)

        # Inventory prediction AI
        rv = self.client.post("/api/ai/inventory-prediction")
        self.assertEqual(rv.status_code, 200)
        inv_pred = json.loads(rv.data)
        self.assertIn("items", inv_pred)

        # Parse voice command
        rv = self.client.post("/api/ai/parse-voice", json={"text": "150 rupaye chai bechi UPI"})
        self.assertEqual(rv.status_code, 200)
        voice = json.loads(rv.data)
        self.assertEqual(voice["intent"], "ADD_SALE")
        self.assertEqual(voice["amount"], 150.0)

        # Chat assistant
        rv = self.client.post("/api/ai/chat", json={"query": "aaj ka munafa kitna hai?"})
        self.assertEqual(rv.status_code, 200)
        chat = json.loads(rv.data)
        self.assertIn("reply", chat)

if __name__ == "__main__":
    unittest.main()
