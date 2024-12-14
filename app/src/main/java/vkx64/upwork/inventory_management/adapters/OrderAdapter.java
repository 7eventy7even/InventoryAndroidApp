package vkx64.upwork.inventory_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vkx64.upwork.inventory_management.R;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private InventoryAdapter.OnItemClickListener onItemClickListener;

    public OrderAdapter(List<InventoryItem> inventoryItems) {
        this.orderList = orderList;
    }

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public void setOnItemClickListener(InventoryAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderName.setText(order.order_name);
        holder.tvOrderDate.setText(order.order_date);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void setOrderList(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        public TextView tvOrderName;
        public TextView tvOrderDate;

        public OrderViewHolder(View view) {
            super(view);
            tvOrderName = view.findViewById(R.id.tvProductName);
            tvOrderDate = view.findViewById(R.id.tvProductCategory);
        }
    }
}
