
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Timer {
    public static void Start(String name) {
        TimeStruct timer = timers.get(name);
        if(timer == null) {
            timer = new TimeStruct();
            timers.put(name, timer);
        }

        if(timer.currentElapse != timer.TIMER_INIT) {
            System.out.println("error starting a running timer " + name);
            return;
        }
        timer.currentElapse = System.nanoTime();
    }

    public static void Stop(String name) {
        TimeStruct timer = timers.get(name);
        if(timer == null) {
            System.out.println("error stopping a non existing timer " + name);
            return;
        }
        if(timer.currentElapse == timer.TIMER_INIT) {
            System.out.println("error stopping a non running timer " + name);
            return;
        }

        long time = System.nanoTime() - timer.currentElapse;
        timer.currentElapse = -1;
        if(time > timer.longestElapse) {
            timer.longestElapse = time;
        }
        if(time < timer.shortestElapse) {
            timer.shortestElapse = time;
        }
        timer.totalTime += time;
        ++timer.elapses;
    }

    public static void Abort(String name) {
        TimeStruct timer = timers.get(name);
        if(timer == null) {
            System.out.println("error aborting a non existing timer " + name);
            return;
        }
        if(timer.currentElapse == timer.TIMER_INIT) {
            System.out.println("error aborting a non running timer " + name);
            return;
        }

        timer.currentElapse = timer.TIMER_INIT;
    }

    public static void print(String name) {
        System.out.print(name + ": \t");
        TimeStruct timer = timers.get(name);
        System.out.print("avg: " + (timer.totalTime / timer.elapses / 1000000.0));
        System.out.println(" elapses: " + timer.elapses);
    }

    public static void printAll() {
        for (String name : timers.keySet()) {
            print(name);
        }
    }


    private static class TimeStruct {
        long longestElapse = Long.MIN_VALUE;
        long shortestElapse = Long.MAX_VALUE;
        long totalTime = 0;
        int elapses = 0;
        final long TIMER_INIT = -1;
        long currentElapse = TIMER_INIT;
    }
    private static Map<String, TimeStruct> timers = new HashMap<>();

}
