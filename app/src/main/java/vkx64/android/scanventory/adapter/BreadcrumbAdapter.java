package vkx64.android.scanventory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vkx64.android.scanventory.R;

public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.BreadcrumbViewHolder> {

    private final List<String> breadcrumbs;
    private final BreadcrumbClickListener listener;

    public interface BreadcrumbClickListener {
        void onBreadcrumbClick(int position);
    }

    public BreadcrumbAdapter(List<String> breadcrumbs, BreadcrumbClickListener listener) {
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
        String breadcrumb = breadcrumbs.get(position);
        holder.tvBreadcrumb.setText(breadcrumb);

        // Show divider (/) only if it's not the last item
        if (position < breadcrumbs.size() - 1) {
            holder.tvDivider.setVisibility(View.VISIBLE);
        } else {
            holder.tvDivider.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBreadcrumbClick(position);
        });
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
