package gr.app.JNITHESIS.recycler_item_deco;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class TABLE_COLUMNS_DECO extends RecyclerView.ItemDecoration{


        int spacing;
        public TABLE_COLUMNS_DECO(Context context, int spacing) {
            this.spacing=spacing;
        }
        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=spacing;
            outRect.right=spacing;
        }


}
