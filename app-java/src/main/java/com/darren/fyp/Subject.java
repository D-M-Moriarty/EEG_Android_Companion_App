package com.darren.fyp;

public interface Subject {
    void registerObserver(Observer observer);
    void notifyObservers();
}
