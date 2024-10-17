package gr.app.JNITHESIS.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import gr.app.JNITHESIS.Consts;
import gr.app.JNITHESIS.DoubleClickListener;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.adapters.table_adapter;
import gr.app.JNITHESIS.adapters.table_field_adapter;
import gr.app.JNITHESIS.databinding.ActivityHandleDatabaseBinding;
import gr.app.JNITHESIS.models.Database;
import gr.app.JNITHESIS.models.Field;
import gr.app.JNITHESIS.models.Table;
import gr.app.JNITHESIS.requests.JRequests;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HandleDatabase extends AppCompatActivity {
    private static String server_port = Consts.server_port;
    private static String server_host = Consts.server_host;


    private ActivityHandleDatabaseBinding binding;
    private Database db;
    private ArrayList<Database> dbs;
    private table_adapter adp;
    private ArrayList<Table> tables = new ArrayList<>();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityHandleDatabaseBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbs = (ArrayList<Database>) getIntent().getSerializableExtra("dbs");
        db = (Database)getIntent().getSerializableExtra("db");
        binding.backOnMain.setOnClickListener(t -> {
            Intent i = new Intent(this , EditDatabases.class);
            i.putExtra("dbs" , dbs);
            startActivity(i);
            finish();
            overridePendingTransition(0,0);
        });
        binding.dbName.setText("Βάση: " + db.name);
        set_tables();
        handle_create_new_table();
        handle_drop_database();
    }

    private void handle_drop_database(){
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.drop_database_sheet, null));
        d.findViewById(R.id.drop_database_btn).setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                d.cancel();
                drop_db(db.name);
            }
        });

        binding.dropDatabase.setOnClickListener(t -> {
            d.show();
        });
    }

    private ArrayList<Field> field_list_on_create = new ArrayList<Field>();
    private table_field_adapter field_adapter;
    void handle_create_new_table(){


        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.create_table_sheet, null));

        AtomicBoolean has_primary_key = new AtomicBoolean(false);

        field_adapter = new table_field_adapter(this , field_list_on_create, field -> {
            int pos = field_list_on_create.indexOf(field);
            if(pos != -1){
                Field f = field_list_on_create.get(0);
                if(f.primary_key) has_primary_key.set(false);

                field_list_on_create.remove(pos);
                field_adapter.notifyItemRemoved(pos);
            }
        });
        RecyclerView field_rv = d.findViewById(R.id.field_rv);
        field_rv.setAdapter(field_adapter);

        /* ------------------------------------------------------------------------ */
        CheckBox nullable_box = d.findViewById(R.id.field_nullable);
        CheckBox Primary_box = d.findViewById(R.id.field_Primary);
        CheckBox Unique_box = d.findViewById(R.id.field_Unique);
        EditText table_name = (EditText) d.findViewById(R.id.table_name);
        EditText field_name = (EditText) d.findViewById(R.id.field_name);
        Spinner dropdown_list = d.findViewById(R.id.field_type_dropdown_list);
        /* ------------------------------------------------------------------------ */


        String[] types = new String[]{Consts.Field_Types.INT, Consts.Field_Types.TEXT};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        dropdown_list.setAdapter(adapter);

        // Δημιουργία Πεδίων
        d.findViewById(R.id.create_new_field).setOnClickListener(create_new_field -> {

            Boolean nullable = nullable_box.isChecked();
            Boolean primary_key = Primary_box.isChecked();
            Boolean isUnique = Unique_box.isChecked();
            String f_name = field_name.getText().toString();
            String f_type  = dropdown_list.getSelectedItem().toString();
            if(f_name.isEmpty() || f_name.trim().isEmpty()){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Ορίστε κάποιο όνομα πεδίου").show();
                return;
            }
            if(f_type.equals(Consts.Field_Types.TEXT) && primary_key == true){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Δεν μπορείς να έχεις Auto Increment και TEXT ως τύπος πεδίου").show();
                return;
            }
            if (field_list_on_create.stream().anyMatch(ti -> ti.name.toLowerCase().equals(f_name.toLowerCase()))){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Το πεδίο " + f_name + " υπάρχει ήδη, δοκιμάστε διαφορετικό όνομα").show();
                return;
            }
            if(has_primary_key.get() && primary_key){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Μπορείς να έχεις μόνο ένα πεδίο ως (Primary Key)").show();
                return;
            }

            if(nullable && primary_key){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Δεν μπορείς να έχει Primary Key και Nullable ταυτόχρονα.").show();
                return;
            }

            Field new_field = new Field();
            new_field.name = f_name;
            new_field.type = f_type;
            new_field.nullable = nullable;
            new_field.primary_key = primary_key;
            new_field.isUnique = isUnique;


            if(primary_key && !has_primary_key.get()) {has_primary_key.set(true); new_field.primary_key = true;}
            field_list_on_create.add(new_field);
            field_adapter.notifyItemInserted(field_list_on_create.size() - 1);
        });

        // Δημιουργία Ολόκληρου Πίνακα στην ΒΔ.
        d.findViewById(R.id.create_table_final).setOnClickListener(t -> {
            String table_name_tmp = table_name.getText().toString();
            if(table_name_tmp.isEmpty() || table_name_tmp.trim().isEmpty()){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Ορίστε κάποιο Όνομα Πίνακα").show();
                return;
            }
            if(table_name_tmp.length() <= 2){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Το όνομα πίνακα θα πρεπει να είναι τουλαχιστον 3 χαρακτήρες").show();
                return;
            }
            if (tables.stream().anyMatch(ti -> ti.name.toLowerCase().equals(table_name_tmp.toLowerCase()))){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Ο Πίνακας με αυτό το όνομα υπάρχει ήδη").show();

                return;
            }
            if(field_list_on_create.isEmpty()){
                Alerter.create(d)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Προσθέστε τουλάχιστον 1 πεδίο!").show();
                return;
            }

            /** Πρέπει να Δημιουργίσω την SQL Query **/
            send_sql_query(table_name.getText().toString(),nullable_box,Primary_box,Unique_box,table_name,field_name,has_primary_key);
            Alerter.create(this).setTitle("Επιτυχές").setText("Ο Πίνακας δημιουργήθηκε!").show();
            d.cancel();
            field_list_on_create.clear();
            reset_table_insertion_fields(nullable_box,Primary_box,Unique_box,table_name,field_name,has_primary_key);
        });

        d.setOnCancelListener(cancel -> {
            field_list_on_create.clear();
            reset_table_insertion_fields(nullable_box,Primary_box,Unique_box,table_name,field_name,has_primary_key);
        });

        binding.createTableBtn.setOnClickListener(t -> {has_primary_key.set(false); d.show();});
    }

    void reset_table_insertion_fields(CheckBox nullable_box,
                                      CheckBox Primary_box,
                                      CheckBox Unique_box,
                                      EditText table_name,
                                      EditText field_name,
                                      AtomicBoolean has_primary_key){
        nullable_box.setChecked(false);
        Primary_box.setChecked(false);
        Unique_box.setChecked(false);
        table_name.setText("");
        field_name.setText("");
        has_primary_key.set(false);
    }



    void drop_db(String db){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().drop_database(db))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        dbs.removeIf(dbs -> dbs.getName().equals(db));
                        runOnUiThread(() ->{
                            Intent i = new Intent(getBaseContext() , EditDatabases.class);
                            i.putExtra("dbs" , dbs);
                            i.putExtra("msg" , db);
                            startActivity(i);
                            overridePendingTransition(0,0);
                        });
                    }

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
                        .setTitle("Σφάλμα").setText("Πρόβλημα: " + e.getMessage()).show();
                //throw new RuntimeException(e);
            }
        }).start();
    }


    void send_sql_query(String tb, CheckBox nullable_box,
                        CheckBox Primary_box,
                        CheckBox Unique_box,
                        EditText table_name,
                        EditText field_name,
                        AtomicBoolean has_primary_key){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().create_table_on_db(db.name ,tb, field_list_on_create))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        Log.d("DB" , body);
                        set_tables();
                    }

                }else{
                    String err = response.body().string();
                    Alerter.create(this)
                            .setIcon(R.drawable.error_box)
                            .setIconColorFilter(Color.parseColor("#F0706A"))
                            .setTitle("Σφάλμα").setText(err).show();

                }
            } catch (IOException e) {
                field_list_on_create.clear();
                reset_table_insertion_fields(nullable_box,Primary_box,Unique_box,table_name,field_name,has_primary_key);

                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Πρόβλημα: " + e.getMessage()).show();

            }
        }).start();
    }


    void set_tables(){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().fetch_database_tables(db.name))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        tables = get_tables(body);
                        adp = new table_adapter(this , tables , tb -> {
                            Intent table_intent = new Intent(this , HandleTable.class);
                            table_intent.putExtra("tb" , tb);
                            table_intent.putExtra("db" , db);
                            table_intent.putExtra("dbs" , dbs);

                            startActivity(table_intent);
                            overridePendingTransition(0,0);
                        });
                        runOnUiThread(() -> binding.tablesRv.setAdapter(adp));
                    }

                }else{
                    String err = response.body().string();
                    Alerter.create(this)
                            .setIcon(R.drawable.error_box)
                            .setIconColorFilter(Color.parseColor("#F0706A"))
                            .setTitle("Σφάλμα").setText(err).show();

                }
            } catch (IOException | JSONException e) {
                // Perhaps server is still not ready.
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText("Πρόβλημα: " + e.getMessage()).show();
                //throw new RuntimeException(e);
            }
        }).start();
    }

    private ArrayList<Table> get_tables(String in) throws JSONException {
        ArrayList<Table> list = new ArrayList<>();
        JSONArray array = new JSONArray(in);
        for(int i=0; i<array.length(); i++){
            JSONObject obj = array.getJSONObject(i);
            Table t = new Table();
            t.name = obj.getString("table");
            t.size = obj.getString("size");
            list.add(t);
        }
        return list;
    }



}
