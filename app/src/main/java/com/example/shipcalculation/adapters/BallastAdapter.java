package com.example.shipcalculation.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipcalculation.R;
import com.example.shipcalculation.models.BallastRecord;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
public class BallastAdapter extends RecyclerView.Adapter<BallastAdapter.BallastViewHolder> {

    // Listener arayüzleri
    public interface OnItemClickListener {
        void onEditClick(BallastRecord ballastRecord);
        void onDeleteClick(BallastRecord ballastRecord);
    }

    private OnItemClickListener listener;
    private final AsyncListDiffer<BallastRecord> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);
    private Context context; // Context'e ihtiyaç duyabiliriz (örn: Toast, Intent)

    public BallastAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // DiffUtil.ItemCallback for efficient list updates
    private static final DiffUtil.ItemCallback<BallastRecord> DIFF_CALLBACK = new DiffUtil.ItemCallback<BallastRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull BallastRecord oldItem, @NonNull BallastRecord newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals") // Bu ek açıklamayı dikkatli kullanın, eğer kendi equals'ınız yoksa ve tüm alanları karşılaştırıyorsanız gerekebilir.
        @Override
        public boolean areContentsTheSame(@NonNull BallastRecord oldItem, @NonNull BallastRecord newItem) {
            // Tüm ilgili alanları karşılaştırın
            if (oldItem.getId() != newItem.getId()) { // ID'ler farklıysa zaten farklı item'lar
                return false;
            }
            if (!oldItem.getBallastTankNo().equals(newItem.getBallastTankNo())) {
                return false;
            }
            if (oldItem.getBallastTankType() != null ? !oldItem.getBallastTankType().equals(newItem.getBallastTankType()) : newItem.getBallastTankType() != null) {
                return false;
            }
            if (Double.compare(oldItem.getBallastTankCapacity(), newItem.getBallastTankCapacity()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getBallastTankSounding(), newItem.getBallastTankSounding()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getDensity(), newItem.getDensity()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getBallastTankVolume(), newItem.getBallastTankVolume()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getBallastTankWeight(), newItem.getBallastTankWeight()) != 0) {
                return false;
            }
            // Date karşılaştırması (null kontrolleri önemli)
            if (oldItem.getLastUpdated() == null) {
                if (newItem.getLastUpdated() != null) {
                    return false; // biri null diğeri değil
                }
                // ikisi de null ise bu alan için eşittirler, diğerlerine bakmaya devam et
            } else if (!oldItem.getLastUpdated().equals(newItem.getLastUpdated())) {
                return false; // biri null değil ve eşit değiller
            }

            return true; // Tüm kontrol edilen alanlar aynıysa true dön
        }

    };

    public void submitList(List<BallastRecord> ballastRecords) {
        differ.submitList(ballastRecords);
    }

    // YENİ METOT: Güncel listeyi almak için
    public List<BallastRecord> getCurrentList() {
        return differ.getCurrentList();
    }

    @NonNull
    @Override
    public BallastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ballast_tank, parent, false);
        return new BallastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BallastViewHolder holder, int position) {
        BallastRecord currentRecord = differ.getCurrentList().get(position);
        holder.bind(currentRecord, listener);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    class BallastViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTankNo;
        //private TextView textViewTankType;
        private TextView textViewTankCapacity;
        private TextView textViewTankSounding;
        private TextView textViewDensity;
        private TextView textViewTankVolume;
        private TextView textViewTankWeight;
        private ImageButton buttonEditTank;
        private ImageButton buttonDeleteTank;
        private TextView textViewLastUpdated;

        public BallastViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTankNo = itemView.findViewById(R.id.textViewBallastTankNo);
            //textViewTankType = itemView.findViewById(R.id.textViewBallastTankType);
            textViewTankCapacity = itemView.findViewById(R.id.textViewBallastTankCapacity);
            textViewTankSounding = itemView.findViewById(R.id.textViewBallastTankSounding);
            textViewDensity = itemView.findViewById(R.id.textViewDensity);
            textViewTankVolume = itemView.findViewById(R.id.textViewBallastTankVolume);
            textViewTankWeight = itemView.findViewById(R.id.textViewBallastTankWeight);
            buttonEditTank = itemView.findViewById(R.id.buttonEditTank);
            buttonDeleteTank = itemView.findViewById(R.id.buttonDeleteTank);
            textViewLastUpdated = itemView.findViewById(R.id.textViewLastUpdated);
        }

        @SuppressLint("SetTextI18n")
        public void bind(final BallastRecord ballastRecord, final OnItemClickListener listener) {
            String tankType = ballastRecord.getBallastTankType();
            String tankNoText;

            // Tank tipini kontrol et
            if (tankType != null && !tankType.isEmpty() && !tankType.equalsIgnoreCase("select tank type")) {
                // Tank tipi geçerli ve "select tank type" değilse, hem numarayı hem de tipi göster
                tankNoText = "No: " + ballastRecord.getBallastTankNo() + " " + tankType;
            } else {
                // Tank tipi boş, null veya "select tank type" ise, sadece numarayı göster
                tankNoText = "No: " + ballastRecord.getBallastTankNo();
            }
            textViewTankNo.setText(tankNoText);
//textViewTankType.setText("Type: " + ballastRecord.getBallastTankType()); // Eğer String ise
            textViewTankCapacity.setText(String.format(Locale.getDefault(), "Capacity: %.3f m³", ballastRecord.getBallastTankCapacity()));
            textViewTankSounding.setText(String.format(Locale.getDefault(), "Sounding: %.2f m", ballastRecord.getBallastTankSounding()));
            textViewDensity.setText(String.format(Locale.getDefault(), "Density: %.3f kg/L", ballastRecord.getDensity()));
            textViewTankVolume.setText(String.format(Locale.getDefault(), "Volume: %.3f m³", ballastRecord.getBallastTankVolume()));
            textViewTankWeight.setText(String.format(Locale.getDefault(), "Weight: %.3f MT", ballastRecord.getBallastTankWeight()));
            textViewLastUpdated.setText("Last Updated: " + ballastRecord.getLastUpdated());
            // Tarih/saat bilgisini formatla ve göster
            if (ballastRecord.getLastUpdated() != null) {
                // İstediğiniz format: "dd.MM.yyyy HH:mm"
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                String formattedDate = sdf.format(ballastRecord.getLastUpdated());
                textViewLastUpdated.setText("Last Updated: " + formattedDate);
                textViewLastUpdated.setVisibility(View.VISIBLE);
            } else {
                // Eğer tarih bilgisi yoksa (olmamalı ama önlem amaçlı)
                textViewLastUpdated.setText("Last Updated: N/A"); // veya View.GONE yapabilirsiniz
                textViewLastUpdated.setVisibility(View.GONE); // Tarih yoksa gizle
            }

            buttonEditTank.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(ballastRecord);
                }
            });

            buttonDeleteTank.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(ballastRecord);
                }
            });

            // İsteğe bağlı: Tüm öğeye tıklama (örneğin detay göstermek için)
            // itemView.setOnClickListener(v -> {
            //     // Handle item click
            // });
        }
    }
}
