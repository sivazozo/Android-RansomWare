package fr.esgi.ransomware.task;

import android.content.Context;
import android.os.Handler;

public abstract class Task {
    protected final Context context;
    protected final Handler handler;

    public Task(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public abstract void run() throws Exception;
}
