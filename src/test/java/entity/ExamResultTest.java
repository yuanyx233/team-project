package entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ExamResultTest {
    @ParameterizedTest
    @CsvSource({
        "TTT,   3, 3, 100, A, true",
        "TFTT,  4, 3, 75,  B, true",
        "TFTFT, 5, 3, 60,  C, true",
        "FFF,   3, 0, 0,   F, false",
        "TFFF,  4, 1, 25,  F, false",
        "TFTFF, 5, 2, 40,  F, false"
    })
    void finishedBattleProducesResult(String answers, int answered, int correct,
                                      double score, String grade, boolean won) {
        ExamBattle battle = new ExamBattle();
        for (char answer : answers.toCharArray()) {
            battle.submitAnswer(answer == 'T');
        }

        ExamResult result = new ExamResult(battle);
        assertEquals(answered, result.getAnsweredCount());
        assertEquals(correct, result.getCorrectCount());
        assertEquals(score, result.getScore(), 0.001);
        assertEquals(grade, result.getGrade());
        assertEquals(won, result.hasWon());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void unfinishedBattleHasNoResult(int answered) {
        ExamBattle battle = new ExamBattle();
        for (int i = 0; i < answered; i++) {
            battle.submitAnswer(true);
        }
        assertThrows(IllegalStateException.class, () -> new ExamResult(battle));
    }
}
