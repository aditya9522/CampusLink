from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.api import deps
from app.db.session import get_db
from app.models.models import TravelPlan as TravelPlanModel, User as UserModel
from app.schemas.travel import TravelPlan, TravelPlanCreate

router = APIRouter()

@router.get("/", response_model=List[TravelPlan])
async def read_travel_plans(
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve travel plans.
    """
    result = await db.execute(select(TravelPlanModel).offset(skip).limit(limit))
    return result.scalars().all()

@router.post("/", response_model=TravelPlan)
async def create_travel_plan(
    *,
    db: AsyncSession = Depends(get_db),
    plan_in: TravelPlanCreate,
    current_user: UserModel = Depends(deps.get_current_active_user),
) -> Any:
    """
    Create new travel plan.
    """
    db_obj = TravelPlanModel(
        **plan_in.dict(),
        organizer_id=current_user.id
    )
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    return db_obj
