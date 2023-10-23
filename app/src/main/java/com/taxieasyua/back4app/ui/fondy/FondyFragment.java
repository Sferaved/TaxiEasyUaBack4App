package com.taxieasyua.back4app.ui.fondy;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.taxieasyua.back4app.R.string.verify_internet;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.NetworkChangeReceiver;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentBonusBinding;
import com.taxieasyua.back4app.databinding.FragmentFondyBinding;
import com.taxieasyua.back4app.ui.finish.ApiClient;
import com.taxieasyua.back4app.ui.finish.BonusResponse;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.finish.RouteResponse;
import com.taxieasyua.back4app.ui.home.MyBottomSheetErrorFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FondyFragment extends Fragment {

    private @NonNull
    FragmentFondyBinding binding;
    private AppCompatButton btnSum, btnPay, btnOrder;
    private TextView textView;
    private NetworkChangeReceiver networkChangeReceiver;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFondyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        networkChangeReceiver = new NetworkChangeReceiver();
        progressBar = binding.progressBar;
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
//        textView = binding.textBonus;
//        String bonus = logCursor(MainActivity.TABLE_USER_INFO, requireActivity()).get(5);
//        if(bonus == null) {
//            bonus = getString(R.string.upd_bonus_info);
//        } else {
//            textView.setText(getString(R.string.my_bonus) + bonus);
//        }
        btnSum  = binding.btnSum;
        btnSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected()) {
                    @SuppressLint("UseRequireInsteadOfGet") String email = logCursor(MainActivity.TABLE_USER_INFO, Objects.requireNonNull(requireActivity())).get(3);
                    progressBar.setVisibility(View.VISIBLE);

                }
            }
        });

        btnPay  = binding.btnPay;
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(requireActivity(), SimpleExampleActivity.class);
                intent.putExtra("cost", "160");
                startActivity(intent);
            }
        });

        // Ваш текущий фрагмент или активность
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

// Переход к фрагменту HomeFragment

        btnOrder = binding.btnOrder;
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.nav_home);
            }
        });



    }

    private boolean connected() {

        Boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(
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
            Toast.makeText(requireActivity(), verify_internet, Toast.LENGTH_LONG).show();
        }
        Log.d("TAG", "connected: " + hasConnect);
        return hasConnect;
    }

    String baseUrl = "https://m.easy-order-taxi.site";
    private List<RouteResponse> routeList = new ArrayList<>();




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