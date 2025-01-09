package vkx64.android.scanventory.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vkx64.android.scanventory.R;
import vkx64.android.scanventory.database.TableOrders;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<TableOrders> orderList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TableOrders order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HistoryAdapter(List<TableOrders> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TableOrders order = orderList.get(position);

        holder.tvOrderId.setText("Order ID #" + order.getOrder_id());
        holder.tvOrderDate.setText(order.getOrder_date());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(order);
        });
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        }
    }

    public int getItemCount() {
        return orderList.size();
    }

    public void updateData(List<TableOrders> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

}
