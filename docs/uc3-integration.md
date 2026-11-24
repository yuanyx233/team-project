# UC3: Final Monster exam

Zhenbin An owns the exam. The UI uses Swing; the use case and entities have no Swing, HTTP or JSON imports.

```text
ExamView -> ExamController -> ExamInputBoundary -> ExamInteractor
                                                  | ExamBattle / ExamResult
                                                  | QuestionGateway -> Hugging Face
                                                  | ExamStateGateway -> shared game state
                                                  v
ExamOutputBoundary <- ExamPresenter -> ExamViewModel -> ExamView
```

`Main` connects the objects. `ExamView` runs actions in a SwingWorker and renders on the EDT.
Question requests reuse UC2's `HuggingFaceQuestionGateway`, with purpose EXAM and count 5.
The view receives answer options but never the correct answer index.

## Rules

- Entry costs 40 Energy after loading succeeds. Battle HP is separate from Energy.
- Both sides start at 60 HP. Correct answers damage the Boss by 20; wrong answers damage the player by 20.
- At most five questions. Boss HP 0 means victory; player HP 0 or running out of questions means defeat.
- This course's preparation points unlock Hint at 1 and Shield at 2. Each skill is usable once per battle.
- Hint removes a wrong option. Shield blocks the next wrong answer's damage without awarding a correct answer.
- Score uses actual submitted answers. A >= 80, B >= 70, C >= 60, D >= 50, otherwise F.
- Wins and losses are recorded immediately; completed exams cannot be retaken.
- Leaving an unfinished exam keeps the Energy cost and records no grade.
- Questions require a live connection. API errors leave Energy and progress unchanged.

## Shared progress

The Registrar components and academic entities are reused from the UC1 branch. The integrated local
catalogue uses CS101/CS201, BIO101/BIO201 and HIST101/HIST201, with the six agreed abstract course names.
The Registrar does not call the separate UC1 live program-page scraper in this integration.

`FileGameStateDataAccessObject` supplies the current academic record to UC1, UC2 and UC3. Its activity
slot prevents concurrent class challenges, exams and rest. Continue replaces the record, so consumers
request the current record rather than retaining an old reference.

Version-2 checkpoints include the confirmed major, class results and exam results. Version-1 checkpoints
remain readable. Battle HP, unfinished answers and skill usage are not saved. Invalid checkpoints are
validated before replacing current state.

## Checks

`mvn test` includes battle rules, exam actions, shared progress/checkpoint tests, Swing navigation,
and a complete Boss fight through Swing buttons. Automated tests inject fixture questions.
`app.QuestionApiCheck` is the optional live Hugging Face check for all six topics.
