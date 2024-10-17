package gr.app.JNITHESIS.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gr.app.JNITHESIS.R;
import gr.app.JNITHESIS.models.Database;

public class database_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Database> list;
    private OnItemClickListener listener;
    public database_adapter(Context context, ArrayList<Database> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }


    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) return new empty_holder(LayoutInflater.from(context).inflate(R.layout.adapter_database_layout_empty, parent, false));
        return new default_holder(LayoutInflater.from(context).inflate(R.layout.adapter_database_layout, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof default_holder) ((default_holder) holder).bind(list.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if(list.isEmpty()) return 0;
        return 1;
    }

    @Override public int getItemCount() {return list.isEmpty() ? 1 : list.size();}

    public class default_holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name,collation;
        public default_holder(@NonNull View i) {
            super(i);
            name = i.findViewById(R.id.name);
            collation = i.findViewById(R.id.collation);
            i.setOnClickListener(this);
        }
        public void bind(Database db){
            name.setText(db.name);
            collation.setText(db.collation);
            itemView.setTag(db);
        }
        @Override  public void onClick(View v) { if (listener != null) listener.onItemClick((Database) itemView.getTag()); }
    }

    public class empty_holder extends RecyclerView.ViewHolder{
        public empty_holder(@NonNull View i) {super(i);}
    }

    public interface OnItemClickListener {void onItemClick(Database db);}


}
