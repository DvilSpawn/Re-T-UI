package ohi.andre.consolelauncher.commands.main.raw;

import java.io.File;
import java.util.Arrays;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.tuils.Tuils;

public class ls implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        File file = info.args != null && info.args.length > 0 ? info.get(File.class) : info.currentDirectory;
        if (file == null || !file.exists()) {
            return info.res.getString(R.string.output_filenotfound);
        }
        if (file.isFile()) {
            return file.getName();
        }

        String[] names = file.list();
        if (names == null || names.length == 0) {
            return "[]";
        }
        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < names.length; i++) {
            File child = new File(file, names[i]);
            if (child.isDirectory()) {
                names[i] = names[i] + File.separator;
            }
        }
        return Tuils.toPlanString(names, Tuils.NEWLINE);
    }

    @Override
    public int helpRes() {
        return R.string.help_ls;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.FILE};
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return ((MainPack) pack).res.getString(R.string.output_filenotfound);
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return exec(pack);
    }
}
