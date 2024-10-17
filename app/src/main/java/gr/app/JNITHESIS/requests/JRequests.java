package gr.app.JNITHESIS.requests;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gr.app.JNITHESIS.models.Field;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class JRequests {


    public RequestBody fetch_schemas(){
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", "SELECT schema_name,DEFAULT_CHARACTER_SET_NAME FROM information_schema.schemata WHERE schema_name NOT IN ('mysql', 'phpmyadmin', 'information_schema' , 'performance_schema')");
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }


    public RequestBody create_database_query(String dbname){
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", "CREATE DATABASE " + dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }


    public RequestBody create_table_on_db(String dbname , String tb , ArrayList<Field> fields){
    //                fields.get(0).A_I;
    //                fields.get(0).name;
    //                fields.get(0).nullable;
    //                fields.get(0).type;


        StringBuilder s = new StringBuilder("CREATE TABLE " + tb + " (");
        String primary_key_field = null;
        boolean hasUnique = false;
        List<String> UniqieFields = new ArrayList<>();

        // Loop through the fields to build the column definitions
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            s.append(field.name).append(" ").append(field.type);
            if (!field.nullable) s.append(" NOT NULL");
            if (field.primary_key) s.append(" AUTO_INCREMENT");
            if(field.primary_key) if(primary_key_field == null)  primary_key_field = field.name;
            if (field.isUnique){
                hasUnique = true;
                UniqieFields.add(field.name);
            };


            if (i < fields.size() - 1) s.append(", ");
        }

        if (primary_key_field != null) s.append(", PRIMARY KEY(").append(primary_key_field).append(")");

        if (hasUnique && !UniqieFields.isEmpty()) {
            s.append(",");
            for (int i = 0; i < UniqieFields.size(); i++)
                if(i == UniqieFields.size()-1)
                    s.append(" UNIQUE (" + UniqieFields.get(i) + ")");
                else
                s.append(" UNIQUE (" + UniqieFields.get(i) + "),");
        }

        s.append(");");

        Log.d("STR" , s.toString());

        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody fetch_table_COLUMNS(String dbname, String tb){
        String s = "SHOW COLUMNS FROM " + tb;
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody fetch_table_data(String dbname, String tb){
        String s = "SELECT * FROM " + tb;
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody fetch_database_tables(String dbname){

        String s = "SELECT table_name AS \"table\", " +
                "ROUND(((data_length + index_length) / 1024 / 1024), 2) AS \"size\" " +
                "FROM information_schema.tables " +
                "WHERE table_schema = '" + dbname + "' " +
                "ORDER BY CREATE_TIME DESC;";

        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody drop_database(String dbname){

        String s = "DROP DATABASE " + dbname;
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody drop_table(String dbname,String table){

        String s = "DROP TABLE " + table;
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody insert_new_data_into_table(String dbname, String table, ArrayList<String>cols, JSONArray parsed_data) throws JSONException {

        StringBuilder s = new StringBuilder("INSERT INTO " + table + " (");


        for(int i=0; i<cols.size(); i++){
            if(i==cols.size()-1) s.append(cols.get(i));
            else  s.append(cols.get(i)+",");
        } s.append(") VALUES (");

        for(int i=0; i<parsed_data.length(); i++){
            JSONObject obj = parsed_data.getJSONObject(i);
            String dt = obj.getString("data");
            String field = obj.getString("field");
            if(field.equals("text"))
                if(i == parsed_data.length()-1) s.append("\"" + dt + "\"");
                else s.append("\"" + dt + "\",");
            else{
                if(i == parsed_data.length()-1) s.append(dt);
                else s.append(dt + ",");
            }
        } s.append(")");
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody remove_data_from_table(String dbname, String table, String PRI, String data) throws JSONException {

        String s = "DELETE FROM " + table + " WHERE " + PRI + " = " + data;
        Log.d("DELETE" , s);

        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }

    public RequestBody update_data_from_table(String dbname, String table, String clause ,String clause_value, ArrayList<Field> fields, ArrayList<String> data) throws JSONException {
        int j = 0;
        StringBuilder s = new StringBuilder("UPDATE " + table + " ");
        s.append("SET ");
        for(int i=0; i<fields.size(); i++){
            Field field = fields.get(i);
            if(field.primary_key) continue;

            String type = field.type;
            String col = field.name;
            String val = data.get(j); j++;


            if(i==fields.size()-1){
                if(val.equals("null") || val.isEmpty() || val.trim().isEmpty()) s.append(col + " = " + "NULL");
                else{
                    if(type.equals("text")) s.append(col + " = '" + val + "'");
                    else s.append(col + " = " + val);
                }
            }else{
                if(val.equals("null") || val.isEmpty() || val.trim().isEmpty()) s.append(col + " = " + "NULL,");
                else{
                    if(type.equals("text")) s.append(col + " = \'" + val + "\',");
                    else s.append(col + " = " + val+",");
                }
            }


        }
        s.append(" WHERE " + clause + " = " + clause_value);

        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }


    public RequestBody drop_index_if_exist_on_update_column_field(String dbname, String table, ArrayList<Field> indexes) throws JSONException {

        StringBuilder s = new StringBuilder("ALTER TABLE " + table);
        for(int i=0; i<indexes.size(); i++){
            Field f = indexes.get(i);
            if(f.primary_key) continue;
            if(i==indexes.size()-1)  s.append(" DROP INDEX IF EXISTS " + f.name);
            else if(i == 0)  s.append(" DROP INDEX IF EXISTS " + f.name + ",");
            else  s.append(" DROP INDEX IF EXISTS " + f.name + ",");
        }

        Log.d("UPDATE" , s.toString());

        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }
    public RequestBody update_table_fields(String dbname, String table, ArrayList<Field> fields, ArrayList<String> n_f_n, ArrayList<String> n_f_t) throws JSONException {

        StringBuilder s = new StringBuilder("ALTER TABLE "+  table);
        for(int i=0; i<fields.size(); i++) {
            Field field = fields.get(i);
            if(i==fields.size()-1)
                s.append(" CHANGE COLUMN " + field.name + " " + n_f_n.get(i) + " " + n_f_t.get(i) + " " + (field.isUnique?"UNIQUE ":"") + (field.nullable?"NULL ":"NOT NULL ") + (field.primary_key?"AUTO_INCREMENT ":""));
            else s.append(" CHANGE COLUMN " + field.name + " " + n_f_n.get(i) + " " + n_f_t.get(i) + " " + (field.isUnique?"UNIQUE ":"") + (field.nullable?"NULL ":"NOT NULL ") + (field.primary_key?"AUTO_INCREMENT, ":","));
        }

        Log.d("UPDATE" , s.toString());

        //CHANGE COLUMN ID NEW_ID int(11), CHANGE COLUMN USERNAME NEW_USERNAME TEXT;";
        JSONObject payload = new JSONObject();
        try {
            payload.put("query", s);
            payload.put("dbname", dbname);
        } catch (JSONException e) {return null;}
        RequestBody ReqBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );
        return ReqBody;
    }




}
