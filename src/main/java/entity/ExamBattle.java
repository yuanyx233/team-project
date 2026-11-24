package entity;

public class ExamBattle {
    private static final int STARTING_HP = 60;
    private static final int DAMAGE = 20;
    private static final int MAX_QUESTIONS = 5;

    private final int preparationPoints;
    private int playerHp = STARTING_HP;
    private int bossHp = STARTING_HP;
    private int answeredCount;
    private int correctCount;
    private boolean hintUsed;
    private boolean shieldUsed;
    private boolean shieldActive;

    public ExamBattle() {
        this(0);
    }

    public ExamBattle(int preparationPoints) {
        if (preparationPoints < 0) {
            throw new IllegalArgumentException("Preparation points cannot be negative.");
        }
        this.preparationPoints = preparationPoints;
    }

    public void submitAnswer(boolean correct) {
        if (isFinished()) {
            throw new IllegalStateException("This battle has already ended.");
        }

        answeredCount++;
        if (correct) {
            bossHp -= DAMAGE;
            correctCount++;
        } else if (shieldActive) {
            shieldActive = false;
        } else {
            playerHp -= DAMAGE;
        }
        if (isFinished()) {
            shieldActive = false;
        }
    }

    public boolean canUseHint() {
        return !isFinished() && preparationPoints >= 1 && !hintUsed;
    }

    public int useHint(int correctOption) {
        if (!canUseHint()) {
            throw new IllegalStateException("Hint is not available.");
        }
        if (correctOption < 0 || correctOption > 3) {
            throw new IllegalArgumentException("The answer must be between 0 and 3.");
        }
        hintUsed = true;
        // All exam questions have four options.
        return correctOption == 0 ? 1 : 0;
    }

    public boolean canUseShield() {
        return !isFinished() && preparationPoints >= 2 && !shieldUsed;
    }

    public void useShield() {
        if (!canUseShield()) {
            throw new IllegalStateException("Shield is not available.");
        }
        shieldUsed = true;
        shieldActive = true;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public boolean isFinished() {
        return bossHp == 0 || playerHp == 0 || answeredCount >= MAX_QUESTIONS;
    }

    public boolean hasWon() {
        return bossHp == 0;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public int getBossHp() {
        return bossHp;
    }

    public int getAnsweredCount() {
        return answeredCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }
}
