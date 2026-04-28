package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.BuildConfig;

public class ClockManager {

    public static final String ACTION_CLOCK_STATE = BuildConfig.APPLICATION_ID + ".clock_state";
    public static final String EXTRA_TIMER_RUNNING = "timer_running";
    public static final String EXTRA_TIMER_REMAINING = "timer_remaining";
    public static final String EXTRA_STOPWATCH_RUNNING = "stopwatch_running";
    public static final String EXTRA_STOPWATCH_ELAPSED = "stopwatch_elapsed";
    public static final String EXTRA_MESSAGE = "message";

    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)^\\s*(\\d+)\\s*([smh])\\s*$");

    private static ClockManager instance;

    public static synchronized ClockManager getInstance(Context context) {
        if (instance == null) {
            instance = new ClockManager(context.getApplicationContext());
        }
        return instance;
    }

    public static long parseDurationMillis(String value) {
        if (value == null) {
            return -1L;
        }

        Matcher matcher = DURATION_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return -1L;
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase(Locale.getDefault());
        switch (unit) {
            case "s":
                return amount * 1000L;
            case "m":
                return amount * 60_000L;
            case "h":
                return amount * 3_600_000L;
            default:
                return -1L;
        }
    }

    public static String formatDuration(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private final Context appContext;
    private final LocalBroadcastManager lbm;
    private final Handler handler;

    private long timerEndElapsedRealtime = -1L;
    private boolean timerRunning = false;

    private long stopwatchStartElapsedRealtime = -1L;
    private long stopwatchBaseElapsed = 0L;
    private boolean stopwatchRunning = false;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            boolean needsAnotherTick = false;
            String message = null;

            if (timerRunning) {
                long remaining = getTimerRemainingMillis();
                if (remaining <= 0L) {
                    timerRunning = false;
                    timerEndElapsedRealtime = -1L;
                    playCompletionTone();
                    message = "Timer finished.";
                } else {
                    needsAnotherTick = true;
                }
            }

            if (stopwatchRunning) {
                needsAnotherTick = true;
            }

            broadcastState(message);
            if (needsAnotherTick) {
                handler.postDelayed(this, 1000L);
            }
        }
    };

    private ClockManager(Context context) {
        this.appContext = context;
        this.lbm = LocalBroadcastManager.getInstance(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public synchronized String startTimer(long durationMs) {
        if (durationMs <= 0L) {
            return "Invalid duration. Use values like 30s, 5m, or 1h.";
        }

        timerEndElapsedRealtime = SystemClock.elapsedRealtime() + durationMs;
        timerRunning = true;
        scheduleTicker();
        broadcastState();
        return "Timer started for " + formatDuration(durationMs) + ".";
    }

    public synchronized String addToTimer(long durationMs) {
        if (durationMs <= 0L) {
            return "Invalid duration. Use values like 30s, 5m, or 1h.";
        }
        if (!timerRunning) {
            return "No running timer to extend.";
        }

        timerEndElapsedRealtime += durationMs;
        scheduleTicker();
        broadcastState();
        return "Added " + formatDuration(durationMs) + " to timer.";
    }

    public synchronized String stopTimer() {
        if (!timerRunning) {
            return "No timer is running.";
        }

        timerRunning = false;
        timerEndElapsedRealtime = -1L;
        broadcastState();
        return "Timer stopped.";
    }

    public synchronized String getTimerStatus() {
        if (!timerRunning) {
            return "No timer is running.";
        }
        return "Timer remaining: " + formatDuration(getTimerRemainingMillis()) + ".";
    }

    public synchronized boolean isTimerRunning() {
        return timerRunning;
    }

    public synchronized String startStopwatch() {
        if (stopwatchRunning) {
            return "Stopwatch already running: " + formatDuration(getStopwatchElapsedMillis()) + ".";
        }

        stopwatchStartElapsedRealtime = SystemClock.elapsedRealtime();
        stopwatchRunning = true;
        scheduleTicker();
        broadcastState();
        return "Stopwatch started.";
    }

    public synchronized String stopStopwatch() {
        if (!stopwatchRunning) {
            return "No stopwatch is running.";
        }

        stopwatchBaseElapsed = getStopwatchElapsedMillis();
        stopwatchRunning = false;
        stopwatchStartElapsedRealtime = -1L;
        broadcastState();
        return "Stopwatch stopped at " + formatDuration(stopwatchBaseElapsed) + ".";
    }

    public synchronized String resetStopwatch() {
        stopwatchRunning = false;
        stopwatchStartElapsedRealtime = -1L;
        stopwatchBaseElapsed = 0L;
        broadcastState();
        return "Stopwatch reset.";
    }

    public synchronized String getStopwatchStatus() {
        if (!stopwatchRunning && stopwatchBaseElapsed <= 0L) {
            return "No stopwatch is running.";
        }
        return "Stopwatch: " + formatDuration(getStopwatchElapsedMillis()) + ".";
    }

    public synchronized void broadcastState() {
        broadcastState(null);
    }

    private synchronized void broadcastState(String message) {
        Intent intent = new Intent(ACTION_CLOCK_STATE);
        intent.putExtra(EXTRA_TIMER_RUNNING, timerRunning);
        intent.putExtra(EXTRA_TIMER_REMAINING, getTimerRemainingMillis());
        intent.putExtra(EXTRA_STOPWATCH_RUNNING, stopwatchRunning);
        intent.putExtra(EXTRA_STOPWATCH_ELAPSED, getStopwatchElapsedMillis());
        if (message != null) {
            intent.putExtra(EXTRA_MESSAGE, message);
        }
        lbm.sendBroadcast(intent);
    }

    private long getTimerRemainingMillis() {
        if (!timerRunning) {
            return 0L;
        }
        return Math.max(0L, timerEndElapsedRealtime - SystemClock.elapsedRealtime());
    }

    private long getStopwatchElapsedMillis() {
        if (stopwatchRunning && stopwatchStartElapsedRealtime > 0L) {
            return stopwatchBaseElapsed + (SystemClock.elapsedRealtime() - stopwatchStartElapsedRealtime);
        }
        return stopwatchBaseElapsed;
    }

    private void scheduleTicker() {
        handler.removeCallbacks(ticker);
        if (timerRunning || stopwatchRunning) {
            handler.post(ticker);
        }
    }

    private void playCompletionTone() {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 70);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1200);
        } catch (Exception ignored) {
        }
    }
}
