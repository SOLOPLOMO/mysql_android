package gr.app.JNITHESIS.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.models.Field;

public class table_field_adapter extends RecyclerView.Adapter<table_field_adapter.default_holder> {
    private Context context;
    private ArrayList<Field> list;
    private OnItemClickListener listener;
    public table_field_adapter(Context context, ArrayList<Field> list,OnItemClickListener listener){
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override public default_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new default_holder(LayoutInflater.from(context).inflate(R.layout.adapter_table_field_layout, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull default_holder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override public int getItemCount() {
        return list.isEmpty() ? 0 : list.size();
    }

    public class default_holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name,type,nullable,unique;
        public default_holder(@NonNull View i) {
            super(i);
            name = i.findViewById(R.id.name);
            type = i.findViewById(R.id.type);
            nullable = i.findViewById(R.id.nullable);
            unique = i.findViewById(R.id.unique);
            i.setOnClickListener(this);
        }
        public void bind(Field field){
            name.setText(field.name);


            if(field.primary_key) {
                Drawable img = context.getResources().getDrawable(R.drawable.primary_key);
                img.setBounds(0, 0, 60, 60);
                name.setCompoundDrawables(img, null, null, null);
            }
            type.setText(field.type);
            nullable.setText(field.nullable == true ? "TRUE" : "FALSE");
            unique.setText(field.isUnique == true ? "TRUE" : "FALSE");
            itemView.setTag(field);
        }
        @Override  public void onClick(View v) { if (listener != null) listener.onItemClick((Field) itemView.getTag()); }
    }
    public interface OnItemClickListener {void onItemClick(Field field);}
}
