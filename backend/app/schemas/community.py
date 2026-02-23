from typing import Optional
from pydantic import BaseModel

# Shared properties
class CommunityBase(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    member_count: Optional[int] = 0
    image_url: Optional[str] = None

# Properties to receive on creation
class CommunityCreate(CommunityBase):
    name: str

# Properties to receive on update
class CommunityUpdate(CommunityBase):
    pass

# Properties shared by models stored in DB
class CommunityInDBBase(CommunityBase):
    id: int

    class Config:
        from_attributes = True

# Properties to return to client
class Community(CommunityInDBBase):
    pass
