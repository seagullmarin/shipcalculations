package com.example.shipcalculation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipcalculation.R;
import com.example.shipcalculation.models.FreshWaterSoundingLog;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SoundingLogAdapter extends ListAdapter<FreshWaterSoundingLog, SoundingLogAdapter.SoundingLogViewHolder> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    // Uzun tıklama için arayüz
    public interface OnItemLongClickListener {
        void onItemLongClick(FreshWaterSoundingLog log);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public SoundingLogAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<FreshWaterSoundingLog> DIFF_CALLBACK = new DiffUtil.ItemCallback<FreshWaterSoundingLog>() {
        @Override
        public boolean areItemsTheSame(@NonNull FreshWaterSoundingLog oldItem, @NonNull FreshWaterSoundingLog newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FreshWaterSoundingLog oldItem, @NonNull FreshWaterSoundingLog newItem) {
            return oldItem.getTankId() == newItem.getTankId() &&
                    oldItem.getMeasurementTime().equals(newItem.getMeasurementTime()) &&
                    oldItem.getSounding() == newItem.getSounding() &&
                    oldItem.getCalculatedVolume() == newItem.getCalculatedVolume();
        }
    };

    @NonNull
    @Override
    public SoundingLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sounding_log, parent, false);
        return new SoundingLogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundingLogViewHolder holder, int position) {
        FreshWaterSoundingLog currentLog = getItem(position);
        holder.bind(currentLog);
    }

    class SoundingLogViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLogDate, textViewLogSounding, textViewLogVolume;

        public SoundingLogViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLogDate = itemView.findViewById(R.id.textViewLogDate);
            textViewLogSounding = itemView.findViewById(R.id.textViewLogSounding);
            textViewLogVolume = itemView.findViewById(R.id.textViewLogVolume);

            // Uzun tıklama olayı
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(FreshWaterSoundingLog log) {
            textViewLogDate.setText(dateFormat.format(log.getMeasurementTime()));
            textViewLogSounding.setText(String.format(Locale.getDefault(), "%.2f m", log.getSounding()));
            textViewLogVolume.setText(String.format(Locale.getDefault(), "%.2f m³", log.getCalculatedVolume()));
        }
    }
}
