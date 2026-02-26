from typing import Any, List
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import Message as MessageModel, User
from app.schemas.chat import Message

router = APIRouter()

@router.get("/{channel}", response_model=List[Message])
async def read_messages(
    channel: str,
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve message history for a channel.
    """
    result = await db.execute(
        select(MessageModel)
        .where(MessageModel.channel == channel)
        .order_by(MessageModel.timestamp.desc())
        .offset(skip)
        .limit(limit)
    )
    messages = result.scalars().all()
    
    # Populate sender_name from relationship
    for msg in messages:
        # Since msg.sender is a relationship, we need to ensure it's loaded
        # or just fetch names separately for efficiency. 
        # For simplicity in this async setup, let's assume joined load or fetch.
        user_result = await db.execute(select(User.full_name).where(User.id == msg.sender_id))
        msg.sender_name = user_result.scalar_one_or_none() or f"User {msg.sender_id}"
        
    return messages
