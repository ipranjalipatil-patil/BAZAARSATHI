import os
import uuid
import time
from backend.config import Config

class InMemoryCollection:
    """Fallback in-memory collection mimicking PyMongo collection API."""
    def __init__(self, name):
        self.name = name
        self.docs = []

    def create_index(self, keys, **kwargs):
        pass

    def insert_one(self, doc):
        new_doc = dict(doc)
        if "_id" not in new_doc:
            new_doc["_id"] = str(uuid.uuid4())
        self.docs.append(new_doc)
        class Result:
            inserted_id = new_doc["_id"]
        return Result()

    def insert_many(self, doc_list):
        inserted_ids = []
        for d in doc_list:
            res = self.insert_one(d)
            inserted_ids.append(res.inserted_id)
        class Result:
            pass
        r = Result()
        r.inserted_ids = inserted_ids
        return r

    def find(self, filter_dict=None, sort=None):
        filter_dict = filter_dict or {}
        res = []
        for d in self.docs:
            match = True
            for k, v in filter_dict.items():
                if d.get(k) != v:
                    match = False
                    break
            if match:
                res.append(dict(d))
        if sort:
            # Simple sorting support
            key, direction = sort[0]
            res.sort(key=lambda x: x.get(key, 0), reverse=(direction == -1))
        return res

    def find_one(self, filter_dict=None):
        results = self.find(filter_dict)
        return results[0] if results else None

    def update_one(self, filter_dict, update_dict):
        target = self.find_one(filter_dict)
        if target:
            idx = self.docs.index(target)
            set_vals = update_dict.get("$set", update_dict)
            self.docs[idx].update(set_vals)
            class Result:
                modified_count = 1
            return Result()
        class Result:
            modified_count = 0
        return Result()

    def delete_one(self, filter_dict):
        target = self.find_one(filter_dict)
        if target:
            self.docs.remove(target)
            class Result:
                deleted_count = 1
            return Result()
        class Result:
            deleted_count = 0
        return Result()

    def delete_many(self, filter_dict=None):
        filter_dict = filter_dict or {}
        before_len = len(self.docs)
        self.docs = [d for d in self.docs if not all(d.get(k) == v for k, v in filter_dict.items())]
        class Result:
            deleted_count = before_len - len(self.docs)
        return Result()

    def count_documents(self, filter_dict=None):
        return len(self.find(filter_dict))


class InMemoryDatabase:
    """Fallback in-memory database mimicking PyMongo Database API."""
    def __init__(self, name):
        self.name = name
        self._collections = {}

    def __getattr__(self, name):
        if name not in self._collections:
            self._collections[name] = InMemoryCollection(name)
        return self._collections[name]

    def __getitem__(self, name):
        return self.__getattr__(name)


client = None
db = None

def get_db():
    global client, db
    if db is None:
        try:
            from pymongo import MongoClient
            client = MongoClient(Config.MONGO_URI, serverSelectionTimeoutMS=2000)
            # Trigger quick server ping
            client.admin.command('ping')
            db = client[Config.DB_NAME]
            db.users.create_index("phone_or_email", unique=True)
            db.transactions.create_index([("vendor_id", 1), ("date", -1)])
            db.expenses.create_index([("vendor_id", 1), ("date", -1)])
            db.inventory.create_index([("vendor_id", 1), ("product_name", 1)])
            print("[Info] Successfully connected to MongoDB Atlas!")
        except Exception as e:
            print(f"[Notice] MongoDB Atlas unreachable: {e}. Activating high-speed in-memory database engine.")
            db = InMemoryDatabase(Config.DB_NAME)
    return db
