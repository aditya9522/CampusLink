# College Event Backend

Complete FastAPI backend for the College Event Application.

## Features
- **FastAPI**: Modern, fast web framework for building APIs with Python 3.7+.
- **PostgreSQL**: Robust relational database for events and user data.
- **JWT Authentication**: Secure user authentication and authorization.
- **WebSockets**: Real-time notifications and updates.
- **Redis**: Fast caching and real-time data storage.
- **Alembic**: Database migrations management.
- **Docker**: Containerized environment for easy deployment.

## Project Structure
```text
backend/
├── app/
│   ├── api/          # API endpoints (v1)
│   ├── core/         # Configuration and security
│   ├── db/           # Database session and base
│   ├── models/       # SQLAlchemy models
│   ├── schemas/      # Pydantic models (data validation)
│   ├── services/     # Business logic
│   ├── websockets/   # WebSocket connection manager
│   └── main.py       # Application entry point
├── migrations/       # Database migrations
├── tests/            # Unit and integration tests
├── .env              # Environment variables
├── docker-compose.yml # Docker services
└── Dockerfile        # App container definition
```

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Python 3.11+ (if running locally)

### Running with Docker (Recommended)
1. Start the services:
   ```bash
   docker-compose up -d
   ```
2. The API will be available at `http://localhost:8000`.
3. API Documentation (Swagger) at `http://localhost:8000/docs`.

### Running Locally
1. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # or .\venv\Scripts\activate on Windows
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the database and redis using Docker:
   ```bash
   docker-compose up -d db redis
   ```
4. Run migrations:
   ```bash
   alembic revision --autogenerate -m "Initial"
   alembic upgrade head
   ```
5. Start the server:
   ```bash
   uvicorn app.main:app --reload
   ```

## API Endpoints
- `POST /api/v1/login/access-token`: Get JWT token
- `POST /api/v1/users/`: Register new user
- `GET /api/v1/users/me`: Get current user info
- `GET /api/v1/events/`: List all events
- `POST /api/v1/events/`: Create a new event
- `WS /api/v1/ws/{token}`: WebSocket endpoint for real-time updates
