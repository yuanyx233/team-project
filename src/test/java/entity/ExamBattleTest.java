package entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ExamBattleTest {
    @Test
    void answersDamageTheRightSide() {
        ExamBattle battle = new ExamBattle();
        battle.submitAnswer(true);
        assertEquals(40, battle.getBossHp());
        assertEquals(60, battle.getPlayerHp());

        battle.submitAnswer(false);
        assertEquals(40, battle.getBossHp());
        assertEquals(40, battle.getPlayerHp());
        assertEquals(2, battle.getAnsweredCount());
        assertEquals(1, battle.getCorrectCount());
        assertFalse(battle.isFinished());
        assertFalse(battle.hasWon());
    }

    @ParameterizedTest
    @CsvSource({"true, 0, 60, 3", "false, 60, 0, 0"})
    void threeHitsEndBattle(boolean correct, int bossHp, int playerHp, int correctCount) {
        ExamBattle battle = new ExamBattle();
        for (int i = 0; i < 3; i++) {
            battle.submitAnswer(correct);
        }

        assertEquals(bossHp, battle.getBossHp());
        assertEquals(playerHp, battle.getPlayerHp());
        assertEquals(correctCount, battle.getCorrectCount());
        assertEquals(correct, battle.hasWon());
        assertTrue(battle.isFinished());
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer(true));
        assertEquals(3, battle.getAnsweredCount());
        assertEquals(bossHp, battle.getBossHp());
        assertEquals(playerHp, battle.getPlayerHp());
    }

    @ParameterizedTest
    @CsvSource({"true, 0, 20, 3", "false, 20, 0, 2"})
    void fifthAnswerDecidesBattle(boolean correct, int bossHp, int playerHp, int correctCount) {
        ExamBattle battle = new ExamBattle();
        battle.submitAnswer(true);
        battle.submitAnswer(false);
        battle.submitAnswer(true);
        battle.submitAnswer(false);
        assertFalse(battle.isFinished());

        battle.submitAnswer(correct);
        assertEquals(5, battle.getAnsweredCount());
        assertEquals(correctCount, battle.getCorrectCount());
        assertEquals(bossHp, battle.getBossHp());
        assertEquals(playerHp, battle.getPlayerHp());
        assertTrue(battle.isFinished());
        assertEquals(correct, battle.hasWon());
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer(false));
        assertEquals(5, battle.getAnsweredCount());
    }

    @ParameterizedTest
    @CsvSource({"0, false, false", "1, true, false", "2, true, true", "3, true, true"})
    void pointsUnlockSkills(int points, boolean hint, boolean shield) {
        ExamBattle battle = new ExamBattle(points);
        assertEquals(hint, battle.canUseHint());
        assertEquals(shield, battle.canUseShield());
    }

    @Test
    void lockedSkillsCannotBeUsed() {
        ExamBattle battle = new ExamBattle();
        assertThrows(IllegalStateException.class, () -> battle.useHint(0));
        assertThrows(IllegalStateException.class, battle::useShield);
        assertEquals(0, battle.getAnsweredCount());
        assertEquals(60, battle.getPlayerHp());
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "3, 0"})
    void hintEliminatesOneWrongOption(int correctOption, int eliminatedOption) {
        ExamBattle battle = new ExamBattle(2);
        assertEquals(eliminatedOption, battle.useHint(correctOption));
        assertThrows(IllegalStateException.class, () -> battle.useHint(correctOption));
        assertEquals(0, battle.getAnsweredCount());
        assertTrue(battle.canUseShield());
    }

    @ParameterizedTest
    @CsvSource({"-1", "4"})
    void invalidOptionDoesNotUseUpHint(int option) {
        ExamBattle battle = new ExamBattle(1);
        assertThrows(IllegalArgumentException.class, () -> battle.useHint(option));
        assertTrue(battle.canUseHint());
    }

    @Test
    void shieldWaitsForOneWrongAnswer() {
        ExamBattle battle = new ExamBattle(2);
        battle.useShield();
        assertThrows(IllegalStateException.class, battle::useShield);
        battle.submitAnswer(true);
        assertTrue(battle.isShieldActive());
        battle.submitAnswer(false);
        assertEquals(60, battle.getPlayerHp());
        assertFalse(battle.isShieldActive());
        battle.submitAnswer(false);
        assertEquals(40, battle.getPlayerHp());
        assertEquals(1, battle.getCorrectCount());
        assertEquals(3, battle.getAnsweredCount());
    }

    @Test
    void survivingFiveQuestionsStillLosesIfBossIsAlive() {
        ExamBattle battle = new ExamBattle(2);
        battle.useShield();
        battle.submitAnswer(false);
        battle.submitAnswer(false);
        battle.submitAnswer(true);
        battle.submitAnswer(true);
        battle.submitAnswer(false);
        assertEquals(20, battle.getPlayerHp());
        assertEquals(20, battle.getBossHp());
        assertTrue(battle.isFinished());
        assertFalse(battle.hasWon());
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer(true));
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void skillsCannotBeUsedAfterBattle(boolean correct) {
        ExamBattle battle = new ExamBattle(2);
        for (int i = 0; i < 3; i++) {
            battle.submitAnswer(correct);
        }
        assertFalse(battle.canUseHint());
        assertFalse(battle.canUseShield());
        assertThrows(IllegalStateException.class, () -> battle.useHint(0));
        assertThrows(IllegalStateException.class, battle::useShield);
    }

    @Test
    void shieldExpiresWhenBattleEnds() {
        ExamBattle battle = new ExamBattle(2);
        battle.useShield();
        for (int i = 0; i < 3; i++) {
            battle.submitAnswer(true);
        }
        assertFalse(battle.isShieldActive());
    }

    @Test
    void negativePointsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ExamBattle(-1));
    }
}
