from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, and_
from app.models.models import Participation, Event
from fastapi import HTTPException

async def register_for_event(db: AsyncSession, user_id: int, event_id: int):
    # Check if already registered
    result = await db.execute(
        select(Participation).where(
            and_(Participation.user_id == user_id, Participation.event_id == event_id)
        )
    )
    existing = result.scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=400, detail="Already registered for this event")
    
    participation = Participation(user_id=user_id, event_id=event_id)
    db.add(participation)
    await db.commit()
    await db.refresh(participation)
    return participation

async def get_user_participations(db: AsyncSession, user_id: int):
    result = await db.execute(
        select(Event).join(Participation).where(Participation.user_id == user_id)
    )
    return result.scalars().all()
