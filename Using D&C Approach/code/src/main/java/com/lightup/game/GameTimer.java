package com.lightup.game;

import javax.swing.JLabel;
import javax.swing.Timer;
public class GameTimer {
    private JLabel timerLabel;
    private Timer timer;
    private int secondsElapsed;
    
    public GameTimer(JLabel timerLabel) {
        this.timerLabel = timerLabel;
        this.secondsElapsed = 0;
    }
    
    public void setTimerLabel(JLabel label) {
        this.timerLabel = label;
    }
    
    public void start() {
        secondsElapsed = 0;
        updateDisplay();
        
        if (timer != null) {
            timer.stop();
        }
        
        timer = new Timer(1000, e -> {
            secondsElapsed++;
            updateDisplay();
        });
        timer.start();
    }
    
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    public void reset() {
        stop();
        secondsElapsed = 0;
        updateDisplay();
    }
    
    public int getElapsedSeconds() {
        return secondsElapsed;
    }
    
    public String getFormattedTime() {
        return "Time: " + secondsElapsed + "s";
    }
    
    private void updateDisplay() {
        if (timerLabel != null) {
            timerLabel.setText(getFormattedTime());
        }
    }
}
