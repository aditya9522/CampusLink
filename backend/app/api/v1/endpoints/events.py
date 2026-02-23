from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import Event as EventModel, User as UserModel
from app.schemas.event import Event, EventCreate, EventUpdate

router = APIRouter()

@router.get("/", response_model=List[Event])
async def read_events(
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve events.
    """
    result = await db.execute(select(EventModel).offset(skip).limit(limit))
    return result.scalars().all()

@router.post("/", response_model=Event)
async def create_event(
    *,
    db: AsyncSession = Depends(get_db),
    event_in: EventCreate,
    current_user: UserModel = Depends(deps.get_current_active_user),
) -> Any:
    """
    Create new event.
    """
    db_obj = EventModel(
        **event_in.dict(),
        organizer_id=current_user.id
    )
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    return db_obj

@router.get("/{id}", response_model=Event)
async def read_event(
    *,
    db: AsyncSession = Depends(get_db),
    id: int,
) -> Any:
    """
    Get event by ID.
    """
    result = await db.execute(select(EventModel).where(EventModel.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Event not found")
    return event

@router.post("/{id}/register")
async def register_participation(
    *,
    db: AsyncSession = Depends(get_db),
    id: int,
    current_user: UserModel = Depends(deps.get_current_active_user),
) -> Any:
    """
    Register for an event.
    """
    from app.services import event_service
    return await event_service.register_for_event(db, current_user.id, id)
