# Team Project

![Stressdew Valley title screen](assets/stressdew-valley-title.png)

Please keep this up-to-date with information about your project throughout the term.

The readme should include information such as:
- a summary of what your application is all about
- a list of the user stories, along with who is responsible for each one
- information about the API(s) that your project uses
- screenshots or animations demonstrating current functionality

By keeping this README up-to-date,
your team will find it easier to prepare for the final presentation
at the end of the term.

# Stressdew Valley

## Team Information
### Team Members
- Yuxiang Yuan
- Xinjia Shen
- Zhenbin An
- Lanxin Fu

---
## Project Description
### Domain
University life simulation, presented as a casual campus game.

**Stressdew Valley** is a casual game set along a simplified St. George Street. 
Players choose a major, receive its predefined courses, complete class challenges, take exams, and rest to start a new day with real Toronto weather, restore energy,
and save their progress.

**Why we are building it:** The project’s purpose is entertainment. It offers a simplified, fictional university experience rather than predicting real course
workloads or academic performance.

**Supported course content:** UC1 owns scraping the UofT major catalogue for display. Only the following
three majors are playable; other scraped majors must not assign unsupported courses.

| Playable major | Assigned courses |
|---|---|
| CS | Programming Foundation; Software Design |
| Biology | Cell Biology; Ecology |
| History | World History; Historical Perspective |

---
## User Stories
### US1 — Choose a Major
As a student player, I want to choose my major so that I can personalize my university experience and receive its predefined courses.

- **Building:** Registrar’s Office (Sidney Smith)
- **Responsible team member:** Lanxin Fu
- **MVP:** Choose one predefined major, view its attached courses, and confirm the selection. The system automatically assigns those courses.
- **Outside MVP:** Individual course selection, changing majors after confirmation, prerequisites, and timetable conflicts.

### US2 — Complete a Class Challenge
As a student player, I want to complete a class challenge for a course attached to my major so that I can earn preparation points before its exam.

- **Building:** Lecture Hall (Bahen Centre)
- **Responsible team member:** Xinjia Shen
- **MVP:** Complete one short challenge per assigned course, receive immediate feedback, spend energy, and earn preparation points.
- **Outside MVP:** Repeated challenges for additional rewards and scheduled classes.

### US3 — Fight the Final Monster
As a student player, I want to answer course questions in a Boss battle and use skills unlocked by class preparation so that I can defeat the Final Monster and earn a course grade.

- **Building:** Exam Centre
- **Responsible team member:** Zhenbin An
- **MVP:** Fight one exam Boss per assigned course using Hugging Face questions. Correct answers attack the Boss; wrong answers hurt the player. Preparation points unlock Hint and Shield. Both wins and losses produce a recorded grade.
- **Outside MVP:** Exam dates, timers, retakes, and GPA calculation.

### US4 — Rest, Start a New Day, and Save Progress
As a student player, I want to rest at my residence to start a new day with real-world weather and have my progress saved so that I can recover energy and continue later.

- **Building:** Innis Residence
- **Responsible team member:** Yuxiang Yuan
- **MVP:** Rest to start the next day, show that day's real Toronto weather, restore energy according to the weather, save completed progress, and continue from the saved checkpoint when reopening the game.
- **Outside MVP:** A term length with an end-of-term summary, weekend events, sleep schedules, and saving unfinished challenges or exams.

---
## Use Cases

### UC1 — Choose a Major

**Supports:** US1  
**Location:** Registrar’s Office  
**Primary actor:** Student player

**Preconditions:**
- A game has started.
- The player has not confirmed a major.
- A predefined catalogue of majors and their attached courses is available.

**Trigger:** The player requests to choose a major.

**Success outcome:** The selected major and its attached courses are assigned to the player.

**Main success scenario:**
1. The system displays the available majors and their descriptions.
2. The player selects a major.
3. The system displays the courses attached to that major.
4. The player confirms the selection.
5. The system records the major and automatically assigns its courses.
6. The system displays the confirmed major and assigned courses.

**Extensions:**
- **4a — The player cancels:** No major or courses are assigned.
- **5a — The selected major is unavailable:** The system explains the problem and allows another selection without changing the player's academic plan.

**MVP boundary:** Courses are predefined by major and cannot be selected individually. Changing majors after confirmation is outside the MVP.

---

### UC2 — Complete a Class Challenge

**Supports:** US2  
**Location:** Lecture Hall  
**Primary actor:** Student player

**Preconditions:**
- The player has confirmed a major and received its attached courses.
- Class questions are available for the assigned courses.

**Trigger:** The player requests a class challenge.

**Success outcome:** The challenge is recorded as completed, preparation points are awarded, and energy is deducted once.

**Main success scenario:**
1. The system displays the player's assigned courses and class-challenge completion status.
2. The player selects a course.
3. The system checks that its challenge is unfinished and the player has sufficient energy.
4. The system explains the challenge and its energy cost.
5. The player confirms participation.
6. The system loads and validates three API questions, then deducts 20 energy once and presents the challenge.
7. The player answers each question and receives immediate feedback.
8. The system records completion and awards preparation points based on correct answers.
9. The system displays the challenge result and updated energy.

**Extensions:**
- **3a — The challenge is already completed:** The system displays the previous result without deducting energy or awarding additional points.
- **3b — Insufficient energy:** The system suggests resting. The challenge does not start and no energy is deducted.
- **5a — The player cancels before starting:** Nothing changes.
- **6a — Questions cannot be loaded:** The question gateway throws an error and the screen explains the failure. No energy is deducted and no challenge starts. The player may retry; there is no local question bank.
- **7a — No answer is selected:** The system requests an answer without advancing.
- **7b — The player leaves early:** The system confirms cancellation. No completion or preparation points are recorded; spent energy is not refunded.

**MVP boundary:** One class challenge per assigned course. Preparation points do not directly change course grades.

**UC2 rules:** Each correct answer earns one preparation point. Points are recorded only after all
three answers and final confirmation. Completed results can be viewed without charging or rewarding again.

---

### UC3 — Fight the Final Monster

**Supports:** US3  
**Location:** Exam Centre  
**Primary actor:** Student player

**Preconditions:** A major is confirmed, the course is assigned, and no other activity is running.

**Main flow:**
1. The player selects a course and reads the rules and available skills.
2. On confirmation, the system loads five distinct four-option questions from Hugging Face.
3. If loading succeeds, the system checks energy again and spends 40 Energy once.
4. The player and Final Monster start with 60 HP. A correct answer deals 20 damage to the Boss;
   an incorrect answer deals 20 damage to the player.
5. The player may use Hint with at least one preparation point, or Shield with at least two.
   Each skill can be used once per battle, without spending points. Hint removes one wrong option;
   Shield blocks the next wrong answer's damage, but the answer is still counted as wrong.
6. The battle ends when either HP reaches zero or five questions have been answered.
   The player wins only if the Boss reaches zero HP.
7. The system records the result before allowing the player to leave the final feedback screen.
   Score = correct answers / actual answers submitted * 100. Grades: A >= 80, B >= 70,
   C >= 60, D >= 50, otherwise F.

**Alternatives:**
- Completed course: show its previous result, without another charge or retake.
- No major, unassigned course, insufficient energy or another active activity: explain the problem and do not start.
- API failure or invalid questions: do not deduct energy; allow retry. Questions require an internet connection.
- No answer selected: remain on the same question without damage.
- Leave before starting: no change. Leave an unfinished battle: confirm, keep the energy cost and record no grade.
- Duplicate or stale clicks: do not apply an answer or skill twice.
- A win or loss is final for that course. Unfinished battles are not saved.

---

### UC4 — Rest and Start a New Day

**Supports:** US4  
**Location:** Innis Residence  
**Primary actor:** Student player

**Preconditions:**
- A game is active.
- No class challenge or exam is in progress.

**Trigger:** The player requests to rest.

**Success outcome:** The game moves to the next day with real weather, energy is restored according to that weather, and completed progress is saved as a checkpoint.

**Main success scenario:**
1. The player requests to rest.
2. The system explains that resting ends the day, restores energy based on the next day's weather, and saves completed progress.
3. The player confirms.
4. The system advances to the next day.
5. The system retrieves the historical Toronto weather for the real date matched to the new day and converts it into a game condition: clear, cloudy, rain, or snow.
6. The system restores energy according to the weather condition.
7. The system saves the selected major, assigned courses, completed challenges, preparation points, exam results, energy, current day, and the day's weather.
8. The system shows the new day: day number, weather, and restored energy.

**Extensions:**
- **3a — The player cancels:** Nothing changes.
- **5a — The weather source is unavailable:** The system uses its bundled copy of the same historical weather data and continues at step 6.
- **6a — Energy is already above the weather-based level:** The system keeps the current energy and continues to save the checkpoint.
- **7a — Saving fails:** The new day and restored energy remain for the current session. The system reports that progress was not saved and offers a save retry.

**MVP boundary:** Rest is immediate, with no real-time waiting. The day number and weather only affect energy restored at Residence; they do not affect other buildings. There is no term calendar, exam date, or class schedule. Residence is always accessible at zero energy.

**Rule to finalize:** The real start date matched to day 1, and the energy restored for each condition (proposed: clear 100%, cloudy 95%, rain 85%, snow 80% of maximum).

---

### UC5 — Continue a Saved Game

**Supports:** US4  
**Location:** Title screen  
**Primary actor:** Student player

**Preconditions:**
- The application is running and the title screen is displayed.

**Trigger:** The player requests to continue a saved game.

**Success outcome:** The saved checkpoint is restored and the player resumes at Residence.

**Main success scenario:**
1. The player requests to continue.
2. The system finds the saved checkpoint.
3. The system checks that the checkpoint is valid.
4. The system restores the player's major, assigned courses, completed challenges, preparation points, exam results, energy, current day, and the day's weather.
5. The system displays Residence with the restored day, weather, and progress.

**Extensions:**
- **2a — No checkpoint exists:** The system explains that no saved game is available and offers a new game.
- **3a — The checkpoint is damaged or unsupported:** The system explains that it cannot be loaded and leaves the saved file unchanged.

**MVP boundary:** Only completed progress saved at Residence is restored. Unfinished challenges and exams are not saved.

---

## Shared Campus Navigation

A small, clickable St. George Street connects the Registrar’s Office, Lecture Hall, Exam Centre, and Residence.

Selecting a building opens its activity screen. Players can return to the street; leaving an unfinished activity follows that activity's cancellation rules. Each member implements their building’s activity screen and its connection to the shared navigation.

Navigation does not consume energy, and Residence remains accessible at zero energy. Walking animation is a stretch goal. Opening hours, weather effects, and travel costs are outside the MVP.

---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|:-------------:|:--------------|
| Lanxin Fu | UC1 |
| Xinjia Shen | UC2 |
| Zhenbin An | UC3 |
| Yuxiang Yuan | UC4, UC5 |
| Shared |  |

---
## Entities

### Player
Represents the student character, their energy, and their academic record.

### Major
Represents a predefined major and its attached courses. Selecting a major automatically assigns those courses to the player.

### Course
Represents a course attached to a major, including its description and question category.

### AcademicRecord
Represents the player's confirmed major, assigned courses, and academic progress.

### CourseProgress
Tracks class completion, preparation points, and the exam result for one assigned course.

### Question
Represents an immutable multiple-choice question with a stable source ID, answer options, a zero-based
correct answer, and an optional explanation. A simple hint can eliminate one incorrect option.

### QuestionSession
Represents an active class challenge or exam, tracking questions, submitted answers, completion status, and hint usage.

### ChallengeResult
Represents a completed class challenge: correct answer count, total question count, and preparation points.
It contains no exam grade. The shared academic record and checkpoint adapter still need to store it.

### GameDay
Represents the current day number and the real date it is matched to. Resting advances it to the next day.
- **Instance variables:** `dayNumber: int`, `startDate: LocalDate` (the real date matched to day 1)
- **Methods:** `getRealDate(): LocalDate`, `next(): GameDay`

## Supporting Value Objects

### ExamResult
Represents the recorded score and course grade from a completed exam.

### DailyWeather
Represents one game day's weather: the real date, the game condition (clear, cloudy, rain, or snow), and the high and low temperatures. Converting raw API data into a game condition happens outside the entity.
- **Instance variables:** `date: LocalDate`, `condition: WeatherCondition` (`CLEAR`, `CLOUDY`, `RAIN`, or `SNOW`), `highTemperature: double`, `lowTemperature: double`
- **Methods:** `getEnergyRestorePercent(): int` (based on the condition), getters (immutable)

---
## API Usage

**US2/US3 API:** [Hugging Face Datasets API](https://huggingface.co/docs/dataset-viewer/index) with the
[MMLU dataset](https://huggingface.co/datasets/cais/mmlu)

**How it is used:** The shared `QuestionGateway` loads questions from the public Hugging Face dataset-viewer
rows endpoint. All six courses have separate practice/exam selections from introductory MMLU categories:
`high_school_computer_science`, `high_school_biology`, and `high_school_world_history`.
Practice supports up to three questions; exams support up to five to accommodate UC3's new `ExamBattle`.
Existing three-question exam requests remain supported. UC2 challenges still contain exactly three questions.

MMLU has no difficulty parameter. Curated remote row references select basic programming, software concepts,
cell biology, ecology, world events, and source interpretation. This is a basic selection, not a guaranteed
difficulty rating. MMLU also has no explanations; classroom feedback shows correctness and the source answer.

Requests have a five-second timeout. Network, HTTP and invalid-data failures throw `QuestionLoadException`.
There is no bundled question bank, offline cache or fallback. The stored row references contain no question
text or answers and cannot be used offline. Practice and exam IDs are disjoint for each course.

**Question API verification:** Real Java calls passed for all six courses in both purposes on 2026-11-23
(48 questions across 12 requests: three practice and five exam questions per course, no API key).
Normal automated tests use test-only fixtures, without network.

**Planned US4 API:** [Open-Meteo Historical Weather API](https://open-meteo.com/en/docs/historical-weather-api)

**How it will be used:** Each game day is matched to a real date, and that day's Toronto weather (clear, cloudy, rain,
or snow) is shown when the player wakes up and decides how much energy is restored. A bundled copy of the same data is
used if the API is unavailable.

**Status:** Integrated with Residence, energy recovery and checkpoint saving.

---
## Design Diagrams
### UML Class Diagram
- [UC2 architecture and interaction sequence](docs/uc2-integration.md)
- [US4 — Rest, Start a New Day, and Save Progress](docs/uml/us4-class-diagram.puml)

### Sequence Diagrams

---
## How to Run
Use JDK 11 or newer and Maven 3.9.x.

```bash
mvn clean test
mvn compile exec:java
```

In IntelliJ, open `pom.xml` as a Maven project and run `app.Main`.
Start a New Game, choose CS, Biology or History at the Registrar, and visit Lecture Hall to earn
preparation points. Enter Exam Centre to fight the Final Monster. Residence restores energy and
saves completed progress; Continue returns to the last saved day at Residence.

Each major has two fictional courses:
- CS: Programming Foundation, Software Design.
- Biology: Cell Biology, Ecology.
- History: World History, Historical Perspective.

Questions come from Hugging Face and require internet access. A failed request does not spend energy.
Checkpoint files are stored at `~/.stressdew-valley/checkpoint.json`.
Old version-1 checkpoints still load; new checkpoints also save the major, class results and exam results.

See [UC2 integration notes](docs/uc2-integration.md) for its standalone test preview and API check,
and [UC3 integration notes](docs/uc3-integration.md) for the Boss battle wiring.

---
## Notes
[Any additional notes.]
