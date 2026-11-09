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
### As a student player, I want to choose my major and courses and attend classes and exams so that I can earn a good GPA.
### User Story 2
### User Story 3
### User Story 4
(Add additional user stories as needed.)
---
## Use Cases
### Use Case 1.1: Choose a major and enrol in courses
Primary actor: student player 

Preconditions: 
- A game has started
- The player has not finalized their academic choices for the current semester
- The game has a predefined catalogue of majors and courses

Trigger: The player asks to set up their academic plan

Success outcome: The selected major and courses are recorded, and the player can view their confirmed academic plan.

Main success scenario:
1. The system displays the available majors. 
2. The player selects a major. 
3. The system displays the courses available for that major and the permitted number of selections. 
4. The player selects courses and submits their academic plan. 
5. The system checks that the selections meet the enrolment rules. 
6. The system records the major and selected courses. 
7. The system displays the confirmed academic plan.

Extensions:
- 4a. The player cancels before confirming.
  The system ends the selection process without changing the recorded academic plan.
- 5a. The selections do not meet the enrolment rules.
  The system explains what must change and allows the player to revise their selections. No invalid plan is recorded

### Use Case 1.2: Attend a class
Primary actor: Student player

Preconditions:
- The player is enrolled in a course.
- The current semester is in progress.
- The game has class content available for that course.

Trigger: The player asks to attend a class for an enrolled course.

Success outcome: The class is recorded as completed, and the player sees their participation result.

Main success scenario:
1. The player requests to attend a class for an enrolled course.
2. The system checks that the class is available and has not already been completed.
3. The system presents the class material and a short practice question.
4. The player submits an answer.
5. The system evaluates the answer and displays feedback.
6. The system records the class as completed.
7. The system displays the updated course progress.

Extensions:
- 2a. The class is unavailable or already completed.
  The system explains why it cannot be attended. Course progress remains unchanged.
- 4a. The player submits no answer.
  The system asks the player to provide an answer and continues the class.
- 4b. The player leaves before submitting.
  The class is not recorded as completed, and course progress remains unchanged.
- 5a. The answer is incorrect.
  The system explains the correct answer. The class is still recorded as completed, with the actual participation result.

### Use Case 1.3: Take a course exam
Primary actor: Student player
Preconditions:
- The player is enrolled in the selected course.
- The game has exam questions and a grading rule for that course.
- The player has an academic record that can store course results.

Trigger: The player asks to take the course exam.

Success outcome: The exam score and corresponding course grade are recorded, the GPA is recalculated, and the player sees the results.

Main success scenario:
1. The player requests to take an exam for an enrolled course.
2. The system checks that the exam is available and has not already been completed.
3. The system explains the exam rules and presents the questions.
4. The player answers the questions and submits the exam.
5. The system checks that all required questions have answers.
6. The system evaluates the answers and calculates the exam score.
7. The system determines the course grade using the agreed grading rule.
8. The system records the result and recalculates the player’s GPA.
9. The system displays the exam score, course grade, and updated GPA.

Extensions:
- 2a. The exam is unavailable or already completed.
  The system explains why the exam cannot be started. Grades and GPA remain unchanged.
- 5a. Required answers are missing.
  The system identifies the unanswered questions and allows the player to complete them before resubmitting.
- 4a. The player cancels before submission.
  Under the proposed MVP rule, the attempt is discarded. No grade is recorded, and GPA remains unchanged.
- 8a. The system cannot record the result.
  The system reports that finalization failed and allows a retry without awarding the result twice.

### Use Case 2
### Use Case 3
### Use Case 4
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