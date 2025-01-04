package vkx64.android.scanventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vkx64.android.scanventory.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

    private final Context context;
    private final List<String> imagePaths;
    private final OnAddImageClickListener addImageClickListener;
    private final OnImageClickListener imageClickListener;

    public interface OnAddImageClickListener {
        void onAddImageClick();
    }

    public interface OnImageClickListener {
        void onImageClick(String imagePath);
    }

    public GalleryAdapter(Context context, List<String> imagePaths, OnAddImageClickListener addImageClickListener, OnImageClickListener imageClickListener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.addImageClickListener = addImageClickListener;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position == 0) {
            holder.bindPlusButton();
        } else {
            holder.bindImage(imagePaths.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size() + 1; // Include the "plus button"
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        void bindImage(String imagePath) {
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.im_placeholder)
                    .into(ivImage);

            ivImage.setOnClickListener(v -> {
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(imagePath);
                }
            });
        }

        void bindPlusButton() {
            ivImage.setImageResource(R.drawable.ic_square_plus);

            ivImage.setOnClickListener(v -> {
                if (addImageClickListener != null) {
                    addImageClickListener.onAddImageClick();
                }
            });
        }
    }
}
