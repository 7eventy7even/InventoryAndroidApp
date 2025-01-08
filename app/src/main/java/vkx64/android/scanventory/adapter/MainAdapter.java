package vkx64.android.scanventory.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import vkx64.android.scanventory.R;
import vkx64.android.scanventory.database.TableGroups;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.utilities.FileHelper;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GROUP = 1;
    private static final int VIEW_TYPE_PRODUCT = 2;
    private static final Logger log = LogManager.getLogger(MainAdapter.class);

    private final Context context;
    private final List<Object> items;
    private final MainClickListener mainClickListener;

    public interface MainClickListener {
        void onGroupClick(TableGroups group);
        void onProductClick(TableItems item);
        void onGroupLongClick(TableGroups group); // Long press for groups
        void onProductLongClick(TableItems item); // Long press for items

        void onThreeDotMenuClick(TableGroups group);
    }

    public MainAdapter(Context context, List<Object> items, MainClickListener listener) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.mainClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof TableGroups) return VIEW_TYPE_GROUP;
        if (items.get(position) instanceof TableItems) return VIEW_TYPE_PRODUCT;
        throw new IllegalStateException("Unknown item type at position " + position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view, mainClickListener);
        } else {
            View view = inflater.inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view, mainClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).bind((TableGroups) items.get(position));
        } else if (holder instanceof ProductViewHolder) {
            ((ProductViewHolder) holder).bind((TableItems) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Object> newItems) {
        this.items.clear();
        if (newItems != null) this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    // Group ViewHolder
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvGroupName;
        private final ImageView ivGroupIcon;
        private final MainClickListener listener;
        private final ImageButton ibThreeDotMenu;

        public GroupViewHolder(@NonNull View itemView, MainClickListener listener) {
            super(itemView);
            this.listener = listener;
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            ivGroupIcon = itemView.findViewById(R.id.ivGroupImage);
            ibThreeDotMenu = itemView.findViewById(R.id.ibThreeDotMenu);
        }

        public void bind(TableGroups group) {
            tvGroupName.setText(group.getGroup_name());

            String groupImagePath = FileHelper.getGroupImage(itemView.getContext(), group.getGroup_id());
            if (groupImagePath != null) {
                Glide.with(itemView.getContext())
                        .load(new File(groupImagePath))
                        .placeholder(R.drawable.ic_folders)
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .into(ivGroupIcon);
            } else {
                ivGroupIcon.setImageResource(R.drawable.ic_folders);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onGroupClick(group);
            });

            itemView.setOnLongClickListener(v -> {
                // Exclude "..." folder from long press actions
                if (!"...".equals(group.getGroup_name()) && listener != null) {
                    listener.onGroupLongClick(group);
                }
                return true; // Consume the event
            });

            ibThreeDotMenu.setOnClickListener(v -> {
                if (listener != null) listener.onThreeDotMenuClick(group); // Trigger the listener
            });
        }
    }

    // Product ViewHolder
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProductImage;
        private final TextView tvProductName, tvSellingCount;
        private final MainClickListener listener;

        public ProductViewHolder(@NonNull View itemView, MainClickListener listener) {
            super(itemView);
            this.listener = listener;
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSellingCount = itemView.findViewById(R.id.tvSellingCount);
        }

        public void bind(TableItems item) {
            tvProductName.setText(item.getItem_name());
            tvSellingCount.setText("Selling: " + item.getItem_selling() + "/" + item.getItem_storage());

            // Fetch the primary image for the item
            String primaryImagePath = FileHelper.getPrimaryImageForItem(itemView.getContext(), item.getItem_id());
            Log.d("MainAdapter", "ImagePath: " + primaryImagePath);
            if (primaryImagePath != null) {
                // Load the primary image using Glide
                Glide.with(itemView.getContext())
                        .load(new File(primaryImagePath))
                        .placeholder(R.drawable.im_placeholder)
                        .into(ivProductImage);
            } else {
                // Use the placeholder image if no primary image is found
                ivProductImage.setImageResource(R.drawable.im_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(item);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onProductLongClick(item);
                return true;
            });
        }
    }
}

