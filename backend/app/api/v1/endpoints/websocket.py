from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends
from app.websockets.manager import manager
from app.api import deps
from app.models.models import User, Message
from app.db.session import AsyncSessionLocal
from jose import jwt
from app.core.config import settings
from datetime import datetime

router = APIRouter()

@router.websocket("/ws/{token}")
async def websocket_endpoint(websocket: WebSocket, token: str):
    # Verify token manually for websocket because it doesn't support headers easily
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        user_id = int(payload.get("sub"))
    except:
        await websocket.close(code=4003)
        return

    await manager.connect(websocket, user_id)
    try:
        while True:
            # Expecting JSON data: {"content": "...", "channel": "..."}
            data = await websocket.receive_json()
            content = data.get("content")
            channel = data.get("channel", "general")
            
            if content:
                # Save to database
                async with AsyncSessionLocal() as db:
                    new_msg = Message(
                        sender_id=user_id,
                        content=content,
                        channel=channel
                    )
                    db.add(new_msg)
                    await db.commit()
                    await db.refresh(new_msg)
                    
                    # Prepare message to broadcast
                    broadcast_data = {
                        "id": new_msg.id,
                        "sender_id": user_id,
                        "content": content,
                        "channel": channel,
                        "timestamp": new_msg.timestamp.isoformat()
                    }
                    
                    # Broadcast to all
                    await manager.broadcast_to_channel(broadcast_data, channel)
                    
    except WebSocketDisconnect:
        manager.disconnect(websocket, user_id)
    except Exception as e:
        print(f"WS Error: {e}")
        manager.disconnect(websocket, user_id)
