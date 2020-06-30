package com.shtrih.tinyjavapostester.task.listener;

public interface Listener<T> {
    public void handle(T value);
}
