# courtVision
A fully on-chain NBA fantasy basketball dApp. Fair, transparent, and verifiable gameplay.

## Project Structure

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.1.5
- **Database**: PostgreSQL (production) / H2 (testing)
- **Authentication**: JWT with Spring Security
- **API**: RESTful endpoints with comprehensive integration tests

### Frontend (React)
- **Framework**: React with React Router v6
- **State Management**: React Context API
- **HTTP Client**: Axios with JWT interceptor
- **UI**: Component-based architecture

## Testing

### Integration Tests (32 passed)
The project includes comprehensive integration tests covering:

#### Authentication Tests (9 tests)
- User registration with validation
- Login with JWT token generation
- Duplicate username/email prevention
- Multi-user scenarios
- Register and login workflows

#### League Management Tests (13 tests)
- Create leagues with input validation
- League name constraints (3-100 characters)
- Player count validation (2-8 players)
- Retrieve user leagues
- Update league information
- Authorization checks (creator-only)
- Multiple league scenarios

#### League Invitation Tests (10 tests)
- Invite players to leagues
- Token-based invitation workflow
- Accept invitation and join league
- Full league handling
- Complete multi-user workflows
- Pending invitation retrieval

### Running Tests
```bash
cd backend
mvn clean test
```

**Test Results:**
```
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Key Features:**
- H2 in-memory database (no PostgreSQL needed for tests)
- Real JWT token generation and validation
- Full Spring Security integration testing
- Comprehensive error scenario coverage
- Complete database isolation between tests
- Execution time: ~84 seconds

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 14+
- npm or yarn

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8080`

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive JWT token

### Leagues
- `POST /api/leagues` - Create a new league
- `GET /api/leagues` - Get all user's leagues
- `GET /api/leagues/{id}` - Get league by ID
- `PUT /api/leagues/{id}` - Update league (creator only)
- `GET /api/leagues/{id}/members` - Get league members

### Invitations
- `POST /api/leagues/{id}/invite` - Invite player to league
- `GET /api/invitations/pending` - Get pending invitations
- `POST /api/invitations/{token}/join` - Accept invitation and join league
