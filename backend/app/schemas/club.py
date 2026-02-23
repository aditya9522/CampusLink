from typing import Optional
from pydantic import BaseModel
from datetime import datetime

# Shared properties
class ClubBase(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    category: Optional[str] = None
    logo_url: Optional[str] = None

# Properties to receive on creation
class ClubCreate(ClubBase):
    name: str

# Properties to receive on update
class ClubUpdate(ClubBase):
    pass

# Properties shared by models stored in DB
class ClubInDBBase(ClubBase):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True

# Properties to return to client
class Club(ClubInDBBase):
    pass
