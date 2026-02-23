import asyncio
from app.db.session import AsyncSessionLocal
from app.models.models import User, Event, Club, Community, TravelPlan
from app.core.security import get_password_hash
from sqlalchemy import select
from datetime import datetime, timedelta

async def seed_db():
    async with AsyncSessionLocal() as db:
        # 1. Admin User
        result = await db.execute(select(User).where(User.email == "admin@college.edu"))
        admin = result.scalar_one_or_none()
        
        if not admin:
            admin = User(
                email="admin@college.edu",
                hashed_password=get_password_hash("admin123"),
                full_name="Campus Admin",
                is_superuser=True,
                is_active=True
            )
            db.add(admin)
            await db.commit()
            await db.refresh(admin)
            print("Admin user created")

        # 2. Sample Clubs
        clubs = [
            {"name": "Technical Club", "description": "Coding, AI, and Robotics enthusiasts", "category": "Technical"},
            {"name": "Cultural Committee", "description": "Dancing, Music, and Art events", "category": "Cultural"},
            {"name": "Sports Hub", "description": "Cricket, Football, and more", "category": "Sports"}
        ]
        for c in clubs:
            res = await db.execute(select(Club).where(Club.name == c["name"]))
            if not res.scalar_one_or_none():
                db.add(Club(**c))

        # 3. Sample Communities
        communities = [
            {"name": "Freshers Batch 2024", "description": "Official group for all new students", "member_count": 450},
            {"name": "Competitive Coding", "description": "Preparing for ACM-ICPC and Codeforces", "member_count": 120},
            {"name": "Meme Central", "description": "Campus life memes and jokes", "member_count": 890}
        ]
        for com in communities:
            res = await db.execute(select(Community).where(Community.name == com["name"]))
            if not res.scalar_one_or_none():
                db.add(Community(**com))

        # 4. Sample Events
        events = [
            {
                "title": "Hackathon 2024", 
                "description": "24-hour sprint to build awesome projects", 
                "location": "Main Auditorium",
                "start_time": datetime.now() + timedelta(days=5),
                "organizer_id": admin.id
            },
            {
                "title": "Music Night", 
                "description": "Battle of the bands and solo performances", 
                "location": "Open Theatre",
                "start_time": datetime.now() + timedelta(days=2),
                "organizer_id": admin.id
            }
        ]
        for e in events:
            res = await db.execute(select(Event).where(Event.title == e["title"]))
            if not res.scalar_one_or_none():
                db.add(Event(**e))

        await db.commit()
        print("Seed data applied successfully!")

if __name__ == "__main__":
    asyncio.run(seed_db())
