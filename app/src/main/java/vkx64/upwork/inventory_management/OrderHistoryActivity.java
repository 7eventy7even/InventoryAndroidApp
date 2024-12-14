package vkx64.upwork.inventory_management;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.MyDatabase;
import vkx64.upwork.inventory_management.database.Order;

public class OrderHistoryActivity extends AppCompatActivity {

    RecyclerView rvOrderHistoryList;
    private AppDatabase db;
//    private OrderHistoryAdapter adapter;

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

        db = MyDatabase.getDatabase();
    }

//    private void SetupRecyclerView() {
//        List<Order> itemList = db.orderDao().getAllOrders();
//        adapter = new OrderHistoryAdapter(itemList);
//    }
}