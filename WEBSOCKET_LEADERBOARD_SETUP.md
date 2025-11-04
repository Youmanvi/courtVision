# WebSocket-Based Leaderboard Setup Guide

## Overview

The courtVision application now includes a real-time leaderboard system powered by:
- **Backend**: Spring WebSocket + STOMP messaging + Kafka
- **Frontend**: React + TypeScript + STOMP client

Scores update in real-time as they're calculated, with WebSocket pushing updates to all connected clients.

---

## Backend Setup (Completed ✅)

### Files Created
- `src/main/java/com/courtvision/config/WebSocketConfig.java` - STOMP endpoint configuration
- `src/main/java/com/courtvision/controller/LeaderboardController.java` - WebSocket message handling
- `src/main/java/com/courtvision/dto/LeaderboardMessage.java` - WebSocket message format
- `src/main/java/com/courtvision/kafka/ScoreUpdateConsumer.java` - Kafka to WebSocket bridge

### Features
- ✅ STOMP over WebSocket with SockJS fallback
- ✅ CORS enabled for localhost:3000 and localhost:8080
- ✅ Real-time score broadcasting to subscribed clients
- ✅ Automatic reconnection with 5-second retry
- ✅ Integrated with existing Kafka score updates

### Endpoints
- **WebSocket**: `ws://localhost:8080/ws/scores`
- **STOMP Subscribe**: `/topic/leaderboard/{leagueId}`
- **STOMP Send**: `/app/leaderboard/subscribe/{leagueId}`

---

## Frontend Setup

### Step 1: Install WebSocket Dependencies

```bash
cd frontend
npm install sockjs-client @stomp/stompjs
npm install -D @types/sockjs-client
```

### Step 2: Files Created

```
frontend/src/
├── services/
│   └── leaderboardService.ts          # WebSocket connection & subscription management
├── components/
│   ├── Leaderboard.tsx                # Full leaderboard component
│   ├── LeaderboardRow.tsx             # Individual row component
│   └── LeaderboardWidget.tsx          # Mini widget for dashboards
└── pages/
    └── LeaderboardPage.tsx            # Full-page leaderboard route
```

### Step 3: Add Routes

In your React Router configuration, add:

```typescript
import LeaderboardPage from './pages/LeaderboardPage';

const routes = [
  // ... other routes
  {
    path: '/leagues/:leagueId/leaderboard',
    element: <LeaderboardPage />,
  },
];
```

### Step 4: Update Package.json

Ensure your `package.json` includes:

```json
{
  "dependencies": {
    "react": "^18.0.0",
    "react-router-dom": "^6.0.0",
    "sockjs-client": "^1.6.1",
    "@stomp/stompjs": "^7.0.0"
  }
}
```

---

## Usage Examples

### Full Page Leaderboard

Navigate to: `http://localhost:3000/leagues/1/leaderboard`

The `LeaderboardPage` component will:
1. Connect to WebSocket automatically
2. Fetch league information
3. Subscribe to real-time score updates
4. Display full leaderboard with stats

### Embedded Widget

In any page component:

```tsx
import LeaderboardWidget from '../components/LeaderboardWidget';
import { useNavigate } from 'react-router-dom';

export function DashboardPage() {
  const navigate = useNavigate();

  return (
    <div>
      {/* Other dashboard content */}

      <LeaderboardWidget
        leagueId={1}
        leagueName="NBA Fantasy League"
        maxRows={5}
        currentUserId={userId}
        onLeaderboardClick={() => navigate('/leagues/1/leaderboard')}
      />
    </div>
  );
}
```

### Manual WebSocket Control

```typescript
import leaderboardService from '../services/leaderboardService';

// Connect to WebSocket
await leaderboardService.connect();

// Subscribe to league leaderboard
leaderboardService.subscribeToLeaderboard(leagueId, (message) => {
  console.log('Leaderboard updated:', message);
});

// Unsubscribe
leaderboardService.unsubscribeFromLeaderboard(leagueId);

// Disconnect
leaderboardService.disconnect();
```

---

## API Contract

### LeaderboardMessage (WebSocket)

```typescript
interface LeaderboardMessage {
  league_id: number;
  league_name: string;
  scores: ScoreData[];
  timestamp: string;      // ISO 8601 datetime
  message_type: 'INITIAL_LOAD' | 'SCORES_UPDATED';
}

interface ScoreData {
  id?: number;
  league_id: number;
  league_name?: string;
  user_id: number;
  username: string;
  total_score: number;
  average_score: number;
  players_evaluated: number;
  calculated_at: string;  // ISO 8601 datetime
  created_at?: string;
}
```

---

## Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Score Calculation                        │
│  (Daily @ 2 AM UTC or via /api/scores/recalculate)          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    ScoreService                             │
│  - Calculate fantasy points from NBA stats                  │
│  - Save to ScoreCalculation table                           │
│  - Publish ScoreUpdateEvent to Kafka                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Kafka Topic                                │
│  league-scores-updated                                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              ScoreUpdateConsumer                            │
│  - Consume ScoreUpdateEvent from Kafka                      │
│  - Convert to LeaderboardMessage                            │
│  - Call LeaderboardController.broadcastScoreUpdate()        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           LeaderboardController                             │
│  - Publish to STOMP /topic/leaderboard/{leagueId}           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│      WebSocket Connected Clients                            │
│  - React components receive LeaderboardMessage              │
│  - Update UI with real-time rankings                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Features

### Leaderboard Component
- ✅ Real-time score updates via WebSocket
- ✅ Automatic reconnection on disconnect
- ✅ Current user highlighting
- ✅ Medal badges for top 3
- ✅ Loading and error states
- ✅ Connection status indicator
- ✅ Last update timestamp
- ✅ League stats (highest score, average, participants)

### LeaderboardWidget Component
- ✅ Compact size for dashboards
- ✅ Top N scores display (configurable)
- ✅ Leader highlight section
- ✅ Clickable navigation to full leaderboard
- ✅ Current user identification
- ✅ Responsive design

### LeaderboardService
- ✅ STOMP client connection management
- ✅ Multiple league subscriptions
- ✅ Automatic reconnection
- ✅ Clean disconnect/cleanup
- ✅ Type-safe message handling

---

## Configuration

### Backend (src/main/resources/application.properties)

```properties
# WebSocket is auto-configured via WebSocketConfig.java
# No additional properties needed
```

### Frontend (environment.ts or similar)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'http://localhost:8080/ws/scores',  // Used by LeaderboardService
};
```

---

## Testing

### Manual Testing

1. Start backend:
```bash
cd backend
mvn spring-boot:run
```

2. Start frontend:
```bash
cd frontend
npm start
```

3. Navigate to: `http://localhost:3000/leagues/1/leaderboard`

4. Trigger score calculation:
```bash
curl -X POST http://localhost:8080/api/scores/leagues/1/recalculate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

5. Watch leaderboard update in real-time ✨

### Automated Tests

Backend tests (49 tests, all passing):
```bash
cd backend
mvn clean test
```

---

## Troubleshooting

### WebSocket Connection Fails

**Issue**: `Failed to connect to leaderboard`

**Solutions**:
1. Verify backend is running: `http://localhost:8080/api/scores/leagues/1/scoreboard`
2. Check CORS configuration in `WebSocketConfig.java`
3. Verify WebSocket endpoint: `ws://localhost:8080/ws/scores`
4. Check browser console for detailed errors

### Scores Not Updating

**Issue**: Scores appear but don't update in real-time

**Solutions**:
1. Verify scores are being calculated: Check backend logs
2. Verify Kafka is running (if using Kafka)
3. Check that `ScoreUpdateConsumer` is being invoked
4. Verify `LeaderboardController.broadcastScoreUpdate()` is called

### High Latency

**Issue**: Updates take too long to appear

**Solutions**:
1. Check network latency to backend
2. Verify no firewall blocking WebSocket
3. Monitor Kafka consumer lag
4. Check backend CPU/memory usage

---

## Performance Considerations

- **Scalability**: Use STOMP message broker instead of simple broker for production
- **Memory**: Each subscribed client holds a connection; monitor connection count
- **Bandwidth**: Score updates are JSON; consider compression for high-frequency updates
- **Database**: Composite indexes on (league_id, user_id) optimize queries

---

## Future Enhancements

- [ ] Support for multiple subscribers per WebSocket connection
- [ ] Message history/replay functionality
- [ ] Animated score transitions
- [ ] Leaderboard filters (by position, date range)
- [ ] Export leaderboard to CSV
- [ ] Historical rankings chart
- [ ] Achievement badges system

---

## Files Checklist

### Backend
- ✅ `WebSocketConfig.java`
- ✅ `LeaderboardController.java`
- ✅ `LeaderboardMessage.java`
- ✅ `ScoreUpdateConsumer.java` (updated)

### Frontend
- ✅ `leaderboardService.ts`
- ✅ `Leaderboard.tsx`
- ✅ `LeaderboardRow.tsx`
- ✅ `LeaderboardWidget.tsx`
- ✅ `LeaderboardPage.tsx`

---

## Support

For issues or questions:
1. Check backend logs: `backend/logs/*.log`
2. Check browser console: F12 → Console tab
3. Verify all dependencies installed: `npm list`
4. Check backend compilation: `mvn clean compile`
