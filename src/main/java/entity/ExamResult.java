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
