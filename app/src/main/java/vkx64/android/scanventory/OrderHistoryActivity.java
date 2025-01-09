package vkx64.android.scanventory;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.adapter.HistoryAdapter;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.DaoOrders;
import vkx64.android.scanventory.database.TableOrders;

public class OrderHistoryActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvOrderHistory;
    private HistoryAdapter adapter;
    private ImageButton ibLeftButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        loadOrderHistory();
    }

    private void initializeViews() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(List.of());
        rvOrderHistory.setAdapter(adapter);

        adapter.setOnItemClickListener(order -> {
            Toast.makeText(this, "Click on order ID#" + order.getOrder_id(), Toast.LENGTH_SHORT).show();
        });

        ibLeftButton = findViewById(R.id.ibLeftButton);
        ibLeftButton.setOnClickListener(v -> finish());
    }

    private void loadOrderHistory() {
        executor.execute(() -> {
            DaoOrders dao = AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoOrders();

            List<TableOrders> orderHistoryList = dao.getAllOrderHistory();

            runOnUiThread(() -> {
                if (orderHistoryList.isEmpty()) {
                    Toast.makeText(this, "No order history available.", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.updateData(orderHistoryList); // Update adapter with fetched data
                }
            });
        });
    }

    // Shutdown the executor when the activity is destroyed to avoid leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}