package gr.app.JNITHESIS.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.app.JNITHESIS.Consts;
import gr.app.JNITHESIS.DoubleClickListener;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.databinding.ActivityHandleTableBinding;
import gr.app.JNITHESIS.models.Database;
import gr.app.JNITHESIS.models.Field;
import gr.app.JNITHESIS.models.Table;
import gr.app.JNITHESIS.requests.JRequests;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HandleTable extends AppCompatActivity {
    private static String server_port = Consts.server_port;
    private static String server_host = Consts.server_host;

    private ActivityHandleTableBinding binding;
    private Table tb;
    private Database db;
    private ArrayList<Database> dbs;
    ArrayList<Field> Fields = new ArrayList<Field>();
    private String column_in_delete_condition = null;
    int TABLE_ROW_MARGIN_LEFT = 16;
    int TABLE_ROW_MARGIN_RIGHT = 16;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityHandleTableBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.backOnMain.setOnClickListener(t -> {finish(); overridePendingTransition(0,0);});

        tb = (Table)getIntent().getSerializableExtra("tb");
        db = (Database) getIntent().getSerializableExtra("db");
        dbs = (ArrayList<Database>) getIntent().getSerializableExtra("dbs");

        binding.tableNameHeader.setText("Πίνακας: " + tb.name);

        fetch_table_columns();
        handle_drop_this_table();
    }

    void handle_drop_this_table(){
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.drop_table_sheet, null));
        d.findViewById(R.id.drop_table_btn).setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                drop_table(db.name,tb.name);
            }
        });

        binding.dropTable.setOnClickListener(t -> {
            d.show();
        });
    }
    void handle_insert_new_data_into_table(){
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.insert_new_data_into_table_sheet, null));
        LinearLayout parent = d.findViewById(R.id.parent);
        for(Field f : Fields){
            if(f.primary_key) continue;
            parent.addView(ColumnView(d,f));
            parent.addView(ColumnInput(d,f));
        }
        d.findViewById(R.id.append_new_data_btn).setOnClickListener(t -> {
            JSONArray parsed_data = new JSONArray();
            ArrayList<String> cols = new ArrayList<>();

            for(int i=0; i<parent.getChildCount(); i++){
                View v = parent.getChildAt(i);
                if(v instanceof EditText){
                    EditText input = (EditText) v;
                    String text = input.getText().toString();
                    String tag = input.getTag(R.id.field_nullable).toString();
                    String type = input.getTag(R.id.field_type).toString();
                    if(tag.equals("0")){
                        if(text.trim().isEmpty() || text.isEmpty()){
                            input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                            return;
                        }else input.setBackground(getDrawable(R.drawable.edit_text_view));
                        if (!type.equals("text")) {
                            try {Integer.parseInt(text);} catch (NumberFormatException e) {
                                input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                                Alerter.create(this)
                                        .setDuration(1500)
                                        .setIcon(R.drawable.error_box)
                                        .setIconColorFilter(Color.parseColor("#F0706A"))
                                        .setTitle("Σφάλμα").setText("Λάθος τύπος του πεδίου. Δοκιμάστε τύπο " + type + " μονο").show();

                                return;
                            }
                        }
                        try {
                            JSONObject json_object = new JSONObject();
                            json_object.put("data" , text);
                            json_object.put("field" , type);
                            parsed_data.put(json_object);
                            cols.add(input.getTag(R.id.field_name).toString());
                        } catch (JSONException e) {
                            Alerter.create(this)
                                    .setDuration(1500)
                                    .setIcon(R.drawable.error_box)
                                    .setIconColorFilter(Color.parseColor("#F0706A"))
                                    .setTitle("Σφάλμα").setText(e.getMessage()).show();
                            return;
                        }

                    }else{
                        // Field can be empty::null but we check if it is not empty.
                        if(!text.trim().isEmpty() || !text.isEmpty()) {
                            if (!type.equals("text")) {
                                try {Integer.parseInt(text);} catch (NumberFormatException e) {
                                    input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                                    Alerter.create(this)
                                            .setDuration(1500)
                                            .setIcon(R.drawable.error_box)
                                            .setIconColorFilter(Color.parseColor("#F0706A"))
                                            .setTitle("Σφάλμα").setText("Λάθος τύπος του πεδίου. Δοκιμάστε τύπο " + type + " μονο").show();

                                    return;
                                }
                            }
                            try {
                                JSONObject json_object = new JSONObject();
                                json_object.put("data" , text);
                                json_object.put("field" , type);
                                parsed_data.put(json_object);
                                cols.add(input.getTag(R.id.field_name).toString());
                            } catch (JSONException e) {
                                Alerter.create(this)
                                        .setDuration(1500)
                                        .setIcon(R.drawable.error_box)
                                        .setIconColorFilter(Color.parseColor("#F0706A"))
                                        .setTitle("Σφάλμα").setText(e.getMessage()).show();
                                return;
                            }

                        }
                    }
                }
            }


            insert_new_data_into_table(cols,parsed_data);
            d.cancel();
        });
        binding.insertNewData.setOnClickListener(t -> {
            d.show();
       });
    }
    void insert_new_data_into_table(ArrayList<String> cols, JSONArray parsed_data){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = null;
            try {
                request = new Request.Builder()
                        .post( new JRequests().insert_new_data_into_table(db.name ,tb.name,cols,parsed_data))
                        .url(server_host + server_port).build();
            } catch (JSONException e) {
                Alerter.create(this)
                        .setDuration(1500)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText(e.getMessage()).show();
                return;
            }
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        Log.d("TABLE" , body);
                        fetch_table_columns();
                    }
                }else{
                    String err = response.body().string();
                    if(err.contains("Duplicate entry")){
                        Log.d("Err" , err);
                        String regex = "Duplicate entry '(.*?)' for key '(.*?)'";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(err);

                        if (matcher.find()) {
                            String entry = matcher.group(1);
                            String field = matcher.group(2);
                            Alerter.create(this)
                                    .setIcon(R.drawable.error_box)
                                    .setIconColorFilter(Color.parseColor("#F0706A"))
                                    .setTitle("Σφάλμα").setText("Δεν μπορείτε να έχετε ίδιες τιμές ("+ entry +") στο πεδίο " + field).show();
                        } else {
                            Alerter.create(this)
                                    .setIcon(R.drawable.error_box)
                                    .setIconColorFilter(Color.parseColor("#F0706A"))
                                    .setTitle("Σφάλμα").setText("Δεν μπορείτε να έχετε ίδιες τιμές!").show();
                        }


                    }

//                    Alerter.create(this)
//                            .setIcon(R.drawable.error_box)
//                            .setIconColorFilter(Color.parseColor("#F0706A"))
//                            .setTitle("Σφάλμα").setText("EDW" + err).show();

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
    void drop_table(String db,String tb){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().drop_table(db,tb))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){

                        runOnUiThread(() ->{
                            Intent i = new Intent(getBaseContext() , HandleDatabase.class);
                            i.putExtra("db" , this.db);
                            i.putExtra("dbs" , this.dbs);
                            startActivity(i);
                            finish();
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
    HashMap<String,Integer> column_width = new HashMap<>();
    void fetch_table_columns(){
        Fields.clear();
        runOnUiThread(() -> binding.tableLayout.removeAllViews());
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().fetch_table_COLUMNS(db.name ,tb.name))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        TableRow tr = new TableRow(this);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        //lp.setMargins(TABLE_ROW_MARGIN_LEFT,0,TABLE_ROW_MARGIN_RIGHT,0);
                        tr.setLayoutParams(lp);
                        TypedValue outValue = new TypedValue();
                        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                        tr.setBackground(getDrawable(outValue.resourceId));
                        tr.setClickable(true);
                        tr.setPadding(32,16,32,16);
                        try {
                            JSONArray cols_array = new JSONArray(body);
                            for(int i=0; i<cols_array.length(); i++){
                                JSONObject field_obj = cols_array.getJSONObject(i);
                                TextView c = new TextView(this);
                                c.setText(field_obj.getString("Field"));
                                c.setTypeface(Typeface.DEFAULT_BOLD);
                                c.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                c.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                tr.addView(c);
                                runOnUiThread(() -> c.post(() -> {
                                    c.setWidth(c.getWidth() + 256);
                                    column_width.put(c.getText().toString() , c.getWidth());
                                }));
                                Field field = new Field();
                                field.primary_key = field_obj.getString("Key").contains("PRI") ? true : false;
                                field.name =  field_obj.getString("Field");
                                field.nullable = field_obj.getString("Null").equals("NO") ? false : true;
                                field.type = field_obj.getString("Type");
                                field.isUnique = field_obj.getString("Key").contains("UNI") ? true : false;

                                Log.d("OBJ" , field.toString());
                                if(field.primary_key) column_in_delete_condition = field.name;
                                Fields.add(field);
                            }


                            if(column_in_delete_condition == null){
                                String col = Fields.stream()
                                        .filter(field -> field.isUnique)
                                        .findFirst()
                                        .orElse(Fields.get(0)).name;
                                column_in_delete_condition = col;
                            }

                            Log.d("OBJ" , "COLUMN IN COND: " + column_in_delete_condition);
                        } catch (JSONException e) {Alerter.create(this).setText(e.getMessage()).show();}
                        runOnUiThread(() -> binding.tableLayout.addView(tr));
                        runOnUiThread(() -> handle_insert_new_data_into_table());
                        fetch_table_data();
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
    void fetch_table_data(){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .post( new JRequests().fetch_table_data(db.name ,tb.name))
                    .url(server_host + server_port).build();
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        try {
                            JSONArray array = new JSONArray(body);
                            for(int i=0; i<array.length(); i++){
                                // Δημιουργία Row
                                TableRow tr = new TableRow(this);
                                if(i%2 == 0) tr.setBackground(getDrawable(R.drawable.ripple_table_row_white));
                                else tr.setBackground(getDrawable(R.drawable.ripple_table_row_blue));
                                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(TABLE_ROW_MARGIN_LEFT,0,TABLE_ROW_MARGIN_RIGHT,0);
                                tr.setLayoutParams(lp);
                                tr.setClickable(true);
                                tr.setPadding(32,16,32,16);

                                JSONObject object = array.getJSONObject(i);
                                Iterator<String> keys= object.keys();
                                String data_in_condition = "";
                                HashMap<String,String> map = new HashMap<>();
                                while (keys.hasNext())
                                {
                                    String keyValue = (String)keys.next();
                                    String valueString = object.getString(keyValue);
                                    map.put(keyValue,valueString);

                                    if(keyValue.equals(column_in_delete_condition)) data_in_condition = valueString;
                                    TextView c = new TextView(this);
                                    c.setMaxWidth(128);
                                    c.setText(valueString);
                                    c.setSingleLine(true);
                                    c.setEllipsize(TextUtils.TruncateAt.END);
                                    c.setPadding(0,0,64,0);
                                    tr.addView(c);

                                }



                                Log.d("MAP" , "COLUMN: " + column_in_delete_condition);
                                Log.d("MAP" , map.toString());

                                String finalData_in_condition = data_in_condition;
                                tr.setOnClickListener(v -> {
                                    open_table_row_edit_sheet(finalData_in_condition, map);
                                });
                                runOnUiThread(() -> binding.tableLayout.addView(tr));
                            }
                            runOnUiThread(() ->  edit_table_fields());
                        } catch (JSONException e) {
                            Alerter.create(this)
                                    .setIcon(R.drawable.error_box)
                                    .setIconColorFilter(Color.parseColor("#F0706A"))
                                    .setTitle("Σφάλμα").setText(e.getMessage()).show();
                        }
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
    private LinearLayout ColumnView(BottomSheetDialog d, Field f){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,16,0,2);
        LinearLayout layout = new LinearLayout(d.getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);
        LinearLayout.LayoutParams Tparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t = new TextView(d.getContext());
        t.setText(f.name + " " + (!f.nullable ? "*" : ""));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(15);
        t.setLayoutParams(Tparams);
        LinearLayout.LayoutParams s = new LinearLayout.LayoutParams(0, 1,1);
        View space = new View(d.getContext());
        space.setLayoutParams(s);
        LinearLayout.LayoutParams Tfparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t2 = new TextView(d.getContext());
        t2.setText(f.type);
        t2.setTypeface(Typeface.DEFAULT_BOLD);
        t2.setTextSize(15);
        t2.setLayoutParams(Tfparams);
        layout.addView(t);
        layout.addView(space);
        layout.addView(t2);
        return layout;
    }
    private EditText ColumnInput(BottomSheetDialog d, Field f){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText t = new EditText(d.getContext());
        t.setSingleLine(false);
        t.setBackground(getDrawable(R.drawable.edit_text_view));
        t.setTextSize(14);
        t.setTextColor(getColor(R.color.black));
        t.setLayoutParams(params);
        if(f.nullable)t.setTag(R.id.field_nullable,"1"); else t.setTag(R.id.field_nullable,"0");
        t.setTag(R.id.field_type,f.type);
        t.setTag(R.id.field_name , f.name);
        return t;
    }
    private LinearLayout ColumnViewV2(BottomSheetDialog d, Field f){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,16,0,2);
        LinearLayout layout = new LinearLayout(d.getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);
        LinearLayout.LayoutParams Tparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t = new TextView(d.getContext());
        t.setText(f.name + " " + (!f.nullable ? "*" : ""));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(15);
        t.setLayoutParams(Tparams);
        LinearLayout.LayoutParams s = new LinearLayout.LayoutParams(0, 1,1);
        View space = new View(d.getContext());
        space.setLayoutParams(s);
        LinearLayout.LayoutParams Tfparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t2 = new TextView(d.getContext());
        t2.setText(f.type);
        t2.setTypeface(Typeface.DEFAULT_BOLD);
        t2.setTextSize(15);
        t2.setLayoutParams(Tfparams);
        layout.addView(t);
        layout.addView(space);
        layout.addView(t2);
        return layout;
    }
    private EditText ColumnInputV2(BottomSheetDialog d, Field f, String data){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText t = new EditText(d.getContext());
        t.setSingleLine(false);
        t.setText(data);
        t.setBackground(getDrawable(R.drawable.edit_text_view));
        t.setTextSize(14);
        t.setTextColor(getColor(R.color.black));
        t.setLayoutParams(params);
        if(f.nullable)t.setTag(R.id.field_nullable,"1"); else t.setTag(R.id.field_nullable,"0");
        t.setTag(R.id.field_type,f.type);
        t.setTag(R.id.field_name , f.name);
        return t;
    }
    private LinearLayout ColumnViewV3(BottomSheetDialog d){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,16,0,2);
        LinearLayout layout = new LinearLayout(d.getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);
        LinearLayout.LayoutParams Tparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t = new TextView(d.getContext());
        t.setText("Name");
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(15);
        t.setLayoutParams(Tparams);
        LinearLayout.LayoutParams s = new LinearLayout.LayoutParams(0, 1,1);
        View space = new View(d.getContext());
        space.setLayoutParams(s);
        LinearLayout.LayoutParams Tfparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView t2 = new TextView(d.getContext());
        t2.setText("Type");
        t2.setTypeface(Typeface.DEFAULT_BOLD);
        t2.setTextSize(15);
        t2.setLayoutParams(Tfparams);
        layout.addView(t);
        layout.addView(space);
        layout.addView(t2);
        return layout;
    }

    //            <Spinner android:background="@drawable/edit_text_view" android:id="@+id/field_type_dropdown_list" android:layout_width="match_parent" android:layout_height="26dp" android:padding="0dp" android:spinnerMode="dropdown"/>
    private LinearLayout ColumnInputV3(BottomSheetDialog d, Field f){
        LinearLayout double_ln = new LinearLayout(d.getContext());
        double_ln.setOrientation(LinearLayout.HORIZONTAL);
        double_ln.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,0.5f);
        EditText t = new EditText(d.getContext());
        t.setSingleLine(false);
        t.setBackground(getDrawable(R.drawable.edit_text_view));
        t.setTextSize(14);
        t.setTextColor(getColor(R.color.black));
        t.setLayoutParams(params);
        if(f.nullable)t.setTag(R.id.field_nullable,"1"); else t.setTag(R.id.field_nullable,"0");
        t.setTag(R.id.field_type , f.type);
        t.setTag(R.id.field_name , f.name);
        t.setText(f.name);


        Spinner type = new Spinner(d.getContext());
        type.setBackground(getDrawable(R.drawable.edit_text_view));
        type.setPadding(0,0,0,0);
        type.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,0.5f));
        String[] types = new String[]{Consts.Field_Types.INT, Consts.Field_Types.TEXT};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        type.setAdapter(adapter);
        if(f.type.equals("text")) type.setSelection(1);

        double_ln.addView(t);
        double_ln.addView(type);


        return double_ln;
    }

    private void remove_data_from_table(String data){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = null;
            try {
                request = new Request.Builder()
                        .post( new JRequests().remove_data_from_table(db.name ,tb.name,column_in_delete_condition,data))
                        .url(server_host + server_port).build();
            } catch (JSONException e) {
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText(e.getMessage()).show();
            }
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()) fetch_table_columns();
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
    private void open_table_row_edit_sheet(String data, HashMap<String,String> map){
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.table_row_edit_sheet, null));
        LinearLayout parent = d.findViewById(R.id.parent);

        for(Field f : Fields){
            if(f.primary_key) continue;
            parent.addView(ColumnViewV2(d,f));
            parent.addView(ColumnInputV2(d,f, map.get(f.name)));
        }

        d.findViewById(R.id.remove_table_row_data).setOnClickListener(t -> {remove_data_from_table(data); d.cancel();});
        d.findViewById(R.id.update_table_row_data).setOnClickListener(t -> {
            String clause_column_in_update = "";
            String clause_column_in_update_value = "";

            ArrayList<String> fields_data = new ArrayList<>();
            for(Field f : Fields){
                if(f.primary_key) {clause_column_in_update = f.name; break;}
                else if(f.isUnique) {clause_column_in_update = f.name; break;}
                clause_column_in_update = f.name;
            } clause_column_in_update_value = map.get(clause_column_in_update);

            for(int i=0; i<parent.getChildCount(); i++){
                View v = parent.getChildAt(i);
                if(v instanceof EditText){
                    EditText input = (EditText) v;
                    String text = input.getText().toString();
                    String tag = input.getTag(R.id.field_nullable).toString();
                    String type = input.getTag(R.id.field_type).toString();
                    if(tag.equals("0")){
                        if(text.trim().isEmpty() || text.isEmpty()){
                            input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                            return;
                        }else input.setBackground(getDrawable(R.drawable.edit_text_view));
                        if (!type.equals("text")) {
                            try {Integer.parseInt(text);} catch (NumberFormatException e) {
                                input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                                Alerter.create(this)
                                        .setDuration(1500)
                                        .setIcon(R.drawable.error_box)
                                        .setIconColorFilter(Color.parseColor("#F0706A"))
                                        .setTitle("Σφάλμα").setText("Λάθος τύπος του πεδίου. Δοκιμάστε τύπο " + type + " μονο").show();
                                return;
                            }
                        }
                    }else{
                        // Field can be empty::null but we check if it is not empty.
                        if(!text.trim().isEmpty() || !text.isEmpty()) {
                            if (!type.equals("text")) {
                                try {Integer.parseInt(text);} catch (NumberFormatException e) {
                                    input.setBackground(getDrawable(R.drawable.edit_text_red_view));
                                    Alerter.create(this)
                                            .setDuration(1500)
                                            .setIcon(R.drawable.error_box)
                                            .setIconColorFilter(Color.parseColor("#F0706A"))
                                            .setTitle("Σφάλμα").setText("Λάθος τύπος του πεδίου. Δοκιμάστε τύπο " + type + " μονο").show();

                                    return;
                                }
                            }
                        }
                    }
                    fields_data.add(text);
                }
            }


            String final_clause_in_update = clause_column_in_update;
            String final_clause_value_in_update = clause_column_in_update_value;

            new Thread(() -> {
                OkHttpClient HttpClient = new OkHttpClient();
                Request request = null;
                try {
                    request = new Request.Builder()
                            .post( new JRequests().update_data_from_table(db.name ,tb.name , final_clause_in_update,final_clause_value_in_update, Fields, fields_data ))
                            .url(server_host + server_port).build();
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    Response response = HttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        String body = response.body().string();
                        if(!body.isEmpty()) fetch_table_columns();
                    }else{
                        String err = response.body().string();
                        if(err.contains("Duplicate entry")){
                            Log.d("Err" , err);
                            String regex = "Duplicate entry '(.*?)' for key '(.*?)'";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(err);
                            if (matcher.find()) {
                                String entry = matcher.group(1);
                                String field = matcher.group(2);
                                Alerter.create(this)
                                        .setIcon(R.drawable.error_box)
                                        .setIconColorFilter(Color.parseColor("#F0706A"))
                                        .setTitle("Σφάλμα").setText("Δεν μπορείτε να έχετε ίδιες τιμές ("+ entry +") στο πεδίο " + field).show();
                            } else {
                                Alerter.create(this)
                                        .setIcon(R.drawable.error_box)
                                        .setIconColorFilter(Color.parseColor("#F0706A"))
                                        .setTitle("Σφάλμα").setText("Δεν μπορείτε να έχετε ίδιες τιμές!").show();
                            }
                        }

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

        });
        d.show();
    }

    private void edit_table_fields(){
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setContentView(LayoutInflater.from(this).inflate(R.layout.update_table_fields_sheet, null));
        LinearLayout parent = d.findViewById(R.id.parent);
        for(Field f : Fields){
            parent.addView(ColumnViewV3(d));
            parent.addView(ColumnInputV3(d,f));
        }


        d.findViewById(R.id.update_fields).setOnClickListener(t -> {
            ArrayList<String> new_field_names = new ArrayList<>();
            ArrayList<String> new_field_types = new ArrayList<>();

            for(int i=0; i<parent.getChildCount(); i++){
                View child = parent.getChildAt(i);
                if(child instanceof LinearLayout){
                    for(int j=0; j<((LinearLayout) child).getChildCount(); j++){
                        View input = ((LinearLayout) child).getChildAt(j);
                        if(input instanceof EditText) new_field_names.add(((EditText) input).getText().toString());
                        else if(input instanceof Spinner){
                            if(((Spinner) input).getSelectedItem().toString().equals("INT")) new_field_types.add("int(11)");
                            else new_field_types.add("TEXT");
                        }
                    }
                }
            }
            UpdateFields(new_field_names,new_field_types);
        });

        binding.editFields.setOnClickListener(t -> d.show());
    }

    private void UpdateFields(ArrayList<String> n_f_n , ArrayList<String> n_f_t){
        new Thread(() -> {
            OkHttpClient HttpClient = new OkHttpClient();
            Request request = null;
            try {
                request = new Request.Builder()
                        .post( new JRequests().drop_index_if_exist_on_update_column_field(db.name ,tb.name, Fields))
                        .url(server_host + server_port).build();
            } catch (JSONException e) {
                Alerter.create(this)
                        .setIcon(R.drawable.error_box)
                        .setIconColorFilter(Color.parseColor("#F0706A"))
                        .setTitle("Σφάλμα").setText(e.getMessage()).show();
            }
            try {
                Response response = HttpClient.newCall(request).execute();
                if(response.isSuccessful()){
                    String body = response.body().string();
                    if(!body.isEmpty()){
                        try {
                            request = new Request.Builder()
                                    .post( new JRequests().update_table_fields(db.name ,tb.name, Fields , n_f_n,n_f_t))
                                    .url(server_host + server_port).build();
                        } catch (JSONException e) {
                            Alerter.create(this)
                                    .setIcon(R.drawable.error_box)
                                    .setIconColorFilter(Color.parseColor("#F0706A"))
                                    .setTitle("Σφάλμα").setText(e.getMessage()).show();
                        }
                        try {
                            response = HttpClient.newCall(request).execute();
                            if(response.isSuccessful()){
                                body = response.body().string();
                                if(!body.isEmpty()) fetch_table_columns();
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

}
