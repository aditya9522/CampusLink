from fastapi import APIRouter
from app.api.v1.endpoints import login, users, events, websocket, clubs, communities, travel, chat

api_router = APIRouter()
api_router.include_router(login.router, tags=["login"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(events.router, prefix="/events", tags=["events"])
api_router.include_router(clubs.router, prefix="/clubs", tags=["clubs"])
api_router.include_router(communities.router, prefix="/communities", tags=["communities"])
api_router.include_router(travel.router, prefix="/travel", tags=["travel"])
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
api_router.include_router(websocket.router, tags=["websockets"])
