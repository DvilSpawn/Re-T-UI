package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.tuixt.TuixtDialog;
import ohi.andre.consolelauncher.managers.PomodoroManager;
import android.os.Handler;
import android.os.Looper;

public class pomodoro implements CommandAbstraction {

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public String exec(ExecutePack pack) {
        String input = pack.get(String.class, 0);
        PomodoroManager manager = PomodoroManager.getInstance(pack.context);

        if (input != null && input.equals("-stop")) {
            if (manager.isRunning()) {
                manager.stopSession();
                return "Pomodoro session stopped.";
            } else {
                return "No Pomodoro session is running.";
            }
        }

        if (manager.isRunning()) {
            return "A Pomodoro session is already active.";
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            TuixtDialog.showInput(pack.context, "NEW POMODORO", "What task are we focusing on?", "START", "CANCEL", value -> {
                if (value != null && !value.trim().isEmpty()) {
                    PomodoroManager.getInstance(pack.context).startPomodoro(value.trim());
                }
            });
        });

        return "Opening Pomodoro setup...";
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return exec(pack);
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return exec(pack);
    }
}
