package vkx64.upwork.inventory_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vkx64.upwork.inventory_management.database.InventoryItem;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
    private List<InventoryItem> itemList;

    public InventoryAdapter(List<InventoryItem> itemList) {
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductCategory, tvUpdatedDate, tvSellingCount;

        public ViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductCategory = view.findViewById(R.id.tvProductCategory);
            tvUpdatedDate = view.findViewById(R.id.tvUpdatedDate);
            tvSellingCount = view.findViewById(R.id.tvSellingCount);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        String SellingCount = "Selling: " + item.selling_quantity + " / " + item.storage_quantity;
        String UpdatedDate = "Updated at: " + item.date_updated;
        holder.tvProductName.setText(item.product_name);
        holder.tvProductCategory.setText(item.product_category);
        holder.tvUpdatedDate.setText(UpdatedDate);
        holder.tvSellingCount.setText(SellingCount);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

