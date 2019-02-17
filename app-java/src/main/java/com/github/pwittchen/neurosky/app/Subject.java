package com.github.pwittchen.neurosky.app;

public interface Subject {
    void registerObserver(Observer observer);
    void notifyObservers();
}
