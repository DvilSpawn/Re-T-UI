package ohi.andre.consolelauncher.commands.main.raw;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.WebhookManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class webhook extends ParamCommand {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        add {
            @Override
            public String exec(ExecutePack pack) {
                ArrayList<String> args = pack.getList();
                if (args.size() < 3) return "Usage: webhook -add [name] [url] [body_template]";
                
                String name = args.get(0);
                String url = args.get(1);
                
                List<String> bodyParts = args.subList(2, args.size());
                String body = Tuils.toPlanString(bodyParts, Tuils.SPACE);
                
                ((MainPack) pack).webhookManager.add(name, url, body);
                return "Webhook " + name + " added.";
            }

            @Override
            public int[] args() {
                return new int[]{CommandAbstraction.TEXTLIST};
            }
        },
        rm {
            @Override
            public String exec(ExecutePack pack) {
                ArrayList<String> args = pack.getList();
                if (args.size() < 1) return "Usage: webhook -rm [name]";
                String name = args.get(0);
                ((MainPack) pack).webhookManager.remove(name);
                return "Webhook " + name + " removed.";
            }

            @Override
            public int[] args() {
                return new int[]{CommandAbstraction.TEXTLIST};
            }
        },
        ls {
            @Override
            public String exec(ExecutePack pack) {
                List<WebhookManager.Webhook> hooks = ((MainPack) pack).webhookManager.getWebhooks();
                if (hooks.isEmpty()) return "No webhooks configured.";
                StringBuilder sb = new StringBuilder();
                for (WebhookManager.Webhook w : hooks) {
                    sb.append(w.name).append(" -> ").append(w.url).append(Tuils.NEWLINE);
                }
                return sb.toString().trim();
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];
            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }
            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_webhook);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return null;
        }
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    protected String doThings(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = info.lastCommand;
        if (input == null) return null;

        String[] split = input.split(Tuils.SPACE);
        if (split.length < 2) return null;

        String sub = split[1];
        if (sub.startsWith("-")) return null;

        // Trigger webhook
        WebhookManager.Webhook w = info.webhookManager.getWebhook(sub);
        if (w == null) return "Webhook not found: " + sub;

        String[] webhookArgs;
        if (split.length > 2) {
            webhookArgs = new String[split.length - 2];
            System.arraycopy(split, 2, webhookArgs, 0, webhookArgs.length);
        } else {
            webhookArgs = new String[0];
        }

        String bodyContent = w.substitute(webhookArgs);
        if (webhookArgs.length > 0) {
            info.historyManager.add(w.name, Tuils.toPlanString(webhookArgs, Tuils.SPACE));
        }
        
        MediaType mediaType = bodyContent.trim().startsWith("{") || bodyContent.trim().startsWith("[") ? JSON : TEXT;
        RequestBody body = RequestBody.create(bodyContent, mediaType);
        
        Request request = new Request.Builder()
                .url(w.url)
                .post(body)
                .build();

        final Handler handler = new Handler(Looper.getMainLooper());
        
        info.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String error = e.toString();
                handler.post(() -> Tuils.sendOutput(info.context, "Webhook [" + w.name + "] Error: " + error));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response r = response) {
                    final String resBody = r.body() != null ? r.body().string() : "Empty Response";
                    final int code = r.code();
                    handler.post(() -> Tuils.sendOutput(info.context, "Webhook [" + w.name + "] Response [" + code + "]: " + resBody));
                }
            }
        });

        return "Triggering webhook: " + w.name;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_webhook;
    }
}
