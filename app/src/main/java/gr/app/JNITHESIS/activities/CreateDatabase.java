package gr.app.JNITHESIS.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tapadoo.alerter.Alerter;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import gr.app.JNITHESIS.Consts;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.adapters.database_adapter;
import gr.app.JNITHESIS.databinding.ActivityCreateDatabaseBinding;
import gr.app.JNITHESIS.models.Database;
import gr.app.JNITHESIS.requests.JRequests;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateDatabase extends AppCompatActivity {


    private static ActivityCreateDatabaseBinding binding;
    private static String server_port = Consts.server_port;
    private static String server_host = Consts.server_host;
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCreateDatabaseBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.backOnMain.setOnClickListener(t -> {
            Intent i = new Intent(this , MainActivity.class);
            i.putExtra("flag" , "0");
            startActivity(i);
            finish();
            overridePendingTransition(0,0);
        });
        ArrayList<Database> dbs = (ArrayList<Database>) getIntent().getSerializableExtra("dbs");

        binding.createDatabase.setOnClickListener(t -> {
            String dbname = binding.dbname.getText().toString();
            if(dbname.isEmpty() || dbname.trim().isEmpty()){
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Ορίστε κάποιο Όνομα Βάσης").show();
                return;
            }
            if(dbname.length() <= 2){
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Το όνομα θα πρεπει να είναι τουλαχιστον 3 χαρακτήρες").show();
                return;
            }
            // Success
            if (dbs.stream().anyMatch(ti -> ti.name.toLowerCase().equals(dbname.toLowerCase()))){
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Η ΒΔ με αυτό το όνομα υπάρχει ήδη").show();
                return;
            }

            new Thread(() -> {
                OkHttpClient HttpClient = new OkHttpClient();


                Request request = new Request.Builder()
                        .post( new JRequests().create_database_query(dbname))
                        .url(server_host + server_port).build();
                try {
                    Response response = HttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        String body = response.body().string();
                        Alerter.create(this)
                                .setTitle("Επιτυχές").setText("Η Βάση Δεδομένων δημιουργήθηκε επιτυχώς!").show();

                        Database new_db = new Database();
                        new_db.name = dbname;
                        dbs.add(new_db);

                    }else{
                        String err = response.body().string();
                        Alerter.create(this)
                                .setIcon(R.drawable.error_box)
                                .setIconColorFilter(Color.parseColor("#F0706A"))
                                .setTitle("Σφάλμα").setText(err).show();

                    }
                } catch (IOException e) {
                    // Perhaps server is still not ready.
                    Alerter.create(this)
                            .setIcon(R.drawable.error_box)
                            .setIconColorFilter(Color.parseColor("#F0706A"))
                            .setTitle("Σφάλμα").setText("Πρόβλημα CreateDatabase " + e.getMessage()).show();
                    //throw new RuntimeException(e);
                }

            }).start();

        });

    }
}
