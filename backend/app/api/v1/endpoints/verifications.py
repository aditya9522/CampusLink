from typing import Any, List, Optional
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from pathlib import Path

from app.api import deps
from app.db.session import get_db
from app.models.models import VerificationRequest as VRModel, User as UserModel
from pydantic import BaseModel
from datetime import datetime

router = APIRouter()

class VRResponse(BaseModel):
    id: int
    user_id: int
    id_card_url: str
    status: str
    admin_note: str | None
    created_at: datetime
    full_name: str | None = None

    class Config:
        from_attributes = True

@router.post("/request")
async def create_verification_request(
    *,
    db: AsyncSession = Depends(get_db),
    file: UploadFile = File(...),
    current_user: UserModel = Depends(deps.get_current_active_user),
) -> Any:
    """
    Submit a new verification request.
    """
    # Check for existing pending request
    existing = await db.execute(
        select(VRModel).where(VRModel.user_id == current_user.id, VRModel.status == "pending")
    )
    if existing.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="A pending verification request already exists.")

    # Validate file type
    ext = file.filename.split(".")[-1].lower()
    if ext not in ["jpg", "jpeg", "png", "pdf"]:
        raise HTTPException(status_code=400, detail="Invalid file type. Only JPG/PNG/PDF allowed.")

    # Save ID Card
    upload_dir = Path("static/verifications")
    upload_dir.mkdir(parents=True, exist_ok=True)
    
    file_path = upload_dir / f"id_{current_user.id}_{file.filename}"
    contents = await file.read()
    with file_path.open("wb") as buffer:
        buffer.write(contents)
        
    db_obj = VRModel(
        user_id=current_user.id,
        id_card_url=str(file_path).replace("\\", "/")
    )
    db.add(db_obj)
    await db.commit()
    return {"message": "Verification request submitted successfully"}

@router.get("/", response_model=List[VRResponse])
async def read_verification_requests(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_college_admin),
    status: str = "pending"
) -> Any:
    """
    Read verification requests.
    - Super Admin sees all requests.
    - College Admin sees only their college's student requests.
    """
    query = (
        select(VRModel, UserModel.full_name)
        .join(UserModel, VRModel.user_id == UserModel.id)
        .where(VRModel.status == status)
    )

    if not current_user.is_superuser:
        query = query.where(UserModel.college_id == current_user.college_id)

    result = await db.execute(query)
    
    requests = []
    for row in result:
        vr, full_name = row
        vr_dict = VRResponse.from_orm(vr)
        vr_dict.full_name = full_name
        requests.append(vr_dict)
        
    return requests

@router.post("/{id}/approve")
async def approve_request(
    id: int,
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_college_admin),
) -> Any:
    """Approve a verification request."""
    result = await db.execute(select(VRModel).where(VRModel.id == id))
    vr = result.scalar_one_or_none()
    if not vr:
        raise HTTPException(status_code=404, detail="Verification request not found")

    vr.status = "approved"
    db.add(vr)
    await db.commit()
    return {"message": "Approved"}

@router.post("/{id}/reject")
async def reject_request(
    id: int,
    note: Optional[str] = None,
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_college_admin),
) -> Any:
    """Reject a verification request with an optional admin note."""
    result = await db.execute(select(VRModel).where(VRModel.id == id))
    vr = result.scalar_one_or_none()
    if not vr:
        raise HTTPException(status_code=404, detail="Verification request not found")

    vr.status = "rejected"
    vr.admin_note = note
    db.add(vr)
    await db.commit()
    return {"message": "Rejected"}
