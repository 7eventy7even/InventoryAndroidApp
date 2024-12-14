package vkx64.upwork.inventory_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import vkx64.upwork.inventory_management.R;
import vkx64.upwork.inventory_management.database.InventoryItem;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
    private List<InventoryItem> itemList;
    private OnItemClickListener onItemClickListener;

    public InventoryAdapter(List<InventoryItem> itemList) {
        this.itemList = itemList;
    }

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rootLayout;
        TextView tvProductName, tvProductCategory, tvUpdatedDate, tvSellingCount;
        ImageView ivProductImage;

        public ViewHolder(View view) {
            super(view);
            rootLayout = view.findViewById(R.id.rootLayout);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductCategory = view.findViewById(R.id.tvProductCategory);
            tvUpdatedDate = view.findViewById(R.id.tvUpdatedDate);
            tvSellingCount = view.findViewById(R.id.tvSellingCount);
            ivProductImage = view.findViewById(R.id.ivProductImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        String SellingCount = "Selling: " + item.selling_quantity + " / " + item.storage_quantity;
        String UpdatedDate = "Updated at: " + item.date_updated;
        String imageFileName = item.product_id + ".jpg";
        File imageFile = new File(holder.itemView.getContext().getFilesDir(), "Images/" + imageFileName);

        holder.tvProductName.setText(item.product_name);
        holder.tvProductCategory.setText(item.product_category);
        holder.tvUpdatedDate.setText(UpdatedDate);
        holder.tvSellingCount.setText(SellingCount);

        if (imageFile.exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageFile)
                    .into(holder.ivProductImage);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_image)
                    .into(holder.ivProductImage);
        }

        holder.rootLayout.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

