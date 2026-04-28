package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

public class dashboard implements CommandAbstraction {

    @Override
    public String exec(ExecutePack info) throws Exception {
        Intent intent = new Intent(UIManager.ACTION_DASHBOARD);
        if (info.args != null && info.args.length > 0) {
            try {
                Object arg = info.get();
                int page = (arg instanceof Integer) ? (int) arg : Integer.parseInt(arg.toString());
                intent.putExtra("page", page);
            } catch (Exception e) {
                intent.putExtra("page", 1);
            }
        } else {
            intent.putExtra("page", 1);
        }
        LocalBroadcastManager.getInstance(info.context.getApplicationContext()).sendBroadcast(intent);
        return null;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.INT};
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public int helpRes() {
        return R.string.help_dashboard;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
