package com.example.renewalradar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Home screen: the "This month" total card plus the list of subscriptions,
 * sorted so the one that charges soonest is at the top.
 */
public class MainActivity extends AppCompatActivity {

    private SubscriptionAdapter adapter;
    private TextView tvMonthLabel;
    private TextView tvMonthTotal;
    private TextView tvCount;
    private TextView tvEmpty;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar menu with the "Log out" option
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });

        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        tvMonthLabel = findViewById(R.id.tvMonthLabel);
        tvMonthTotal = findViewById(R.id.tvMonthTotal);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(subscription -> {
            // Tap a card -> open it for editing
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra(AddEditActivity.EXTRA_ID, subscription.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // reload every time we come back from the Add/Edit screen
    }

    private void loadData() {
        SessionManager session = new SessionManager(this);
        List<Subscription> all = AppDatabase.getInstance(this).subscriptionDao()
                .getAllForUser(session.getUserId());

        // Sort: active/trial by next charge date first, paused ones at the bottom
        long today = RecurrenceEngine.todayMillis();
        List<Subscription> sorted = new ArrayList<>(all);
        Collections.sort(sorted, (a, b) -> {
            boolean aPaused = Subscription.STATUS_PAUSED.equals(a.status);
            boolean bPaused = Subscription.STATUS_PAUSED.equals(b.status);
            if (aPaused != bPaused) {
                return aPaused ? 1 : -1;
            }
            return Long.compare(RecurrenceEngine.nextDueDate(a, today),
                    RecurrenceEngine.nextDueDate(b, today));
        });
        adapter.setItems(sorted);

        long totalCents = RecurrenceEngine.totalThisMonthCents(all);
        String money = RecurrenceEngine.formatMoney(totalCents);
        String month = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(System.currentTimeMillis());

        tvMonthLabel.setText("Hi " + session.getUserName() + " · " + month);
        tvMonthTotal.setText(money);
        tvCount.setText(all.size() + " subscriptions tracked");
        // This title is only visible when the toolbar is collapsed,
        // so the monthly total stays on screen while scrolling.
        collapsingToolbar.setTitle("This month: " + money);

        tvEmpty.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    new SessionManager(this).logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }
}
