from typing import Optional
from pydantic import BaseModel
from datetime import datetime

# Shared properties
class MessageBase(BaseModel):
    content: Optional[str] = None
    channel: Optional[str] = "general"

# Properties to receive on creation
class MessageCreate(MessageBase):
    content: str

# Properties shared by models stored in DB
class MessageInDBBase(MessageBase):
    id: int
    sender_id: int
    timestamp: datetime

    class Config:
        from_attributes = True

# Properties to return to client
class Message(MessageInDBBase):
    pass
