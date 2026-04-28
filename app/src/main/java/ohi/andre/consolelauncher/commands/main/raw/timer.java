package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.RedirectCommand;
import ohi.andre.consolelauncher.managers.ClockManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class timer implements CommandAbstraction {

    @Override
    public int[] argType() {
        return new int[] {CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public String exec(ExecutePack pack) {
        Object arg = pack.get(Object.class, 0);
        String input = arg == null ? null : arg.toString();
        return execute(pack, input);
    }

    private String execute(ExecutePack pack, String input) {
        if (input == null) {
            return pack.context.getString(R.string.help_timer);
        }

        String trimmed = input.trim();
        if (trimmed.length() == 0) {
            return pack.context.getString(R.string.help_timer);
        }

        ClockManager clockManager = ClockManager.getInstance(pack.context);

        if (trimmed.startsWith("-")) {
            String[] split = trimmed.split("\\s+", 2);
            String option = split[0].toLowerCase();

            if ("-stop".equals(option)) {
                return clockManager.stopTimer();
            }
            if ("-status".equals(option)) {
                return clockManager.getTimerStatus();
            }
            if ("-add".equals(option)) {
                if (split.length < 2) {
                    return pack.context.getString(R.string.help_timer);
                }
                long duration = ClockManager.parseDurationMillis(split[1]);
                return clockManager.addToTimer(duration);
            }

            return pack.context.getString(R.string.output_invalid_param) + " " + split[0];
        }

        long duration = ClockManager.parseDurationMillis(trimmed);
        if (clockManager.isTimerRunning()) {
            if (pack instanceof MainPack && duration > 0L) {
                ((MainPack) pack).redirectator.prepareRedirection(new TimerAddConfirmation(duration));
                return "Timer already running. Do you want to add " + ClockManager.formatDuration(duration) + " to it? (Yes/No)";
            }
            return "A timer is already running. Use timer -add [duration], timer -status, or timer -stop.";
        }
        return clockManager.startTimer(duration);
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return pack.context.getString(R.string.help_timer);
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return pack.context.getString(R.string.help_timer);
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_timer;
    }

    private static class TimerAddConfirmation extends RedirectCommand {
        private final long duration;

        private TimerAddConfirmation(long duration) {
            this.duration = duration;
        }

        @Override
        public String onRedirect(ExecutePack pack) {
            MainPack mainPack = (MainPack) pack;
            String answer = Tuils.EMPTYSTRING;
            if (!afterObjects.isEmpty() && afterObjects.get(0) != null) {
                answer = afterObjects.get(0).toString().trim();
            }

            mainPack.redirectator.cleanup();
            if ("yes".equalsIgnoreCase(answer) || "y".equalsIgnoreCase(answer)) {
                return ClockManager.getInstance(pack.context).addToTimer(duration);
            }
            return "Timer unchanged.";
        }

        @Override
        public int getHint() {
            return R.string.hint_wallpaper_auto_confirm;
        }

        @Override
        public boolean isWaitingPermission() {
            return false;
        }

        @Override
        public int[] argType() {
            return new int[0];
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public int helpRes() {
            return R.string.help_timer;
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int indexNotFound) {
            return null;
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int nArgs) {
            return null;
        }

        @Override
        public String exec(ExecutePack pack) {
            return null;
        }
    }
}
