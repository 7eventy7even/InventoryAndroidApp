package vkx64.android.scanventory.adapter;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;

import vkx64.android.scanventory.R;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.DaoMarkets;
import vkx64.android.scanventory.database.TableMarkets;

public class MarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Listener Interface to notify changes
    public interface OnMarketChangeListener {
        void onMarketChanged();
    }

    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<TableMarkets> markets;
    private Context context;
    private ExecutorService executor;
    private String itemId;

    private OnMarketChangeListener onMarketChangeListener;

    // Storage value to validate market quantities
    private int itemStorage = 0;

    // Constructor
    public MarketAdapter(Context context, List<TableMarkets> markets, ExecutorService executor, String itemId) {
        this.context = context;
        this.markets = markets;
        this.executor = executor;
        this.itemId = itemId;
    }

    // Setter for the listener
    public void setOnMarketChangeListener(OnMarketChangeListener listener) {
        this.onMarketChangeListener = listener;
    }

    // Setter for itemStorage
    public void setItemStorage(int itemStorage) {
        this.itemStorage = itemStorage;
    }

    @Override
    public int getItemCount() {
        return (markets != null ? markets.size() : 0) + 1; // +1 for the Add button
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_market_add, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_market, parent, false);
            return new MarketViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddViewHolder) {
            AddViewHolder addHolder = (AddViewHolder) holder;
            addHolder.ibAdd.setOnClickListener(v -> {
                // Show dialog to add new market entry
                showAddMarketDialog();
            });
        } else if (holder instanceof MarketViewHolder) {
            // Adjust position since position 0 is the Add button
            int actualPosition = position - 1;
            TableMarkets currentMarket = markets.get(actualPosition);
            MarketViewHolder marketHolder = (MarketViewHolder) holder;

            // Remove existing TextWatchers to prevent multiple calls
            if (marketHolder.etMarket.getTag() instanceof TextWatcher) {
                marketHolder.etMarket.removeTextChangedListener((TextWatcher) marketHolder.etMarket.getTag());
            }
            if (marketHolder.etQuantity.getTag() instanceof TextWatcher) {
                marketHolder.etQuantity.removeTextChangedListener((TextWatcher) marketHolder.etQuantity.getTag());
            }

            // Bind data to EditTexts
            marketHolder.etMarket.setText(currentMarket.getMarket_name());
            marketHolder.etQuantity.setText(String.valueOf(currentMarket.getMarket_quantity()));

            // Handle delete button click
            marketHolder.ibDelete.setOnClickListener(v -> {
                // Show confirmation dialog before deletion
                new AlertDialog.Builder(context)
                        .setTitle("Delete Market Entry")
                        .setMessage("Are you sure you want to delete this market entry?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteMarket(currentMarket, actualPosition);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // Add TextWatchers
            TextWatcher marketNameTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Update the currentMarket's name
                    currentMarket.setMarket_name(s.toString());
                    if (onMarketChangeListener != null) {
                        onMarketChangeListener.onMarketChanged();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not used
                }
            };

            TextWatcher marketQuantityTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = s.toString();
                    if (input.isEmpty()) {
                        currentMarket.setMarket_quantity(0);
                        if (onMarketChangeListener != null) {
                            onMarketChangeListener.onMarketChanged();
                        }
                        marketHolder.etQuantity.setError(null);
                        return;
                    }
                    try {
                        int quantity = Integer.parseInt(input);
                        if (quantity > itemStorage) {
                            marketHolder.etQuantity.setError("Cannot exceed storage (" + itemStorage + ")");
                            // Optionally, reset to itemStorage
                            marketHolder.etQuantity.setText(String.valueOf(itemStorage));
                            currentMarket.setMarket_quantity(itemStorage);
                            Toast.makeText(context, "Cannot Exceed Storage!", Toast.LENGTH_SHORT).show();
                        } else {
                            marketHolder.etQuantity.setError(null);
                            currentMarket.setMarket_quantity(quantity);
                            if (onMarketChangeListener != null) {
                                onMarketChangeListener.onMarketChanged();
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Invalid input, set error
                        marketHolder.etQuantity.setError("Invalid quantity");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not used
                }
            };

            // Attach TextWatchers and store them as tags to remove later
            marketHolder.etMarket.addTextChangedListener(marketNameTextWatcher);
            marketHolder.etMarket.setTag(marketNameTextWatcher);

            marketHolder.etQuantity.addTextChangedListener(marketQuantityTextWatcher);
            marketHolder.etQuantity.setTag(marketQuantityTextWatcher);
        }
    }

    private void deleteMarket(TableMarkets market, int position) {
        executor.execute(() -> {
            // Access the Room database
            DaoMarkets daoMarkets = AppClient.getInstance(context).getAppDatabase().daoMarkets();

            // Delete the market entry from the database
            daoMarkets.deleteMarket(market);

            // Update the adapter's data and notify the change on the main thread
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    markets.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Market entry deleted", Toast.LENGTH_SHORT).show();
                    if (onMarketChangeListener != null) {
                        onMarketChangeListener.onMarketChanged();
                    }
                });
            }
        });
    }

    private void showAddMarketDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Market Entry");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_market, null);
        builder.setView(dialogView);

        EditText etMarketName = dialogView.findViewById(R.id.etMarketName);
        EditText etMarketQuantity = dialogView.findViewById(R.id.etMarketQuantity);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Initialize the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Handle Cancel Button Click
        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        // Handle Submit Button Click
        btnSubmit.setOnClickListener(v -> {
            String marketName = etMarketName.getText().toString().trim();
            String marketQuantityStr = etMarketQuantity.getText().toString().trim();

            // Input Validation
            if (marketName.isEmpty() || marketQuantityStr.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int marketQuantity;
            try {
                marketQuantity = Integer.parseInt(marketQuantityStr);
                if (marketQuantity < 0) {
                    throw new NumberFormatException("Negative quantity");
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (marketQuantity > itemStorage) {
                Toast.makeText(context, "Quantity cannot exceed storage (" + itemStorage + ")", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new TableMarkets instance
            TableMarkets newMarket = new TableMarkets(marketQuantity, marketName, itemId);

            // Insert into database
            executor.execute(() -> {
                DaoMarkets daoMarkets = AppClient.getInstance(context).getAppDatabase().daoMarkets();
                long newId = daoMarkets.insertMarket(newMarket); // Ensure insertMarket returns long

                // Set the generated market_id
                newMarket.setMarket_id((int) newId); // Assuming market_id is int

                // Update the list and notify the adapter on the main thread
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        markets.add(newMarket);
                        notifyItemInserted(markets.size());
                        Toast.makeText(context, "Market entry added", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        if (onMarketChangeListener != null) {
                            onMarketChangeListener.onMarketChanged();
                        }
                    });
                }
            });
        });

        // Show the dialog
        alertDialog.show();
    }

    // Method to update the markets list
    public void setMarkets(List<TableMarkets> markets) {
        this.markets = markets;
        notifyDataSetChanged();
    }

    // Method to remove a market from the list
    public void removeMarket(int position) {
        if (markets != null && position >= 0 && position < markets.size()) {
            markets.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Getter for markets
    public List<TableMarkets> getMarkets() {
        return markets;
    }

    // ViewHolder for regular market items
    public static class MarketViewHolder extends RecyclerView.ViewHolder {

        EditText etMarket;
        EditText etQuantity;
        ImageButton ibDelete;

        public MarketViewHolder(@NonNull View itemView) {
            super(itemView);
            etMarket = itemView.findViewById(R.id.etMarket);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            ibDelete = itemView.findViewById(R.id.ibDelete);
        }
    }

    // ViewHolder for the add button
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        ImageButton ibAdd;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            ibAdd = itemView.findViewById(R.id.ibAdd);
        }
    }
}
