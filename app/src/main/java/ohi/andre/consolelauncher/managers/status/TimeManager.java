package ohi.andre.consolelauncher.managers.status;

import android.content.Context;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Ui;

public class TimeManager extends StatusManager {

    private final StatusUpdateListener listener;
    private final int size;

    public TimeManager(Context context, long delay, int size, StatusUpdateListener listener) {
        super(context, delay);
        this.size = size;
        this.listener = listener;
    }

    @Override
    protected void update() {
        if (listener != null) {
            listener.onUpdate(UIManager.Label.time, ohi.andre.consolelauncher.managers.TimeManager.instance.getCharSequence(context, size, "%t"));
        }
    }
}
