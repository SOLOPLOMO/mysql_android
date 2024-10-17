package gr.app.JNITHESIS.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import gr.app.JNITHESIS.Consts;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.databinding.ActivityMainBinding;
import gr.app.JNITHESIS.models.Database;
import gr.app.JNITHESIS.nodejs.node_functions;
import gr.app.JNITHESIS.requests.JRequests;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    static{
        System.loadLibrary("native-lib");
        System.loadLibrary("node");
    }

    private static String server_port = Consts.server_port;
    private static String server_host = Consts.server_host;
    private node_functions node = new node_functions();
    public native Integer runNODE(String[] arguments);


    private ActivityMainBinding binding;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handle_edit_db_btn();
        handle_create_db_btn();

        if(getIntent().getStringExtra("flag") == null) {
            open_server(null);
        }
    }
    private static int flag_create = 1;
    private void handle_edit_db_btn(){
        binding.editDatabase.setOnClickListener(t -> {
            if(flag_create == 0) return;
            flag_create = 0;
            new Thread(() -> {
                OkHttpClient HttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .post(new JRequests().fetch_schemas())
                        .url(server_host + server_port).build();
                try {
                    Response response = HttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        String body = response.body().string();
                        ArrayList<Database> dbs = fetch_dbs(body);
                        runOnUiThread(() ->{
                            Intent edit_db_intent = new Intent(this , EditDatabases.class);
                            edit_db_intent.putExtra("dbs" , dbs);
                            startActivity(edit_db_intent);
                            overridePendingTransition(0,0);
                            flag_create = 1;
                        });
                    }else{
                        String err = response.body().string();
                        Alerter.create(this)
                                .setIcon(R.drawable.error_box)
                                .setIconColorFilter(Color.parseColor("#F0706A"))
                                .setTitle("Σφάλμα").setText(err).show();
                        flag_create = 1;
                    }
                } catch (IOException | JSONException e) {
                    // Perhaps server is still not ready.
                    Alerter.create(this)
                            .setIcon(R.drawable.error_box)
                            .setIconColorFilter(Color.parseColor("#F0706A"))
                            .setTitle("Σφάλμα").setText("Ο Server είναι ακόμα υπό δημιουργια " + e.getMessage()).show();
                    //throw new RuntimeException(e);
                    flag_create = 1;
                }
            }).start();
        });
    }

    private static int flag_edit = 1;
    private void handle_create_db_btn(){

        binding.createDatabase.setOnClickListener(t -> {
            if(flag_edit == 0) return;
            flag_edit = 0;
            new Thread(() -> {
                OkHttpClient HttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .post( new JRequests().fetch_schemas())
                        .url(server_host + server_port).build();
                try {
                    Response response = HttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        String body = response.body().string();

                        ArrayList<Database> dbs = fetch_dbs(body);
                        runOnUiThread(() ->{
                            Intent create_db_intent = new Intent(this , CreateDatabase.class);
                            create_db_intent.putExtra("dbs" , dbs);
                            startActivity(create_db_intent);
                            overridePendingTransition(0,0);
                            flag_edit = 1;
                        });
                    }else{
                        String err = response.body().string();
                        Alerter.create(this)
                                .setIcon(R.drawable.error_box)
                                .setIconColorFilter(Color.parseColor("#F0706A"))
                                .setTitle("Σφάλμα").setText(err).show();
                        flag_edit = 1;
                    }
                } catch (IOException | JSONException e) {
                    // Perhaps server is still not ready.
                    Alerter.create(this)
                            .setIcon(R.drawable.error_box)
                            .setIconColorFilter(Color.parseColor("#F0706A"))
                            .setTitle("Σφάλμα").setText("Ο Server είναι ακόμα υπό δημιουργια").show();
                    //throw new RuntimeException(e);
                    flag_edit = 1;
                }
            }).start();
        });
    }

    private void open_server(String[] cmd_append){

        new Thread(() -> {
            String dir = getApplicationContext().getFilesDir().getAbsolutePath()+"/nodejs";
            File dir_ref = new File(dir);
            if (dir_ref.exists()) node.deleteFolderRecursively(new File(dir));
            node.copyAssetFolder(getApplicationContext().getAssets(), "nodejs", dir);

            String[] cmd = new String[]{
                    "node",
                    dir + "/server.js",
                    server_port
            };

            runNODE(cmd);
        }).start();
    }


    private ArrayList<Database> fetch_dbs(String body) throws JSONException {
        ArrayList<Database> dbs = new ArrayList<>();
        JSONArray dbs_array = new JSONArray(body);
        for(int i = 0; i < dbs_array.length(); i++){
            JSONObject obj = dbs_array.getJSONObject(i);
            Database db = new Database();
            db.name = obj.getString("schema_name");
            db.collation = obj.getString("DEFAULT_CHARACTER_SET_NAME");
            dbs.add(db);
        }
        return dbs;
    }


}