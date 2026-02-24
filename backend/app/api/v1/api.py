from fastapi import APIRouter
from app.api.v1.endpoints import login, users, events, websocket, clubs, communities, travel, chat, notifications, verifications, marketplace, colleges

api_router = APIRouter()
api_router.include_router(login.router, tags=["login"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(events.router, prefix="/events", tags=["events"])
api_router.include_router(clubs.router, prefix="/clubs", tags=["clubs"])
api_router.include_router(communities.router, prefix="/communities", tags=["communities"])
api_router.include_router(travel.router, prefix="/travel", tags=["travel"])
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
api_router.include_router(notifications.router, prefix="/notifications", tags=["notifications"])
api_router.include_router(verifications.router, prefix="/verifications", tags=["verifications"])
api_router.include_router(marketplace.router, prefix="/marketplace", tags=["marketplace"])
api_router.include_router(colleges.router, prefix="/colleges", tags=["colleges"])
api_router.include_router(websocket.router, tags=["websockets"])
