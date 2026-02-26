from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
import uuid
import secrets

from app.api import deps
from app.db.session import get_db
from app.models.models import College as CollegeModel, User as UserModel
from app.core import security
from pydantic import BaseModel

router = APIRouter()

class CollegeBase(BaseModel):
    name: str
    slug: str

class CollegeCreate(CollegeBase):
    pass

class CollegeResponse(CollegeBase):
    id: int
    invite_code: str
    is_active: bool

    class Config:
        from_attributes = True

@router.post("/", response_model=CollegeResponse)
async def create_college(
    *,
    db: AsyncSession = Depends(get_db),
    college_in: CollegeCreate,
    current_user: UserModel = Depends(deps.get_current_active_superuser),
) -> Any:
    """
    Create a new college (Super Admin only).
    """
    # Check if exists
    result = await db.execute(select(CollegeModel).where(CollegeModel.name == college_in.name))
    if result.scalar():
        raise HTTPException(status_code=400, detail="College already exists")
    
    invite_code = str(uuid.uuid4())[:8].upper()
    db_obj = CollegeModel(
        **college_in.dict(),
        invite_code=invite_code
    )
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    return db_obj

@router.get("/", response_model=List[CollegeResponse])
async def read_colleges(
    db: AsyncSession = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve colleges (Super Admin only).
    """
    result = await db.execute(select(CollegeModel).offset(skip).limit(limit))
    return result.scalars().all()

@router.post("/invite-admin")
async def invite_college_admin(
    *,
    db: AsyncSession = Depends(get_db),
    email: str,
    college_id: int,
    current_user: UserModel = Depends(deps.get_current_active_superuser),
) -> Any:
    """
    Invite or promote a user to College Admin.
    """
    # Find user or logic to create a placeholder
    result = await db.execute(select(UserModel).where(UserModel.email == email))
    user = result.scalar_one_or_none()
    
    if not user:
        # Create user with a secure random temporary password
        temp_password = secrets.token_urlsafe(16)
        user = UserModel(
            email=email,
            hashed_password=security.get_password_hash(temp_password),
            full_name="College Admin",
            role="college_admin",
            college_id=college_id
        )
        db.add(user)
    else:
        user.role = "college_admin"
        user.college_id = college_id
    
    await db.commit()
    return {"message": f"User {email} is now an admin for college {college_id}"}

@router.delete("/{id}")
async def delete_college(
    id: int,
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_superuser),
) -> Any:
    """
    Delete a college (Super Admin only). Cascades to all students and events.
    """
    result = await db.execute(select(CollegeModel).where(CollegeModel.id == id))
    college = result.scalar_one_or_none()
    if not college:
        raise HTTPException(status_code=404, detail="College not found")
    await db.delete(college)
    await db.commit()
    return {"message": f"College '{college.name}' deleted"}
