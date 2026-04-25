package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.BuildConfig;

public class PomodoroManager {

    public enum SessionType {
        FOCUS, BREAK, FINISHED
    }

    public static final String ACTION_POMODORO_STATE = BuildConfig.APPLICATION_ID + ".pomodoro_state";
    public static final String EXTRA_POMODORO_RUNNING = "pomodoro_running";
    public static final String EXTRA_POMODORO_REMAINING = "pomodoro_remaining";
    public static final String EXTRA_POMODORO_TOTAL = "pomodoro_total";
    public static final String EXTRA_POMODORO_TASK = "pomodoro_task";
    public static final String EXTRA_POMODORO_TYPE = "pomodoro_type";
    public static final String EXTRA_POMODORO_CYCLE = "pomodoro_cycle";
    public static final String EXTRA_MESSAGE = "message";

    private static final long FOCUS_DURATION = 25 * 60 * 1000L;
    private static final long BREAK_DURATION = 5 * 60 * 1000L;
    private static final int TOTAL_CYCLES = 4;

    private static PomodoroManager instance;

    public static synchronized PomodoroManager getInstance(Context context) {
        if (instance == null) {
            instance = new PomodoroManager(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final LocalBroadcastManager lbm;
    private final Handler handler;

    private long sessionEndElapsedRealtime = -1L;
    private long totalDuration = 0L;
    private boolean running = false;
    private String taskName = "";
    private SessionType currentType = SessionType.FOCUS;
    private int completedFocuses = 0;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            long remaining = getRemainingMillis();
            if (remaining <= 0L) {
                handleTransition();
            } else {
                broadcastState(null);
                handler.postDelayed(this, 1000L);
            }
        }
    };

    private PomodoroManager(Context context) {
        this.appContext = context;
        this.lbm = LocalBroadcastManager.getInstance(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public synchronized void startPomodoro(String task) {
        this.taskName = task;
        this.completedFocuses = 0;
        this.running = true;
        startFocusSession();
    }

    private void startFocusSession() {
        this.currentType = SessionType.FOCUS;
        this.totalDuration = FOCUS_DURATION;
        this.sessionEndElapsedRealtime = SystemClock.elapsedRealtime() + totalDuration;
        
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        broadcastState("Focus session started: " + taskName);
    }

    private void startBreakSession() {
        this.currentType = SessionType.BREAK;
        this.totalDuration = BREAK_DURATION;
        this.sessionEndElapsedRealtime = SystemClock.elapsedRealtime() + totalDuration;

        handler.removeCallbacks(ticker);
        handler.post(ticker);
        broadcastState("Take a break!");
    }

    private void handleTransition() {
        playTone();
        if (currentType == SessionType.FOCUS) {
            completedFocuses++;
            if (completedFocuses >= TOTAL_CYCLES) {
                currentType = SessionType.FINISHED;
                running = true; // Stay in finished state to show the message
                broadcastState("Good job! You did great!");
            } else {
                startBreakSession();
            }
        } else if (currentType == SessionType.BREAK) {
            startFocusSession();
        }
    }

    public synchronized void stopSession() {
        this.running = false;
        this.sessionEndElapsedRealtime = -1L;
        handler.removeCallbacks(ticker);
        broadcastState("Session terminated.");
    }

    public boolean isRunning() {
        return running;
    }

    public long getRemainingMillis() {
        if (!running || currentType == SessionType.FINISHED) return 0L;
        return Math.max(0L, sessionEndElapsedRealtime - SystemClock.elapsedRealtime());
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public String getTaskName() {
        return taskName;
    }

    public SessionType getCurrentType() {
        return currentType;
    }

    public int getCompletedFocuses() {
        return completedFocuses;
    }

    private void playTone() {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000);
        } catch (Exception ignored) {}
    }

    private void broadcastState(String message) {
        Intent intent = new Intent(ACTION_POMODORO_STATE);
        intent.putExtra(EXTRA_POMODORO_RUNNING, running);
        intent.putExtra(EXTRA_POMODORO_REMAINING, getRemainingMillis());
        intent.putExtra(EXTRA_POMODORO_TOTAL, totalDuration);
        intent.putExtra(EXTRA_POMODORO_TASK, taskName);
        intent.putExtra(EXTRA_POMODORO_TYPE, currentType.name());
        intent.putExtra(EXTRA_POMODORO_CYCLE, completedFocuses);
        if (message != null) {
            intent.putExtra(EXTRA_MESSAGE, message);
        }
        lbm.sendBroadcast(intent);
    }
}
