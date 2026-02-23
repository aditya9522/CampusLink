from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import Club as ClubModel
from app.schemas.club import Club, ClubCreate

router = APIRouter()

@router.get("/", response_model=List[Club])
async def read_clubs(
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve clubs.
    """
    result = await db.execute(select(ClubModel).offset(skip).limit(limit))
    return result.scalars().all()

@router.post("/", response_model=Club)
async def create_club(
    *,
    db: AsyncSession = Depends(get_db),
    club_in: ClubCreate,
    current_user = Depends(deps.get_current_active_superuser),
) -> Any:
    """
    Create new club (Superuser only).
    """
    db_obj = ClubModel(**club_in.dict())
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    return db_obj

@router.get("/{id}", response_model=Club)
async def read_club(
    *,
    db: AsyncSession = Depends(get_db),
    id: int,
) -> Any:
    """
    Get club by ID.
    """
    result = await db.execute(select(ClubModel).where(ClubModel.id == id))
    club = result.scalar_one_or_none()
    if not club:
        raise HTTPException(status_code=404, detail="Club not found")
    return club
