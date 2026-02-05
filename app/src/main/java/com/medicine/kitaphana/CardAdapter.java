package com.medicine.kitaphana;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(String topicKey);
    }

    private final List<CardItem> items;
    private final OnCardClickListener listener;
    private Typeface font;

    public CardAdapter(List<CardItem> items, OnCardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Apply custom font
    public void setFont(Typeface font) {
        this.font = font;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardItem item = items.get(position);

        holder.header.setText(item.getHeader());
        holder.topic.setText(item.getTopic());

        if (font != null) {
            holder.header.setTypeface(font);
            holder.topic.setTypeface(font);
        }

        // --- SAVE STATE ---
        boolean isSaved = SaveManager.isSaved(holder.itemView.getContext(), item.getKey());
        holder.btnSave.setImageResource(
                isSaved ? R.drawable.save_doly : R.drawable.save_bos
        );

        // --- SAVE TOGGLE ---
        holder.btnSave.setOnClickListener(v -> {
            SaveManager.toggle(v.getContext(), item.getKey());

            boolean nowSaved = SaveManager.isSaved(v.getContext(), item.getKey());
            holder.btnSave.setImageResource(
                    nowSaved ? R.drawable.save_doly : R.drawable.save_bos
            );
        });

        // --- OPEN TOPIC ---
        holder.cardView.setOnClickListener(v ->
                listener.onCardClick(item.getKey())
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ---------------- VIEW HOLDER ----------------

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView header, topic;
        CardView cardView;
        ImageView btnSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            topic = itemView.findViewById(R.id.topic);
            cardView = itemView.findViewById(R.id.cardView);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
