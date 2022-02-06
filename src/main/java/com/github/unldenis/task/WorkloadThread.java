package com.github.unldenis.task;

import com.google.common.collect.*;
import lombok.*;

import java.util.*;

public class WorkloadThread implements Runnable{

    private final int MAX_MS_PER_TICK;
    private final ArrayDeque<Workload> workloadDeque;
    private Workload firstElement;

    public WorkloadThread(int msPerTick) {
        MAX_MS_PER_TICK = msPerTick;
        workloadDeque = Queues.newArrayDeque();
    }

    public void addLoad(@NonNull Workload workload) {
        workloadDeque.add(workload);
    }

    public void removeLoad(@NonNull Workload workload) {
        if(firstElement == workload) {
            firstElement = null;
        }
        workloadDeque.remove(workload);
    }

    @Override
    public void run() {
        long stopTime = System.currentTimeMillis() + MAX_MS_PER_TICK;
        while(!workloadDeque.isEmpty() && System.currentTimeMillis() <= stopTime) {
            Workload workload = workloadDeque.poll();
            if(!computeWorkload(workload)) {
                break;
            }
        }
    }

    private boolean computeWorkload(Workload workload) {
        if(workload != null) {
            workload.compute();
            if(workload.reschedule()) {
                addLoad(workload);
                if(firstElement == null) {
                    firstElement = workload;
                } else {
                    return firstElement != workload;
                }
            }
        }
        return true;
    }
}