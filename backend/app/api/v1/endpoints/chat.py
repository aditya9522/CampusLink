from typing import Any, List
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import Message as MessageModel
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
    return result.scalars().all()
