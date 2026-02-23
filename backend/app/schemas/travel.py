from typing import Optional
from pydantic import BaseModel
from datetime import datetime

# Shared properties
class TravelPlanBase(BaseModel):
    destination: Optional[str] = None
    date_time: Optional[datetime] = None
    mode: Optional[str] = None
    seats_available: Optional[int] = 1

# Properties to receive on creation
class TravelPlanCreate(TravelPlanBase):
    destination: str
    date_time: datetime

# Properties to receive on update
class TravelPlanUpdate(TravelPlanBase):
    pass

# Properties shared by models stored in DB
class TravelPlanInDBBase(TravelPlanBase):
    id: int
    organizer_id: int

    class Config:
        from_attributes = True

# Properties to return to client
class TravelPlan(TravelPlanInDBBase):
    pass
