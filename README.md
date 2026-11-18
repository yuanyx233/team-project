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

> **PROPOSAL — scope reduction (draft for team review)**
>
> Our previous scope (academics, housing, food and work, NPCs, and email) was too large. This draft keeps two
> themes, **academics** (main focus) and **daily life and saving**, and splits them into **four user stories, one
> per member**, as the course requires ("one user story per team member"). The rubric gives ≤ 1/5 for scope if
> there are fewer user stories than members. A fifth story, the **campus map (US5)**, is shared by everyone.
>
> - Removed: food and work (old US3), NPCs and email (old US4.1/4.2), and housing choice (old UC2.2, moved to
>   *Future Extensions*). The removed text is still in git history.
> - Each section has a **Note for the lead** box with suggestions. The lead of each section decides the final
>   wording. Please delete the note boxes once your section is done.
> - Only US4 has a lead so far. The leads for US1–US3 are **TBA**.
> - **Time budget:** We have about 2–3 weeks of part-time work, plus the campus map. Each story is split into
>   **MVP** (must work for the demo) and **Stretch** (only if time allows). With AI-assisted coding, writing code
>   is not the bottleneck. Understanding our own code for Q&A, and integrating five parts, are.

## Team Information
### Team Members
- Yuxiang Yuan
- Xinjia Shen
- Zhenbin An
- Lanxin Fu

---
## Project Description
### Domain
University life simulation, presented as a casual top-down game

**Stressdew Valley** is a casual simulation game about a term at university. The player chooses a major and
courses, attends classes, writes exams, and manages limited time and Energy from day to day, with real Toronto
weather.

**Why we are building it:** When students plan their next term, it is hard to imagine what a course load will
actually feel like until they are already living it. Stressdew Valley lets any student try out a term before
committing to it: pick a set of courses, play through the term, and see how the workload affects their daily
Energy, their exam results, and their GPA. Along the way, the game shows that steady class attendance pays off at
exam time and that time and energy need to be planned. If a term does not go well, the player can start over with
a different plan. There is no real-world cost.

> **Note for the team:** This is a proposed motivation. The TA said the project should not be *just* a game, so
> each claim above maps to a feature: course load → US1, steady attendance → US2, exam results and GPA → US3,
> daily time and Energy → US4. Our course catalogue is predefined, so we should say "a similar course load" rather
> than "your real next term". Importing real courses can be a future extension.

---
## User Stories
### User Story 1 — Choose a Major and Courses
As a student player, I want to choose my major and enrol in courses for the term so that I have a timetable that
shapes my school days.

Responsible team member: *TBA*

> **Note for the lead:** On its own, choosing courses may look small next to the other stories. The rubric wants
> each story to be *significant*. Our motivation ("try out a term before committing") also depends on this story,
> so courses need to differ from each other.
> - **MVP:** a small course catalogue where each course has a difficulty and workload; enrolment rules (course
>   limit, timetable conflicts, required courses for the major); viewing the timetable (classes and exam days
>   come from it).
> - **Stretch:** dropping or swapping a course during the term.
>
> This story owns the shared academic data (majors, courses, timetable), so please agree on it early with the
> US2 and US3 leads.

### User Story 2 — Attend Classes
As a student player, I want to attend my scheduled classes and answer practice questions so that I can build up my
understanding of each course before its exam.

Responsible team member: *TBA*

> **Note for the lead:** The key design point is how this story differs from US3. Suggested split: attending class
> is *practice*. It changes course progress but **never** changes grades or GPA.
> - **MVP:** attend a scheduled class, answer practice questions, gain course progress, spend Energy. Questions
>   come through a question source shared with US3: start with a bundled question bank, then connect the trivia
>   API (see *API Usage*).
> - **Stretch:** consequences for missing a class; class material before the question.
>
> Things to decide: what "progress" means (classes attended, correct answers, a mastery score), since US3 reads it;
> and how a course maps to a question category. Agree on the shared question source with the US3 lead in week 1.

### User Story 3 — Write Exams and Track GPA
As a student player, I want to write exams for my courses and see my grades and GPA so that I know how well I am
doing academically.

Responsible team member: *TBA*

> **Note for the lead:** This story is *assessment*: it is the only one that changes grades and GPA.
> - **MVP:** write an exam on exam day, with questions from the question source shared with US2; calculate the
>   course grade; update GPA; show the result.
> - **Stretch:** a transcript screen; a midterm.
>
> Things to decide: the grading rule (exam score only, or combined with progress from US2?); the GPA scale (for
> example, the U of T 4.0 scale); and what happens if the player misses an exam day (exam days come from the
> calendar in US4).

### User Story 4 — Live Day by Day and Keep My Progress
As a player, I want each day to pass with real-world weather and have my progress saved so that my university
life feels continuous and I can pick up where I left off.

Responsible team member: Yuxiang Yuan

> **Note for the lead:** With housing removed, every player lives in residence, so there is no commute cost and no
> rent.
> - **MVP:** sleep to start the next day and restore Energy; real weather that affects play (walking outside in
>   rain or snow costs extra Energy, see US5), so the weather API matters to gameplay rather than being
>   display-only; auto-save and load.
> - **Stretch:** A/B days; the weekend card; multiple terms and an ending.

### User Story 5 — Explore the Campus Map
As a player, I want to move around a campus map and visit different buildings so that I can choose where to spend
my time each day.

Responsible team member: Shared (all members)

> **Note for the team:** The map connects every story: each story has its own building, and the player opens that
> story's screens from it.
> - **MVP:** a clickable campus map (UC5.1).
> - **Stretch:** a character that walks on the map.
>
> Proposal for sharing the work: each lead builds their own building and its screens. One member should
> coordinate the map itself (the main window, the layout, and UC5.1) so that the pieces fit together.

---
## Use Cases
### Use Case 1.1 — Choose a major and enrol in courses (US1)
**Primary actor:** student player

**Preconditions:**
- A game has started
- The player has not finalized their academic choices for the current semester
- The game has a predefined catalogue of majors and courses

**Trigger:** The player asks to set up their academic plan

**Success outcome:** The selected major and courses are recorded, and the player can view their confirmed academic plan.

**Main success scenario:**
1. The system displays the available majors.
2. The player selects a major.
3. The system displays the courses available for that major and the permitted number of selections.
4. The player selects courses and submits their academic plan.
5. The system checks that the selections meet the enrolment rules.
6. The system records the major and selected courses.
7. The system displays the confirmed academic plan.

**Extensions:**
- 4a. The player cancels before confirming.
  The system ends the selection process without changing the recorded academic plan.
- 5a. The selections do not meet the enrolment rules.
  The system explains what must change and allows the player to revise their selections. No invalid plan is recorded

> **Note for the lead:** If you add rules from the US1 note (required courses, conflicts, drop a course), list
> each one as an extension under step 5, or add a second use case (for example, UC1.2 Drop a course).

### Use Case 2.1 — Attend a class (US2)
**Primary actor:** Student player

**Preconditions:**
- The player is enrolled in a course.
- The current semester is in progress.
- The game has class content available for that course.

**Trigger:** The player asks to attend a class for an enrolled course.

**Success outcome:** The class is recorded as completed, and the player sees their participation result.

**Main success scenario:**
1. The player requests to attend a class for an enrolled course.
2. The system checks that the class is available and has not already been completed.
3. The system presents the class material and a short practice question.
4. The player submits an answer.
5. The system evaluates the answer and displays feedback.
6. The system records the class as completed.
7. The system displays the updated course progress.

**Extensions:**
- 2a. The class is unavailable or already completed.
  The system explains why it cannot be attended. Course progress remains unchanged.
- 4a. The player submits no answer.
  The system asks the player to provide an answer and continues the class.
- 4b. The player leaves before submitting.
  The class is not recorded as completed, and course progress remains unchanged.
- 5a. The answer is incorrect.
  The system explains the correct answer. The class is still recorded as completed, with the actual participation result.

> **Note for the lead:** This draft was written by Lanxin when US1 covered all academics. Please review it and
> make it yours. Possible additions: an extension for when questions cannot be fetched (fallback questions),
> and the time or Energy cost of attending class.

### Use Case 3.1 — Take a course exam (US3)
**Primary actor:** Student player

**Preconditions:**
- The player is enrolled in the selected course.
- The game has exam questions and a grading rule for that course.
- The player has an academic record that can store course results.

**Trigger:** The player asks to take the course exam.

**Success outcome:** The exam score and corresponding course grade are recorded, the GPA is recalculated, and the player sees the results.

**Main success scenario:**
1. The player requests to take an exam for an enrolled course.
2. The system checks that the exam is available and has not already been completed.
3. The system explains the exam rules and presents the questions.
4. The player answers the questions and submits the exam.
5. The system checks that all required questions have answers.
6. The system evaluates the answers and calculates the exam score.
7. The system determines the course grade using the agreed grading rule.
8. The system records the result and recalculates the player’s GPA.
9. The system displays the exam score, course grade, and updated GPA.

**Extensions:**
- 2a. The exam is unavailable or already completed.
  The system explains why the exam cannot be started. Grades and GPA remain unchanged.
- 5a. Required answers are missing.
  The system identifies the unanswered questions and allows the player to complete them before resubmitting.
- 4a. The player cancels before submission.
  Under the proposed MVP rule, the attempt is discarded. No grade is recorded, and GPA remains unchanged.
- 8a. The system cannot record the result.
  The system reports that finalization failed and allows a retry without awarding the result twice.

> **Note for the lead:** This draft was also written by Lanxin. Please review it. Extensions are usually listed in
> step order (4a currently comes after 5a). Consider a second use case such as *View transcript and GPA*.

### Use Case 4.1 — End the day (US4)
**Primary actor:** Player

**Preconditions:**
- A game is in progress
- The player is in their residence room.

**Trigger:** The player asks to sleep.

**Success outcome:** The game moves to the next day with the correct day type, real historical weather, and updated stats,
and progress is saved.

**Main success scenario:**
1. The player asks to sleep.
2. The system checks that the player is in their residence room.
3. The system advances the calendar to the next day and determines whether it is an A day or a B day.
4. The system restores the player’s Energy according to when the player went to sleep.
5. The system retrieves the historical weather for the real date that corresponds to the new day and converts it into a
game condition: clear, cloudy, rain, or snow.
6. The system saves the game.
7. The system shows the player waking up on the new day, with the day number, day type, weather, any exam scheduled
today, and updated stats.

**Extensions:**
- 2a. The player is not in their residence room.
    1. The system explains that the player must go home to sleep.
    2. The use case ends and the day does not change.
- 3a. The day that just ended was the last day of a week, but not of the term.
    1. The system shows the weekend card, and the player chooses an activity.
    2. The system applies the activity’s effect, and the use case continues at step 3.
- 3b. The day that just ended was the last day of the term.
    1. The system shows the break card, or the ending if this was the final term.
    2. The system starts the next term on day 1, and the use case continues at step 4.
- 5a. The weather source is unavailable.
    1. The system uses its bundled copy of the same historical weather data.
    2. The use case continues at step 6.
- 6a. The system cannot save the game.
    1. The system explains that progress was not saved.
    2. The use case continues at step 7.

> **Note for the lead:** The commute step and rent were removed with housing. Once the weather effect is decided,
> add it back as a step after step 5.

### Use Case 4.2 — Load a saved game (US4)
**Primary actor:** Player

**Preconditions:**
- The application is running and the title screen is displayed.

**Trigger:** The player asks to continue a saved game.

**Success outcome:** The game is restored exactly as it was saved, and the player is at home.

**Main success scenario:**
1. The player asks to continue.
2. The system finds the save file.
3. The system reads the save file and checks that it is valid.
4. The system restores the game state: player profile, stats, major and courses, course progress, grades, calendar,
and the term’s stored weather.
5. The system shows the player at home on the saved day.

**Extensions:**
- 2a. No save file exists.
    1. The system explains that there is no saved game and offers to start a new game.
    2. The use case ends.
- 3a. The save file is damaged or comes from an unsupported version.
    1. The system explains that the save cannot be loaded.
    2. The system does not change the file, and the use case ends.

### Use Case 5.1 — Go to a campus location (US5)
**Primary actor:** Player

**Preconditions:**
- A game is in progress.
- The campus map is displayed.

**Trigger:** The player selects a building on the campus map.

**Success outcome:** The player is at the selected building and sees what they can do there.

**Main success scenario:**
1. The player selects a building on the map.
2. The system checks that the building is open in the current time slot.
3. The system moves the player to the building and applies the Energy cost of walking in today's weather.
4. The system shows the building and the actions available there.

**Extensions:**
- 2a. The building is closed.
    1. The system shows the building's opening hours.
    2. The player stays where they are, and the use case ends.
- 3a. The player does not have enough Energy to walk there.
    1. The system explains that the player is too tired and suggests going home to sleep.
    2. The player stays where they are, and the use case ends.

> **Note for the team (proposal):** Each story gets one building on the map, for example: Residence (US4, sleep),
> Registrar's Office (US1, choose courses), Lecture Hall (US2, attend class), and Exam Centre (US3, write exams).
> Building names and the map layout are still open.

---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|:-------------:|:--------------|
| *TBA* | UC1.1 — Choose a major and enrol in courses |
| *TBA* | UC2.1 — Attend a class |
| *TBA* | UC3.1 — Take a course exam |
| Yuxiang Yuan | UC4.1 — End the day (calendar, historical weather, auto-save); UC4.2 — Load a saved game |
| Shared | UC5.1 — Go to a campus location (map) |

---
## Entities
> **Note for the team:** The course asks for each entity's name, instance variables, and relevant methods (point
> form or UML). The academic entities below are placeholders. The US1–US3 leads should agree on them together,
> because all three stories share them.

### Course (US1, shared with US2 and US3)
*To be defined by the US1 lead.* Possible contents: course code, name, which days it meets, and the question
category it uses.

### AcademicRecord (US2 and US3)
*To be defined by the US2 and US3 leads.* Possible contents: the player's enrolled courses, the progress for each
course (written by US2), and the grades and GPA (written by US3).

### Game Calendar (US4)
Represents the current point in game time: year, term, day, and time slot (Morning, Afternoon, Evening, Late Night).
It knows whether the current day is an A day or a B day, whether it is an exam day (day 12 or day 24), and whether
the week or term has ended. Other use cases use it to spend time slots.

### DailyWeather (US4)
A value object for one game day's weather: the real date it is based on, the game condition (clear, cloudy, rain,
or snow), and the high and low temperatures. The conversion from raw weather data to a game condition is done
outside the entity, so provider-specific API data never enters the domain model.

### CampusLocation (US5)
Represents a building on the campus map: its name, the time slots when it is open, and whether walking there is
outdoors (so the weather applies). The player's current location is one of these.

### Player (Shared)
Represents the student: name, major, location, and stats (GPA, Energy). US4 restores Energy after sleep; other
stories update the remaining stats.

> **Note for the team:** Money was only used by food, work, and rent, which are now removed. Proposal: drop it.

---
## API Usage

**Planned US4 API:** [Open-Meteo](https://open-meteo.com/en/docs/historical-weather-api)

**How it will be used:** Suggestion only: real Toronto weather for each game day.

**Planned US2/US3 API:** [Open Trivia DB](https://opentdb.com/api_config.php)

**How it will be used:** Suggestion only: practice questions in class and exam questions.

**API Name:**
**Documentation:** [Link]
**How it is used in the program:**

---
## Design Diagrams
### UML Class Diagram
### Sequence Diagrams

---
## Future Extensions
- **Choose where to live:** Residence or off-campus housing, with rent per term and a weather-dependent daily
  commute (previously Use Case 2.2).
- **Import real courses:** Let students enter the courses they actually plan to take, so the preview matches their
  real next term.

---
## Development Plan (proposal)
**Week 1:** Agree on the shared entities (`Course`, `Player`, `AcademicRecord`, `GameCalendar`) and the interfaces
between stories, such as the question source shared by US2 and US3. Build the main window and the clickable map.
Each lead checks that their API can be called from Java. Set up Checkstyle.

**Week 2:** Each lead finishes their MVP use case (interactor, controller, presenter, view, and tests). Every PR is
reviewed by at least one other member.

**Week 3:** Integrate everything, and make sure saving covers every story's data. Do stretch goals if time allows.
Prepare slides and the demo; each member practises explaining the architecture of their own part.

**Working with AI coding tools:**
- The team designs entities and interfaces by hand; AI implements against them.
- Only change files in your own story. Changes to shared files (entities, main window) go in their own PR.
- A reviewer should be able to explain what a PR does before approving it.
- Write at least one test for each extension in your use case.

---
## How to Run
[Instructions for running the program.]

---
## Notes
[Any additional notes.]
