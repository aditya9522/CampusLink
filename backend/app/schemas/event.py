from typing import Optional
from datetime import datetime
from pydantic import BaseModel

# Shared properties
class EventBase(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    location: Optional[str] = None
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    image_url: Optional[str] = None

# Properties to receive via API on creation
class EventCreate(EventBase):
    title: str

# Properties to receive via API on update
class EventUpdate(EventBase):
    pass

class EventInDBBase(EventBase):
    id: int
    organizer_id: int
    created_at: datetime

    class Config:
        from_attributes = True

# Additional properties to return via API
class Event(EventInDBBase):
    pass
