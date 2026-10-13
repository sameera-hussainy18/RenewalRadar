package com.example.renewalradar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * One screen for both "Add subscription" and "Edit subscription".
 * If the Intent carries EXTRA_ID we are editing, otherwise we are adding.
 */
public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "subscription_id";

    private static final String[] CATEGORIES = {
            "Streaming", "Music", "Cloud storage", "Software", "Gym & health",
            "Campus bills", "Phone & internet", "Gaming", "News", "Other"
    };

    private TextInputLayout tilName, tilAmount, tilCustomDays;
    private TextInputEditText etName, etAmount, etCustomDays, etDate, etNotes;
    private AutoCompleteTextView actCategory;
    private ChipGroup chipGroupCycle, chipGroupStatus;
    private TextView tvNextDates;

    private Subscription subscription; // null when adding a new one
    private long anchorMillis = RecurrenceEngine.todayMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilName = findViewById(R.id.tilName);
        tilAmount = findViewById(R.id.tilAmount);
        tilCustomDays = findViewById(R.id.tilCustomDays);
        etName = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        etCustomDays = findViewById(R.id.etCustomDays);
        etDate = findViewById(R.id.etDate);
        etNotes = findViewById(R.id.etNotes);
        actCategory = findViewById(R.id.actCategory);
        chipGroupCycle = findViewById(R.id.chipGroupCycle);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        tvNextDates = findViewById(R.id.tvNextDates);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        actCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, CATEGORIES));

        // Show the "every N days" box only when Custom is selected
        chipGroupCycle.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean custom = checkedIds.contains(R.id.chipCustom);
            tilCustomDays.setVisibility(custom ? View.VISIBLE : View.GONE);
        });

        etDate.setOnClickListener(v -> showDatePicker());

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            subscription = AppDatabase.getInstance(this).subscriptionDao().getById(id);
        }

        if (subscription != null) {
            toolbar.setTitle("Edit subscription");
            fillForm(subscription);
            btnDelete.setVisibility(View.VISIBLE);
            showNextDates(subscription);
        } else {
            toolbar.setTitle("Add subscription");
            actCategory.setText(CATEGORIES[0], false);
            btnDelete.setVisibility(View.GONE);
        }
        etDate.setText(RecurrenceEngine.formatDate(anchorMillis));

        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void fillForm(Subscription s) {
        etName.setText(s.name);
        actCategory.setText(s.category, false);
        etAmount.setText(BigDecimal.valueOf(s.amountCents, 2).toPlainString());
        etCustomDays.setText(String.valueOf(s.customDays));
        etNotes.setText(s.notes);
        anchorMillis = s.anchorDate;

        switch (s.cycle) {
            case Subscription.CYCLE_WEEK:
                chipGroupCycle.check(R.id.chipWeekly);
                break;
            case Subscription.CYCLE_YEAR:
                chipGroupCycle.check(R.id.chipYearly);
                break;
            case Subscription.CYCLE_CUSTOM_DAYS:
                chipGroupCycle.check(R.id.chipCustom);
                break;
            default:
                chipGroupCycle.check(R.id.chipMonthly);
                break;
        }

        if (Subscription.STATUS_TRIAL.equals(s.status)) {
            chipGroupStatus.check(R.id.chipTrial);
        } else if (Subscription.STATUS_PAUSED.equals(s.status)) {
            chipGroupStatus.check(R.id.chipPaused);
        } else {
            chipGroupStatus.check(R.id.chipActive);
        }
    }

    /** The "Next 6 charges" box at the bottom of the edit screen. */
    private void showNextDates(Subscription s) {
        List<Long> dates = RecurrenceEngine.nextDueDates(
                s.anchorDate, s.cycle, s.customDays, RecurrenceEngine.todayMillis(), 6);
        StringBuilder sb = new StringBuilder("Next 6 charges:\n");
        for (Long d : dates) {
            sb.append("• ").append(RecurrenceEngine.formatDate(d)).append('\n');
        }
        tvNextDates.setText(sb.toString().trim());
        tvNextDates.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        // MaterialDatePicker works in UTC, so convert our local date to UTC and back
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(anchorMillis);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("First / next charge date")
                .setSelection(utc.getTimeInMillis())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar picked = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            picked.setTimeInMillis(selection);
            Calendar result = Calendar.getInstance();
            result.clear();
            result.set(picked.get(Calendar.YEAR), picked.get(Calendar.MONTH),
                    picked.get(Calendar.DAY_OF_MONTH));
            anchorMillis = result.getTimeInMillis();
            etDate.setText(RecurrenceEngine.formatDate(anchorMillis));
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void save() {
        tilName.setError(null);
        tilAmount.setError(null);
        tilCustomDays.setError(null);

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String amountText = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Please enter a name");
            return;
        }
        long amountCents;
        try {
            amountCents = new BigDecimal(amountText.replace(',', '.'))
                    .movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();
        } catch (NumberFormatException e) {
            tilAmount.setError("Enter an amount like 14.99");
            return;
        }

        String cycle = Subscription.CYCLE_MONTH;
        int checkedCycle = chipGroupCycle.getCheckedChipId();
        if (checkedCycle == R.id.chipWeekly) {
            cycle = Subscription.CYCLE_WEEK;
        } else if (checkedCycle == R.id.chipYearly) {
            cycle = Subscription.CYCLE_YEAR;
        } else if (checkedCycle == R.id.chipCustom) {
            cycle = Subscription.CYCLE_CUSTOM_DAYS;
        }

        int customDays = 30;
        if (Subscription.CYCLE_CUSTOM_DAYS.equals(cycle)) {
            try {
                customDays = Integer.parseInt(etCustomDays.getText().toString().trim());
            } catch (Exception e) {
                customDays = 0;
            }
            if (customDays < 1) {
                tilCustomDays.setError("Enter a number of days (1 or more)");
                return;
            }
        }

        String status = Subscription.STATUS_ACTIVE;
        int checkedStatus = chipGroupStatus.getCheckedChipId();
        if (checkedStatus == R.id.chipTrial) {
            status = Subscription.STATUS_TRIAL;
        } else if (checkedStatus == R.id.chipPaused) {
            status = Subscription.STATUS_PAUSED;
        }

        boolean isNew = subscription == null;
        Subscription s = isNew ? new Subscription() : subscription;
        s.name = name;
        s.category = TextUtils.isEmpty(actCategory.getText()) ? "Other" : actCategory.getText().toString();
        s.amountCents = amountCents;
        s.cycle = cycle;
        s.customDays = customDays;
        s.anchorDate = anchorMillis;
        s.status = status;
        s.notes = etNotes.getText() == null ? "" : etNotes.getText().toString();

        SubscriptionDao dao = AppDatabase.getInstance(this).subscriptionDao();
        if (isNew) {
            s.userId = new SessionManager(this).getUserId();
            dao.insert(s);
        } else {
            dao.update(s);
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete " + subscription.name + "?")
                .setMessage("This removes it from RenewalRadar.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.getInstance(this).subscriptionDao().delete(subscription);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
