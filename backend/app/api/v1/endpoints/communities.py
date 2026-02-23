from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import Community as CommunityModel
from app.schemas.community import Community, CommunityCreate

router = APIRouter()

@router.get("/", response_model=List[Community])
async def read_communities(
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve communities.
    """
    result = await db.execute(select(CommunityModel).offset(skip).limit(limit))
    return result.scalars().all()

@router.post("/", response_model=Community)
async def create_community(
    *,
    db: AsyncSession = Depends(get_db),
    community_in: CommunityCreate,
    current_user = Depends(deps.get_current_active_superuser),
) -> Any:
    """
    Create new community (Superuser only).
    """
    db_obj = CommunityModel(**community_in.dict())
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    return db_obj
