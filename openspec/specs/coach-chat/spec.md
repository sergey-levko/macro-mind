## ADDED Requirements

### Requirement: Chat endpoint accepts user message and returns AI reply
The system SHALL expose `POST /api/v1/chat` that accepts a user message, builds a context-enriched prompt from the user's last 7 days of meal logs, sends it to the AI model, and returns a plain-text reply.

#### Scenario: Successful chat request
- **WHEN** an authenticated user sends `POST /api/v1/chat` with `{ "message": "How is my protein intake?" }`
- **THEN** the system returns HTTP 200 with `{ "reply": "<AI-generated response>" }` within a reasonable timeout

#### Scenario: Message is blank
- **WHEN** an authenticated user sends `POST /api/v1/chat` with a blank or empty `message`
- **THEN** the system returns HTTP 400

#### Scenario: User has no meal logs
- **WHEN** an authenticated user with no logged meals sends a chat message
- **THEN** the system returns HTTP 200 with a reply that acknowledges no meal data is available

### Requirement: Chat context includes recent meal log data
The system SHALL inject a summary of the user's meal logs from the past 7 days into the system prompt for each chat request. The summary SHALL include food names and macro values (calories, protein, carbs, fat) per meal log entry.

#### Scenario: Context is injected per request
- **WHEN** a user sends a chat message and has meal logs in the last 7 days
- **THEN** the AI response reflects awareness of the user's recent eating patterns

### Requirement: Frontend chat panel sends and displays messages
The Coach page SHALL include a chat panel where users can type a message, submit it, and see the AI reply displayed below their message.

#### Scenario: User sends a message
- **WHEN** a user types a question and clicks Send (or presses Enter)
- **THEN** a loading state is shown, then the AI reply appears in the conversation area

#### Scenario: Send is disabled while waiting
- **WHEN** a chat request is in-flight
- **THEN** the send button and input are disabled until the reply arrives
