package gr.app.JNITHESIS.recycler_item_deco;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class DB_item_deco extends RecyclerView.ItemDecoration {

    private final int decorationHeight;
    private Context context;

    public DB_item_deco(Context context) {
        this.context = context;
        decorationHeight = dip(2.5f);
    }

    public int dip(float pixel)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixel * scale + 0.5f);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent != null && view != null) {

            int itemPosition = parent.getChildAdapterPosition(view);
            int totalCount = parent.getAdapter().getItemCount();

            if (itemPosition >= 0 && itemPosition < totalCount - 1) {
                outRect.bottom = decorationHeight;
            }

        }

    }
}
