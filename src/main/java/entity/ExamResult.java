package entity;

public final class ExamResult {
    private final int correctCount;
    private final int answeredCount;
    private final boolean won;

    public ExamResult(ExamBattle battle) {
        if (!battle.isFinished()) {
            throw new IllegalStateException("Finish the battle before recording a result.");
        }
        correctCount = battle.getCorrectCount();
        answeredCount = battle.getAnsweredCount();
        won = battle.hasWon();
    }

    public ExamResult(int correctCount, int answeredCount, boolean won) {
        if (answeredCount < 3 || answeredCount > 5 || correctCount < 0 || correctCount > 3
                || correctCount > answeredCount || answeredCount - correctCount > 4
                || won != (correctCount == 3)
                || (!won && answeredCount < 5 && answeredCount - correctCount < 3)) {
            throw new IllegalArgumentException("Invalid exam result.");
        }
        this.correctCount = correctCount;
        this.answeredCount = answeredCount;
        this.won = won;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getAnsweredCount() {
        return answeredCount;
    }

    public double getScore() {
        return correctCount * 100.0 / answeredCount;
    }

    public String getGrade() {
        double score = getScore();
        if (score >= 80) {
            return "A";
        }
        if (score >= 70) {
            return "B";
        }
        if (score >= 60) {
            return "C";
        }
        if (score >= 50) {
            return "D";
        }
        return "F";
    }

    public boolean hasWon() {
        return won;
    }
}
