# courtVision
A fully on-chain NBA fantasy basketball dApp. Fair, transparent, and verifiable gameplay.

## 🌟 Latest Updates (November 2025)

### ✅ Mission-Critical Blockchain Integration Complete
- **Solana Transaction Signing** - Ed25519 cryptographic implementation
- **Transaction Confirmation Polling** - Real-time blockchain status monitoring
- **Comprehensive API Testing** - Full Postman test suite with 12 requests and 30+ assertions
- **Production-Ready Code** - All implementations compile and tested

See [DELIVERABLES.md](DELIVERABLES.md) for complete implementation details.

## Project Structure

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: PostgreSQL (production) / H2 (testing)
- **Authentication**: JWT with Spring Security
- **Blockchain**: Solana integration with transaction signing
- **Message Queue**: Apache Kafka for event-driven architecture
- **Real-time**: WebSocket with STOMP messaging
- **Scheduling**: Spring Task Scheduler for automated jobs
- **API**: RESTful endpoints with comprehensive integration tests

### Frontend (React)
- **Framework**: React 18.2.0 with React Router v6
- **Language**: TypeScript (partial) / JavaScript
- **State Management**: React Context API
- **HTTP Client**: Axios with JWT interceptor
- **Wallet Integration**: @solana/web3.js (Phantom wallet support)
- **UI**: Component-based architecture with responsive design

## Testing

### Integration Tests (39 passed)
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

#### Draft Management Tests (7 tests)
- Start draft with configurable rounds
- Draft retrieval and status checking
- Authorization checks (creator-only)
- Draft pick retrieval and history
- Default draft configuration
- Duplicate draft prevention

### Running Tests

#### Unit & Integration Tests
```bash
cd backend
mvn clean test
```

**Test Results:**
```
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Key Features:**
- H2 in-memory database (no PostgreSQL needed for tests)
- Real JWT token generation and validation
- Full Spring Security integration testing
- Comprehensive error scenario coverage
- Complete database isolation between tests
- Execution time: ~90 seconds

#### API Testing with Postman

**Quick Start:**
```bash
# Install Newman (Postman CLI runner)
npm install -g newman

# Run tests
newman run postman_collection.json -e postman_environment.json -r cli,html

# Or use the test runner script
.\run_postman_tests.ps1  # Windows
bash run_postman_tests.sh # Linux/Mac
```

**Test Coverage:**
- 12 comprehensive API test requests
- 30+ test assertions
- Automatic JWT token management
- Environment variable substitution
- Full endpoint coverage

**Test Suite Includes:**
- ✅ Authentication (Register, Login, Get User)
- ✅ League Management (Create, List, Get Details)
- ✅ Winner Management (Get League Winner, User Wins)
- ✅ NBA Players (Get All Players, Teams, Positions)

**Results:**
- HTML Report: `postman-test-report.html`
- JSON Report: `postman-test-results.json`
- Expected Success Rate: >95%
- Execution Time: ~2-3 seconds

**Full Documentation:** See [API_TESTING_README.md](API_TESTING_README.md)

## 🔗 Solana Blockchain Integration

### Features

#### Transaction Signing & Submission
- **Cryptography**: Ed25519 signing with Java built-in support
- **Encoding**: Base58 address handling (Solana standard)
- **RPC Integration**: Solana JSON-RPC endpoint communication
- **Status Tracking**: Automatic transaction confirmation monitoring

**File:** `backend/src/main/java/com/courtvision/service/SolanaOracleService.java`

#### Transaction Confirmation Polling
- **Scheduled Polling**: Every 30 seconds for pending transactions
- **Automatic Updates**: Updates transaction status to CONFIRMED/FAILED
- **Timeout Detection**: 5-minute automatic failure timeout
- **Event Publishing**: Kafka events for status changes
- **Retry Mechanism**: Manual retry capability for failed transactions

**File:** `backend/src/main/java/com/courtvision/service/TransactionConfirmationPoller.java`

### Configuration

Add to `application.properties` or environment variables:

```properties
# Solana Oracle Configuration
solana.rpc-endpoint=https://api.devnet.solana.com
solana.network=devnet
solana.oracle-program-id=YOUR_PROGRAM_ID
solana.oracle-wallet-private-key=YOUR_BASE58_PRIVATE_KEY
solana.confirmation-timeout=30
```

### API Endpoints

#### Winner Management
- `GET /api/winners/leagues/{leagueId}` - Get league winner and transaction status
- `GET /api/winners/users/{userId}` - Get user's league wins
- `GET /api/winners/pending` - Get pending blockchain transactions (ADMIN)
- `GET /api/winners/failed` - Get failed transactions (ADMIN)
- `GET /api/winners/transactions/{txHash}` - Check transaction status
- `POST /api/winners/leagues/{leagueId}/announce` - Manual announcement (ADMIN)
- `GET /api/winners/stats` - Winner statistics (ADMIN)

### Kafka Topics

```
league-winners-announced (1 partition)
├── Event: LEAGUE_WINNER_ANNOUNCED
├── Event: TRANSACTION_CONFIRMED
└── Event: TRANSACTION_FAILED
```

### Transaction Status Lifecycle

```
PENDING → SUBMITTED → CONFIRMED (success)
PENDING → SUBMITTED → FAILED (timeout or error)
FAILED → PENDING (retry)
```

### Full Documentation

- **Architecture & Implementation**: [BLOCKCHAIN_INTEGRATION_SUMMARY.md](BLOCKCHAIN_INTEGRATION_SUMMARY.md)
- **Deployment Guide**: [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)
- **File Inventory**: [DELIVERABLES.md](DELIVERABLES.md)

## Getting Started

### Prerequisites

#### Required Services
- **Java 21+** (JDK)
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Apache Kafka 7.5+**
- **Node.js 18+**
- **npm or yarn**

#### For Blockchain Testing
- **Solana Devnet RPC** (https://api.devnet.solana.com)
- **Solana CLI** (optional, for wallet management)
- **Phantom Wallet** (browser extension)

### Quick Start with Docker Compose

The easiest way to get all services running:

```bash
# Start all services (PostgreSQL, Kafka, Backend)
docker-compose up -d

# Check service status
docker-compose ps

# View backend logs
docker-compose logs -f backend

# Stop all services
docker-compose down
```

**Services Started:**
- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Backend: `localhost:8080`

Then proceed to Frontend Setup below.

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

### Drafts
- `POST /api/drafts/leagues/{leagueId}/start` - Start draft for league
- `GET /api/drafts/leagues/{leagueId}` - Get draft status
- `POST /api/drafts/{draftId}/pick` - Make a draft pick
- `GET /api/drafts/{draftId}/picks` - Get all picks in draft
- `GET /api/drafts/{draftId}/picks/user/{userId}` - Get picks by specific user
- `PUT /api/drafts/{draftId}/pause` - Pause draft (creator only)
- `PUT /api/drafts/{draftId}/resume` - Resume paused draft (creator only)

### Winners & Blockchain
- `GET /api/winners/leagues/{leagueId}` - Get league winner with transaction status
- `GET /api/winners/users/{userId}` - Get user's league wins
- `GET /api/winners/transactions/{txHash}` - Check Solana transaction status
- `POST /api/winners/leagues/{leagueId}/announce` - Announce winner (ADMIN)
- `GET /api/winners/stats` - Winner statistics (ADMIN)

### Scores & Leaderboard
- `GET /api/scores/leagues/{leagueId}/scoreboard` - Get league leaderboard
- `POST /api/scores/leagues/{leagueId}/recalculate` - Trigger score recalculation (ADMIN)

### Real-time (WebSocket)
- `ws://localhost:8080/ws/scores` - WebSocket connection
- `/topic/leaderboard/{leagueId}` - Subscribe to league leaderboard updates
- `/app/leaderboard/subscribe/{leagueId}` - Client subscription endpoint

### NBA Players
- `GET /api/players` - Get all NBA players
- `GET /api/players/{id}` - Get player details
- `GET /api/players/teams` - Get all NBA teams
- `GET /api/players/positions` - Get all player positions
- `GET /api/players/search?name=...` - Search players by name

## 📚 Documentation

Comprehensive documentation is available for all aspects of the project:

### API & Testing
- **[API_TESTING_README.md](API_TESTING_README.md)** - Complete API testing guide with Postman
  - Setup instructions (Docker & manual)
  - Multiple testing methods (GUI, CLI, runners)
  - Troubleshooting guide
  - Performance benchmarks
  - CI/CD integration examples

### Blockchain Integration
- **[BLOCKCHAIN_INTEGRATION_SUMMARY.md](BLOCKCHAIN_INTEGRATION_SUMMARY.md)** - Technical implementation details
  - Architecture overview
  - Transaction signing implementation
  - Confirmation polling details
  - Configuration guide
  - Deployment steps
  - Security considerations

### Implementation & Deployment
- **[IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)** - Project completion summary
  - Task completion details
  - Code quality metrics
  - Deployment checklist
  - Next steps

- **[TEST_SETUP_COMPLETE.md](TEST_SETUP_COMPLETE.md)** - Test infrastructure details
  - Setup verification results
  - File manifest
  - Test coverage details
  - Expected results

- **[DELIVERABLES.md](DELIVERABLES.md)** - Complete inventory of deliverables
  - All files listed with purposes
  - Status of each component
  - How to use each part
  - Statistics and metrics

### Getting Help

If you have questions about:
- **Blockchain features**: See [BLOCKCHAIN_INTEGRATION_SUMMARY.md](BLOCKCHAIN_INTEGRATION_SUMMARY.md)
- **API testing**: See [API_TESTING_README.md](API_TESTING_README.md)
- **Project setup**: See [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)
- **File locations**: See [DELIVERABLES.md](DELIVERABLES.md)

## 🤝 Contributing

Contributions are welcome! Please:
1. Create a feature branch
2. Make your changes
3. Add tests for new functionality
4. Run the full test suite
5. Submit a pull request

## 📝 License

This project is part of the CourtVision dApp initiative.

## 🔄 Current Status

**Version:** 1.0.0
**Last Updated:** November 2025
**Build Status:** ✅ SUCCESS
**Test Status:** ✅ 39/39 PASSING
**Blockchain Status:** ✅ IMPLEMENTED (Ready for Devnet)

---

**Ready to get started?** See [API_TESTING_README.md](API_TESTING_README.md) for quick start instructions!
