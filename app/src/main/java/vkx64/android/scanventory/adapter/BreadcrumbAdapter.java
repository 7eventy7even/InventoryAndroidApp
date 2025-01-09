package vkx64.android.scanventory.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import vkx64.android.scanventory.R;
import vkx64.android.scanventory.model.Breadcrumb;

public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.BreadcrumbViewHolder> {

    private final List<Breadcrumb> breadcrumbs;
    private final BreadcrumbClickListener listener;

    public interface BreadcrumbClickListener {
        void onBreadcrumbClick(int position);
    }

    public BreadcrumbAdapter(List<Breadcrumb> breadcrumbs, BreadcrumbClickListener listener) {
        this.breadcrumbs = breadcrumbs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BreadcrumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_breadcrumb, parent, false);
        return new BreadcrumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreadcrumbViewHolder holder, int position) {
        Breadcrumb breadcrumb = breadcrumbs.get(position);
        holder.tvBreadcrumb.setText(breadcrumb.getName());

        // Show divider (/) only if it's not the last item
        if (position < breadcrumbs.size() - 1) {
            holder.tvDivider.setVisibility(View.VISIBLE);
        } else {
            holder.tvDivider.setVisibility(View.GONE);
        }

        // Disable click listener for the current breadcrumb head
        if (position == breadcrumbs.size() - 1) {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBreadcrumbClick(position);
            });
            holder.itemView.setClickable(true);
        }
    }

    @Override
    public int getItemCount() {
        return breadcrumbs.size();
    }

    static class BreadcrumbViewHolder extends RecyclerView.ViewHolder {
        TextView tvBreadcrumb, tvDivider;

        public BreadcrumbViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBreadcrumb = itemView.findViewById(R.id.tvBreadcrumb);
            tvDivider = itemView.findViewById(R.id.tvDivider);
        }
    }
}