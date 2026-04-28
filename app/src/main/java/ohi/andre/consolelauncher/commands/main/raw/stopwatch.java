package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.managers.ClockManager;

public class stopwatch implements CommandAbstraction {

    @Override
    public int[] argType() {
        return new int[] {CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public String exec(ExecutePack pack) {
        Object arg = pack.get(Object.class, 0);
        String input = arg == null ? null : arg.toString().trim().toLowerCase();

        ClockManager clockManager = ClockManager.getInstance(pack.context);

        if (input == null || input.length() == 0 || !input.startsWith("-")) {
            return clockManager.startStopwatch();
        }

        if ("-stop".equals(input)) {
            return clockManager.stopStopwatch();
        }
        if ("-reset".equals(input)) {
            return clockManager.resetStopwatch();
        }
        if ("-status".equals(input)) {
            return clockManager.getStopwatchStatus();
        }

        return pack.context.getString(R.string.output_invalid_param) + " " + input;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return ClockManager.getInstance(pack.context).startStopwatch();
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return ClockManager.getInstance(pack.context).startStopwatch();
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_stopwatch;
    }
}
