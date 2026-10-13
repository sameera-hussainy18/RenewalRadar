package com.example.renewalradar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows each subscription as a card in the RecyclerView.
 */
public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Subscription subscription);
    }

    private final List<Subscription> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public SubscriptionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Subscription> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription s = items.get(position);

        holder.tvName.setText(s.name);
        holder.tvAmount.setText(RecurrenceEngine.formatMoney(s.amountCents));
        holder.tvCategory.setText(s.category + " · " + RecurrenceEngine.cycleLabel(s));

        if (Subscription.STATUS_PAUSED.equals(s.status)) {
            holder.tvDue.setText("Paused");
        } else {
            long next = RecurrenceEngine.nextDueDate(s, RecurrenceEngine.todayMillis());
            int days = RecurrenceEngine.daysUntil(next);
            String when;
            if (days == 0) {
                when = "Charges today";
            } else if (days == 1) {
                when = "Charges tomorrow";
            } else {
                when = "Charges in " + days + " days";
            }
            holder.tvDue.setText(when + " · " + RecurrenceEngine.formatDate(next));
        }

        holder.tvStatus.setVisibility(
                Subscription.STATUS_TRIAL.equals(s.status) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvAmount;
        final TextView tvCategory;
        final TextView tvDue;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDue = itemView.findViewById(R.id.tvDue);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
