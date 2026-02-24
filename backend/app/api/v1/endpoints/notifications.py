from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update

from app.api import deps
from app.db.session import get_db
from app.models.models import Notification as NotificationModel, User as UserModel
from app.schemas.notification import Notification, NotificationCreate
from app.websockets.manager import manager

router = APIRouter()

@router.get("/", response_model=List[Notification])
async def read_notifications(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_user),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve notifications for the current user.
    """
    result = await db.execute(
        select(NotificationModel)
        .where((NotificationModel.user_id == current_user.id) | (NotificationModel.user_id == None))
        .order_by(NotificationModel.created_at.desc())
        .offset(skip)
        .limit(limit)
    )
    return result.scalars().all()

@router.post("/read-all")
async def mark_all_as_read(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(deps.get_current_active_user),
) -> Any:
    """
    Mark all notifications as read.
    """
    await db.execute(
        update(NotificationModel)
        .where(NotificationModel.user_id == current_user.id)
        .values(is_read=True)
    )
    await db.commit()
    return {"message": "All notifications marked as read"}

@router.post("/send", response_model=Notification)
async def send_notification(
    *,
    db: AsyncSession = Depends(get_db),
    notification_in: NotificationCreate,
    current_user: UserModel = Depends(deps.get_current_active_college_admin),
) -> Any:
    """
    Send a notification (Staff/Admin only).
    """
    db_obj = NotificationModel(**notification_in.dict())
    db.add(db_obj)
    await db.commit()
    await db.refresh(db_obj)
    
    # Broadcast via WebSocket
    broadcast_data = {
        "type": "notification",
        "id": db_obj.id,
        "title": db_obj.title,
        "message": db_obj.message,
        "notif_type": db_obj.type
    }
    
    if db_obj.user_id:
        await manager.send_personal_message(broadcast_data, db_obj.user_id)
    else:
        await manager.broadcast_to_channel(broadcast_data, "general")
        
    return db_obj
