package com.example.shipcalculation.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipcalculation.R;
import com.example.shipcalculation.TankLogReportActivity;
import com.example.shipcalculation.models.FreshWaterRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FreshWaterAdapter extends RecyclerView.Adapter<FreshWaterAdapter.FreshWaterViewHolder> {

    // Listener arayüzleri
    public interface OnItemClickListener {
        void onItemClick(FreshWaterRecord freshWaterRecord);
        void onEditClick(FreshWaterRecord freshWaterRecord);
        void onDeleteClick(FreshWaterRecord freshWaterRecord);
        void onCreateReportClick(FreshWaterRecord freshWaterRecord); // YENİ METOT
}

    private OnItemClickListener listener;
    private final AsyncListDiffer<FreshWaterRecord> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);
    private Context context; // Context'e ihtiyaç duyabiliriz (örn: Toast, Intent)

    public FreshWaterAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // DiffUtil.ItemCallback for efficient list updates
    private static final DiffUtil.ItemCallback<FreshWaterRecord> DIFF_CALLBACK = new DiffUtil.ItemCallback<FreshWaterRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull FreshWaterRecord oldItem, @NonNull FreshWaterRecord newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals") // Bu ek açıklamayı dikkatli kullanın, eğer kendi equals'ınız yoksa ve tüm alanları karşılaştırıyorsanız gerekebilir.
        @Override
        public boolean areContentsTheSame(@NonNull FreshWaterRecord oldItem, @NonNull FreshWaterRecord newItem) {
            // Tüm ilgili alanları karşılaştırın
            if (oldItem.getId() != newItem.getId()) { // ID'ler farklıysa zaten farklı item'lar
                return false;
            }
            if (!oldItem.getFreshWaterTankNo().equals(newItem.getFreshWaterTankNo())) {
                return false;
            }
            if (oldItem.getFreshWaterTankType() != null ? !oldItem.getFreshWaterTankType().equals(newItem.getFreshWaterTankType()) : newItem.getFreshWaterTankType() != null) {
                return false;
            }
            if (Double.compare(oldItem.getFreshWaterTankCapacity(), newItem.getFreshWaterTankCapacity()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getFreshWaterTankSounding(), newItem.getFreshWaterTankSounding()) != 0) {
                return false;
            }
            if (Double.compare(oldItem.getFreshWaterTankVolume(), newItem.getFreshWaterTankVolume()) != 0) {
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

    public void submitList(List<FreshWaterRecord> freshWaterRecords) {
        differ.submitList(freshWaterRecords);
    }

    // YENİ METOT: Güncel listeyi almak için
    public List<FreshWaterRecord> getCurrentList() {
        return differ.getCurrentList();
    }

    @NonNull
    @Override
    public FreshWaterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fresh_water_tank, parent, false);
        return new FreshWaterViewHolder(view);
    }

    @UnstableApi
    @Override
    public void onBindViewHolder(@NonNull FreshWaterViewHolder holder, int position) {
        FreshWaterRecord currentRecord = differ.getCurrentList().get(position);
        holder.bind(currentRecord, listener);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    class FreshWaterViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTankNo;
        private TextView textViewTankType;
        private TextView textViewTankCapacity;
        private TextView textViewTankSounding;


        private TextView textViewTankVolume;

        private ImageButton buttonEditTank;
        public ImageButton buttonCreateReport;
        private ImageButton buttonDeleteTank;
        private TextView textViewLastUpdated;

        public FreshWaterViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTankNo = itemView.findViewById(R.id.textViewFreshWaterTankNo);
            textViewTankType = itemView.findViewById(R.id.textViewTankType);
            textViewTankCapacity = itemView.findViewById(R.id.textViewFreshWaterTankCapacity);
            textViewTankSounding = itemView.findViewById(R.id.textViewFreshWaterTankSounding);
            textViewTankVolume = itemView.findViewById(R.id.textViewFreshWaterTankVolume);
            buttonEditTank = itemView.findViewById(R.id.buttonEditTank);
            buttonDeleteTank = itemView.findViewById(R.id.buttonDeleteTank);
            textViewLastUpdated = itemView.findViewById(R.id.textViewLastUpdated);
            buttonCreateReport = itemView.findViewById(R.id.buttonCreateReport); // YENİ BUTON

        }

        @UnstableApi
        @SuppressLint("SetTextI18n")
        public void bind(final FreshWaterRecord freshWaterRecord, final OnItemClickListener listener) {
            Log.d("FreshWaterAdapter", "bind() called - Tank ID: " + freshWaterRecord.getId() +
                    ", No: " + freshWaterRecord.getFreshWaterTankNo() +
                    ", Type: " + freshWaterRecord.getFreshWaterTankType());
            textViewTankNo.setText("No: " + freshWaterRecord.getFreshWaterTankNo()+" "+ freshWaterRecord.getFreshWaterTankType());

            textViewTankType.setText("Type: " + freshWaterRecord.getFreshWaterTankType());
            textViewTankCapacity.setText(String.format(Locale.getDefault(), "Capacity: %.3f m³", freshWaterRecord.getFreshWaterTankCapacity()));
            textViewTankSounding.setText(String.format(Locale.getDefault(), "Sounding: %.2f m", freshWaterRecord.getFreshWaterTankSounding()));
            textViewTankVolume.setText(String.format(Locale.getDefault(), "Volume: %.3f m³", freshWaterRecord.getFreshWaterTankVolume()));
            textViewLastUpdated.setText("Last Updated: " + freshWaterRecord.getLastUpdated());
            // Tank No ve Tip için null kontrolü ve birleşik gösterim
            String tankType = freshWaterRecord.getFreshWaterTankType();
            String tankNoText;

            // Tank tipini kontrol et
            if (tankType != null && !tankType.isEmpty() && !tankType.equalsIgnoreCase("select tank type")) {
                // Tank tipi geçerli ve "select tank type" değilse, hem numarayı hem de tipi göster
                tankNoText =  freshWaterRecord.getFreshWaterTankNo() + " " + tankType;
            } else {
                // Tank tipi boş, null veya "select tank type" ise, sadece numarayı göster
                tankNoText = freshWaterRecord.getFreshWaterTankNo();
            }
            textViewTankNo.setText(tankNoText);

            textViewTankCapacity.setText(String.format(Locale.getDefault(), "Capacity: %.3f m³", freshWaterRecord.getFreshWaterTankCapacity()));
            textViewTankSounding.setText(String.format(Locale.getDefault(), "Sounding: %.2f m", freshWaterRecord.getFreshWaterTankSounding()));
            textViewTankVolume.setText(String.format(Locale.getDefault(), "Volume: %.3f m³", freshWaterRecord.getFreshWaterTankVolume()));
            // textViewLastUpdated.setText("Last Updated: " + freshWaterRecord.getLastUpdated()); // Bu satır aşağıdaki formatlama ile zaten ele alınıyor

            // Tarih/saat bilgisini formatla ve göster
            if (freshWaterRecord.getLastUpdated() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                String formattedDate = sdf.format(freshWaterRecord.getLastUpdated());
                textViewLastUpdated.setText("Last Updated: " + formattedDate);
                textViewLastUpdated.setVisibility(View.VISIBLE);
            } else {
                textViewLastUpdated.setText("Last Updated: N/A");
                textViewLastUpdated.setVisibility(View.GONE);
            }

            buttonEditTank.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(freshWaterRecord);
                }
            });

            buttonDeleteTank.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(freshWaterRecord);
                }
            });

            buttonCreateReport.setOnClickListener(v -> {
                // BUTONA TIKLANDIĞI ANDAKİ DEĞERLERİ LOGLA
                Log.d("FreshWaterAdapter", "buttonCreateReport CLICKED - Tank ID: " + freshWaterRecord.getId() +
                        ", No from record: " + freshWaterRecord.getFreshWaterTankNo() +
                        ", Type from record: " + freshWaterRecord.getFreshWaterTankType() +
                        " (Intent will use these values)");

                if (listener != null) {
                    Intent intent = new Intent(context, TankLogReportActivity.class);
                    intent.putExtra(TankLogReportActivity.EXTRA_TANK_ID, freshWaterRecord.getId());
                    intent.putExtra(TankLogReportActivity.EXTRA_TANK_NUMBER_OR_NAME, freshWaterRecord.getFreshWaterTankNo());
                    intent.putExtra(TankLogReportActivity.EXTRA_TANK_TYPE, freshWaterRecord.getFreshWaterTankType());
                    context.startActivity(intent);
                    listener.onCreateReportClick(freshWaterRecord);
                }
            });
        }
            // İsteğe bağlı: Tüm öğeye tıklama (örneğin detay göstermek için)
            // itemView.setOnClickListener(v -> {
            //     // Handle item click
            // });
        }
    }

