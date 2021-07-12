/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

/**
 *
 * @author Mark
 */
public class TimingHelper {

    private String helperName;

    private long startTime, endTime, cumulativeTime;
    private int reportEvery, reportEveryCounter;
    private boolean timerRunning;

    TimingHelper(String name) {
        this.helperName = name;
        this.cumulativeTime = 0;
        this.reportEvery = 0;
        this.reportEveryCounter = 0;
        startTimer();
    }

    TimingHelper(String name, int reportEvery) {
        this.helperName = name;
        this.cumulativeTime = 0;
        this.reportEvery = reportEvery;
        this.reportEveryCounter = reportEvery;
        startTimer();
    }

    public long stopTimer() {
        endTime = System.currentTimeMillis();
        timerRunning = false;
        return endTime - startTime;
    }

    public void stopAndReport() {
        System.out.println("Time for " + helperName + " is " + stopTimer() + " milliseconds.");
    }

    public void report() {
        endTime = System.currentTimeMillis();
        System.out.println("Time for " + helperName + " is " + (endTime - startTime) + " milliseconds.");
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        timerRunning = true;
    }

    public void resetTimer() {
        startTimer();
        cumulativeTime = 0;
    }

    public void pauseTimer() {
        cumulativeTime += stopTimer();

        if (reportEvery != 0) {
            if (--reportEveryCounter == 0) {
                System.out.println("Average time for last " + reportEvery + " calls of " + helperName + " is " + ((float) cumulativeTime / (float) reportEvery) + " milliseconds.");
                resetTimer();
                reportEveryCounter = reportEvery;
            }
        }
    }
}
