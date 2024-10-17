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

import java.util.ArrayList;

import gr.app.JNITHESIS.Consts;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.adapters.database_adapter;
import gr.app.JNITHESIS.databinding.ActivityEditDatabaseBinding;
import gr.app.JNITHESIS.models.Database;
import gr.app.JNITHESIS.recycler_item_deco.DB_item_deco;

public class EditDatabases extends AppCompatActivity {




    private static ActivityEditDatabaseBinding binding;
    private static String server_port = Consts.server_port;
    private static String server_host = Consts.server_host;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityEditDatabaseBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ArrayList<Database> dbs = (ArrayList<Database>) getIntent().getSerializableExtra("dbs");
        if(getIntent().getStringExtra("msg") != null){
            Alerter.create(this)
                    .setTitle("Επιτυχές").setText("Η Βάση Δεδομένων "+ getIntent().getStringExtra("msg") +" διαγράφτηκε επιτυχώς").show();
        }

        binding.backOnMain.setOnClickListener(t -> {
            Intent i = new Intent(this , MainActivity.class);
            i.putExtra("flag" , "0");
            startActivity(i);
            finish();
            overridePendingTransition(0,0);
        });
        binding.rv.setItemAnimator(null);
        binding.rv.setHasFixedSize(true);
        binding.rv.setItemViewCacheSize(5);
        binding.rv.addItemDecoration(new DB_item_deco(this));
        binding.rv.setAdapter(new database_adapter(this, dbs, db -> {
            Intent i = new Intent(this , HandleDatabase.class);
            i.putExtra("db" , db);
            i.putExtra("dbs" , dbs);
            startActivity(i);
            finish();
            overridePendingTransition(0,0);
        }));



    }
}
