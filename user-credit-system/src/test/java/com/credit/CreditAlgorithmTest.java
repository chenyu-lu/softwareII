package com.credit;

import com.credit.module.rating.algorithm.CreditAlgorithm;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditAlgorithmTest {

    private final CreditAlgorithm algorithm = new CreditAlgorithm();

    @Test
    void highScoreShouldIncreaseCreditByEight() {
        int result = algorithm.calcNewScore(100, 4.8);
        assertThat(result).isEqualTo(108);
    }

    @Test
    void middleHighScoreShouldIncreaseCreditByThree() {
        int result = algorithm.calcNewScore(100, 3.8);
        assertThat(result).isEqualTo(103);
    }

    @Test
    void normalScoreShouldKeepCreditUnchanged() {
        int result = algorithm.calcNewScore(100, 3.0);
        assertThat(result).isEqualTo(100);
    }

    @Test
    void lowScoreShouldDecreaseCreditByFive() {
        int result = algorithm.calcNewScore(100, 2.0);
        assertThat(result).isEqualTo(95);
    }

    @Test
    void veryLowScoreShouldDecreaseCreditByTwelve() {
        int result = algorithm.calcNewScore(100, 1.0);
        assertThat(result).isEqualTo(88);
    }

    @Test
    void creditScoreShouldNotExceedTwoHundred() {
        int result = algorithm.calcNewScore(198, 5.0);
        assertThat(result).isEqualTo(200);
    }

    @Test
    void creditScoreShouldNotBeLowerThanZero() {
        int result = algorithm.calcNewScore(5, 1.0);
        assertThat(result).isEqualTo(0);
    }
}
