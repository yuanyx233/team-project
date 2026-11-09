# Team Project

Please keep this up-to-date with information about your project throughout the term.

The readme should include information such as:
- a summary of what your application is all about
- a list of the user stories, along with who is responsible for each one
- information about the API(s) that your project uses 
- screenshots or animations demonstrating current functionality

By keeping this README up-to-date,
your team will find it easier to prepare for the final presentation
at the end of the term.

# Project Name
## Team Information
### Team Members
- Yuxiang Yuan
- Xinjia Shen
- Zhenbin An
- Lanxin Fu

---
## Project Description
### Domain
[University life simulation, presented as a casual top-down game]
---
## User Stories
### User Story 1
### User Story 2
### User Story 3
### User Story 4.1 — Build Campus Relationships
As a player, I want to talk with campus characters and have them remember
my social interactions so that my relationships feel connected across campus.

Responsible team member: Xinjia Shen

### User Story 4.2 — Check Campus Email

As a student player, I want to check my in-game campus email so that I can
keep track of assignment deadlines and decide how to respond to job offers.

Responsible team member: Xinjia Shen

(Add additional user stories as needed.)
---
## Use Cases
### Use Case 1
### Use Case 2
### Use Case 3
### Use Case 4.1 — Talk with an NPC
**Primary actor:** Player

**Preconditions:**
- A game is in progress.
- The player is near an NPC.

**Trigger:** The player asks to talk with the NPC.

**Success outcome:** The player receives an NPC reply, the completed
conversation is recorded, and Friendship with that NPC increases. Later
conversations with other NPCs can refer to characters the player has met.

**Main success scenario:**
1. The player asks to talk with an NPC.
2. The system confirms that the NPC is available and shows the current
   Friendship level.
3. The player sends a message.
4. The system gives an in-character reply. The reply may refer to another
   character the player has previously talked with.
5. The system records who the player talked with and the conversation.
6. The system increases Friendship with this NPC and advances game time.
7. The system shows the reply and updated Friendship level.

**Extensions:**
- **2a. The NPC is unavailable:** The system explains this and ends the
  interaction without changing Friendship or time.
- **3a. The message is empty or too long:** The system asks for a revised
  message and returns to step 3 without changing game state.
- **4a. A suitable reply cannot be obtained:** The system provides an
  in-character fallback reply and continues at step 5.
- **4b. The player leaves before a reply is ready:** The late reply is
  ignored; no conversation is recorded and game state does not change.
- **6a. Friendship reaches the NPC's event threshold:** The system unlocks
  that NPC's event once and informs the player.

### Use Case 4.2 — Check Campus Email

**Primary actor:** Player

**Preconditions:**
- A game is in progress.
- The player has an in-game campus mailbox.

**Trigger:** The player opens the mailbox.

**Success outcome:** The player can read delivered emails. Reading a course
reminder shows the relevant assignment and due date. If the player replies
to a job offer, the result changes their in-game Money and the offer is
marked resolved.

**Main success scenario:**
1. The player opens the mailbox.
2. The system lists delivered emails and identifies unread messages.
3. The player opens a course reminder.
4. The system shows the course, assignment, and due date.
5. The system marks the reminder as read.

**Extensions:**
- **2a. The mailbox is empty:** The system shows that there is no mail.
- **3a. The player opens a job-offer email:**
    1. The system shows the offer.
    2. The player chooses to reply.
    3. The system determines whether the player gains or loses Money.
    4. The system applies the result, shows what happened, and marks the
       offer resolved so it cannot be answered again.
- **3b. The player ignores a job offer:** Money does not change and the
  offer remains unresolved.
- **3c. The player opens an already resolved offer:** The system shows the
  previous result but does not allow another reply.

(Add additional use cases as needed.)
---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Use Case |
| Name | Use Case |
| Name | Use Case |
---
## Entities
### Entity 1
### Entity 2
### Entity 3
(Add additional entities as needed.)
---
## API Usage
**API Name:**
**Documentation:** [Link]
**How it is used in the program:**
---
## Design Diagrams
### UML Class Diagram
### Sequence Diagrams
---
## How to Run
[Instructions for running the program.]
---
## Notes
[Any addi