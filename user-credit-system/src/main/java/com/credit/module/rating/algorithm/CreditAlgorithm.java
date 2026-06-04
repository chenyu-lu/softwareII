package com.credit.module.rating.algorithm;
import org.springframework.stereotype.Component;

@Component
public class CreditAlgorithm {
    public int calcNewScore(int oldScore, double avgScore) {
        int add = 0;
        if (avgScore >= 4.5) add = 8;
        else if (avgScore >= 3.5) add = 3;
        else if (avgScore >= 2.5) add = 0;
        else if (avgScore >= 1.5) add = -5;
        else add = -12;
        int newScore = oldScore + add;
        return Math.max(0, Math.min(200, newScore));
    }
}

