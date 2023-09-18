package com.taxieasyua.back4app.ui.uid;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.taxieasyua.back4app.R.string.verify_internet;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NetworkChangeReceiver;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentUidBinding;
import com.taxieasyua.back4app.ui.finish.ApiClient;
import com.taxieasyua.back4app.ui.finish.BonusResponse;
import com.taxieasyua.back4app.ui.finish.RouteResponse;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UIDFragment extends Fragment {

    private @NonNull FragmentUidBinding binding;
    private ListView listView;
    private String[] array;
    private static TextView textView;
    private NetworkChangeReceiver networkChangeReceiver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUidBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        listView = binding.listView;

        networkChangeReceiver = new NetworkChangeReceiver();

        if(connected()) {
            @SuppressLint("UseRequireInsteadOfGet") String email = logCursor(MainActivity.TABLE_USER_INFO, Objects.requireNonNull(getActivity())).get(3);
            fetchRoutes(email);
        }
        return root;
    }
    private boolean connected() {

        Boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
                CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            hasConnect = true;
        }
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            hasConnect = true;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            hasConnect = true;
        }

        if (!hasConnect) {
            Toast.makeText(getActivity(), verify_internet, Toast.LENGTH_LONG).show();
        }
        Log.d("TAG", "connected: " + hasConnect);
        return hasConnect;
    }

    String baseUrl = "https://m.easy-order-taxi.site";
    private List<RouteResponse> routeList = new ArrayList<>();

    private void fetchRoutes(String value) {
        String url = baseUrl + "/android/UIDStatusShowEmail/" + value;
        Call<List<RouteResponse>> call = ApiClient.getApiService().getRoutes(url);
        Log.d("TAG", "fetchRoutes: " + url);
        call.enqueue(new Callback<List<RouteResponse>>() {
            @Override
            public void onResponse(Call<List<RouteResponse>> call, Response<List<RouteResponse>> response) {
                if (response.isSuccessful()) {
                    List<RouteResponse> routes = response.body();

                    if (routes != null && !routes.isEmpty()) {
                        routeList.addAll(routes);
                        processRouteList();
                    } else {
                        binding.textUid.setVisibility(View.VISIBLE);
                        binding.textUid.setText(R.string.no_routs);
                    }
                } else {
                    MyBottomSheetErrorFragment bottomSheetDialogFragment = new MyBottomSheetErrorFragment(getString(R.string.verify_internet));
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            }

            @Override
            public void onFailure(Call<List<RouteResponse>> call, Throwable t) {
                // Обработка ошибок сети или других ошибок
                String errorMessage = t.getMessage();
                t.printStackTrace();
                // Дополнительная обработка ошибки
            }
        });
    }

    private void processRouteList() {
        // В этом методе вы можете использовать routeList для выполнения дополнительных действий с данными.

        // Создайте массив строк
        array = new String[routeList.size()];

        String closeReasonText = getString(R.string.close_resone_def);

        for (int i = 0; i < routeList.size(); i++) {
            RouteResponse route = routeList.get(i);
            String routeFrom = route.getRouteFrom();
            String routefromnumber = route.getRouteFromNumber();
            String routeTo = route.getRouteTo();
            String routeTonumber = route.getRouteToNumber();
            String webCost = route.getWebCost();
            String createdAt = route.getCreatedAt();
            String closeReason = route.getCloseReason();

            switch (closeReason){
                case "-1":
                    closeReasonText = getString(R.string.close_resone_in_work);
                    break;
                case "0":
                    closeReasonText = getString(R.string.close_resone_0);
                    break;
                case "1":
                    closeReasonText = getString(R.string.close_resone_1);
                    break;
                case "2":
                    closeReasonText = getString(R.string.close_resone_2);
                    break;
                case "3":
                    closeReasonText = getString(R.string.close_resone_3);
                    break;
                case "4":
                    closeReasonText = getString(R.string.close_resone_4);
                    break;
                case "5":
                    closeReasonText = getString(R.string.close_resone_5);
                    break;
                case "6":
                    closeReasonText = getString(R.string.close_resone_6);
                    break;
                case "7":
                    closeReasonText = getString(R.string.close_resone_7);
                    break;
                case "8":
                    closeReasonText = getString(R.string.close_resone_8);
                    break;
                case "9":
                    closeReasonText = getString(R.string.close_resone_9);
                    break;

            }


//            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
//            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", new Locale("uk", "UA"));
////            TimeZone timeZoneKyiv = TimeZone.getTimeZone("Europe/Kiev");
//            outputFormat.setTimeZone(timeZoneKyiv);

//            try {
//                Date date = inputFormat.parse(createdAt); // Преобразование строки в объект Date
//                assert date != null;
//                String formattedDateTime = outputFormat.format(date); // Форматирование даты в нужный вид

                String routeInfo = getString(R.string.close_resone_from) + routeFrom + " " + routefromnumber
                        + getString(R.string.close_resone_to) + routeTo + " " + routeTonumber
                        + getString(R.string.close_resone_cost) + webCost + " " + getString(R.string.UAH)
                        + getString(R.string.close_resone_time)
                        + createdAt + getString(R.string.close_resone_text) + closeReasonText;
                array[i] = routeInfo;

                // Теперь formattedDateTime содержит отформатированную дату и время
//            } catch (ParseException e) {
//                e.printStackTrace(); // Обработка возможных ошибок парсинга
//            }
        }
        Log.d("TAG", "processRouteList: array " + Arrays.toString(array));
        if(array != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.drop_down_layout, array);
            listView.setAdapter(adapter);

        }
    }

     @SuppressLint("Range")
    public List<String> logCursor(String table, Context context) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(table, null, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                        list.add(c.getString(c.getColumnIndex(cn)));

                    }

                } while (c.moveToNext());
            }
        }
        database.close();
        return list;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}