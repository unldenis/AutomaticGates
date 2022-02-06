package com.github.unldenis.task;

public interface Workload {

    void compute();

    default boolean reschedule() {
        return false;
    }

}