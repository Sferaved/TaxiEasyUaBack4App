package com.taxieasyua.back4app.ui.card;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.res.TypedArrayUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ui.card.unlink.UnlinkApi;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;
import com.taxieasyua.back4app.ui.home.MyBottomSheetMessageFragment;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomCardAdapter extends ArrayAdapter<Map<String, String>> {
    private ArrayList<Map<String, String>> cardMaps;

    private String baseUrl = "https://m.easy-order-taxi.site";
    public CustomCardAdapter(Context context, ArrayList<Map<String, String>> cardMaps) {
        super(context, R.layout.cards_adapter_layout, cardMaps);
        this.cardMaps = cardMaps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.cards_adapter_layout, parent, false);
            holder = new ViewHolder();

            holder.cardImage = view.findViewById(R.id.cardImage); // Правильное определение cardImage
            holder.cardText = view.findViewById(R.id.cardText);
            holder.bankText = view.findViewById(R.id.bankText);
            holder.deleteButton = view.findViewById(R.id.deleteButton);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Map<String, String> cardMap = getItem(position);

        if (cardMap != null) {
            String cardType = cardMap.get("card_type");
            if ("VISA".equals(cardType)) {
                holder.cardImage.setImageResource(R.drawable.visa); // Имя изображения для VISA
            } else if ("MasterCard".equals(cardType)) {
                holder.cardImage.setImageResource(R.drawable.mastercard); // Имя изображения для MasterCard
            } else {
                holder.cardImage.setImageResource(R.drawable.default_card); // Имя изображения по умолчанию
            }
            String masked_card = cardMap.get("masked_card");
            holder.cardText.setText(masked_card);

            String bank_name = cardMap.get("bank_name");
            holder.bankText.setText(bank_name);

            // Обработчик для кнопки удаления
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Получите позицию, которую нужно удалить
                    int position = cardMaps.indexOf(cardMap);

                    // Удалите элемент из базы данных

                        String rectoken = cardMap.get("rectoken");
                        SQLiteDatabase database = getContext().openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
                        database.delete(MainActivity.TABLE_FONDY_CARDS, "rectoken = ?", new String[]{rectoken});
                        database.close();
                        deleteCardTokenFondy(rectoken);


                    // Удалите элемент из cardMaps
                    if (position >= 0) {
                        cardMaps.remove(position);
                        notifyDataSetChanged(); // Обновите адаптер после удаления
                    }
                }
            });
        }

        return view;
    }

    public void deleteCardTokenFondy(String rectoken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UnlinkApi apiService = retrofit.create(UnlinkApi.class);
        Call<Void> call = apiService.deleteCardTokenFondy(rectoken);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    // Обработка успешного ответа
                    Toast.makeText(getContext(), getContext().getString(R.string.un_link_token), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getContext().getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
            }
        });
    }
    static class ViewHolder {
        ImageView cardImage;
        TextView cardText, bankText;
        Button deleteButton;
    }

}
