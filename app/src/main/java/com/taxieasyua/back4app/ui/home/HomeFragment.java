package com.taxieasyua.back4app.ui.home;


import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Color.RED;
import static com.taxieasyua.back4app.R.string.address_error_message;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.ServerConnection;
import com.taxieasyua.back4app.cities.Cherkasy.Cherkasy;
import com.taxieasyua.back4app.cities.Dnipro.Dnipro;
import com.taxieasyua.back4app.cities.Kyiv.KyivCity;
import com.taxieasyua.back4app.cities.Odessa.Odessa;
import com.taxieasyua.back4app.cities.Odessa.OdessaTest;
import com.taxieasyua.back4app.cities.Zaporizhzhia.Zaporizhzhia;
import com.taxieasyua.back4app.databinding.FragmentHomeBinding;
import com.taxieasyua.back4app.ui.finish.FinishActivity;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.start.ResultSONParser;
import com.taxieasyua.back4app.ui.start.StartActivity;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String from, to;
    EditText from_number, to_number;
    String messageResult;
    private Spinner listView;
    Button button;
    private String[] array;
    public  String api;
    public  String[] arrayStreet;
    static FloatingActionButton fab_call;
    private final String TAG = "TAG";
    private static final int CM_DELETE_ID = 1;
    String from_street_rout, to_street_rout;
    private int selectedPosition = -1;
    Button mapbut, gpsbut, buttonAddServices;
    AppCompatButton btncost;
    String FromAddressString, ToAddressString;
    Integer selectedItem;
    private long firstCost;
    public long addCost, cost;

    public static String[] arrayServiceCode() {
        return new String[]{
                "BAGGAGE",
                "ANIMAL",
                "CONDIT",
                "MEET",
                "COURIER",
                "CHECK_OUT",
                "BABY_SEAT",
                "DRIVER",
                "NO_SMOKE",
                "ENGLISH",
                "CABLE",
                "FUEL",
                "WIRES",
                "SMOKE",
        };
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        List<String> stringList = logCursor(StartActivity.CITY_INFO, getActivity());
        switch (stringList.get(1)){
            case "Dnipropetrovsk Oblast":
                arrayStreet = Dnipro.arrayStreet();
                api = StartActivity.apiDnipro;
                break;
            case "Zaporizhzhia":
                arrayStreet = Zaporizhzhia.arrayStreet();
                api = StartActivity.apiZaporizhzhia;
                break;
            case "Cherkasy Oblast":
                arrayStreet = Cherkasy.arrayStreet();
                api = StartActivity.apiCherkasy;
                break;
            case "Odessa":
                arrayStreet = Odessa.arrayStreet();
                api = StartActivity.apiOdessa;
                break;
            case "OdessaTest":
                arrayStreet = OdessaTest.arrayStreet();
                api = StartActivity.apiTest;
                break;
            default:
                arrayStreet = KyivCity.arrayStreet();
                api = StartActivity.apiKyiv;
                break;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, arrayStreet);

        AutoCompleteTextView textViewFrom =binding.textFrom;
        textViewFrom.setAdapter(adapter);
//        Log.d("TAG", "onCreateView startPoint: " + OpenStreetMapActivity.from_name + OpenStreetMapActivity.from_house);
        if(OpenStreetMapActivity.from_name != null && !OpenStreetMapActivity.from_name.equals("name")) {
            textViewFrom.setText(OpenStreetMapActivity.from_name);
            from = OpenStreetMapActivity.from_name;
        }
        from_number = binding.fromNumber;
        if(hasServer()){
            if((OpenStreetMapActivity.from_house != null) && !OpenStreetMapActivity.from_house.equals("house")) {
                String url = "https://m.easy-order-taxi.site/" + api + "/android/autocompleteSearchComboHid/" + from;

                Map sendUrlMapCost = null;
                try {
                    sendUrlMapCost = ResultSONParser.sendURL(url);
                } catch (MalformedURLException | InterruptedException | JSONException e) {
                    Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                }

                String orderCost = (String) sendUrlMapCost.get("message");
                if (orderCost.equals("200")) {
                    Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                } else if (orderCost.equals("400")) {
                    textViewFrom.setTextColor(RED);
                    Toast.makeText(getActivity(), address_error_message, Toast.LENGTH_SHORT).show();
                } else if (orderCost.equals("1")) {
                    from_number.setVisibility(View.VISIBLE);
                    from_number.setText(OpenStreetMapActivity.from_house);
                    from_number.requestFocus();
                } else if (orderCost.equals("0")) {
                    from_number.setText(" ");
                    from_number.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            Toast.makeText(getActivity(), R.string.server_error_connected, Toast.LENGTH_SHORT).show();
        }
        if(hasServer()){
            textViewFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position; // Обновляем выбранную позицию
                adapter.notifyDataSetChanged(); // Обновляем вид списка

                    if(connected()) {
                        from = String.valueOf(adapter.getItem(position));
                        if (from.indexOf("/") != -1) {
                            from = from.substring(0,  from.indexOf("/"));
                        };

                        String url = "https://m.easy-order-taxi.site/" + api + "/android/autocompleteSearchComboHid/" + from;

                        Map sendUrlMapCost = null;
                        try {
                            sendUrlMapCost = ResultSONParser.sendURL(url);
                        } catch (MalformedURLException | InterruptedException | JSONException e) {
                            Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                        }

                        String orderCost = (String) sendUrlMapCost.get("message");
                        if (orderCost.equals("200")) {
                            Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                        } else if (orderCost.equals("400")) {
                            textViewFrom.setTextColor(RED);
                            Toast.makeText(getActivity(), address_error_message, Toast.LENGTH_SHORT).show();
                        } else if (orderCost.equals("1")) {
                            from_number.setVisibility(View.VISIBLE);
                            from_number.setText(" ");
                            from_number.requestFocus();
                        } else if (orderCost.equals("0")) {
                            from_number.setText(" ");
                            from_number.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                    }

            }
        });
        } else {
            Toast.makeText(getActivity(), R.string.server_error_connected, Toast.LENGTH_SHORT).show();
        }
        AutoCompleteTextView textViewTo =binding.textTo;
        textViewTo.setAdapter(adapter);
        to_number = binding.toNumber;
        if(hasServer()){
            textViewTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                              @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   if (connected()) {
                       to = String.valueOf(adapter.getItem(position));
                       if (to.indexOf("/") != -1) {
                           to = to.substring(0, to.indexOf("/"));
                       }
                       ;
                       String url = "https://m.easy-order-taxi.site/" + api + "/android/autocompleteSearchComboHid/" + to;

                       Map sendUrlMapCost = null;
                       try {
                           sendUrlMapCost = ResultSONParser.sendURL(url);
                       } catch (MalformedURLException | InterruptedException | JSONException e) {
                           Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                       }

                       String orderCost = (String) sendUrlMapCost.get("message");

                       if (orderCost.equals("200")) {
                           Toast.makeText(getActivity(), R.string.error_firebase_start, Toast.LENGTH_SHORT).show();
                       } else if (orderCost.equals("400")) {
                           textViewTo.setTextColor(RED);
                           Toast.makeText(getActivity(), address_error_message, Toast.LENGTH_SHORT).show();
                       } else if (orderCost.equals("1")) {
                           to_number.setVisibility(View.VISIBLE);
                           to_number.setText(" ");
                           to_number.requestFocus();
                       }  else if (orderCost.equals("0")) {
                           to_number.setText(" ");
                           to_number.setVisibility(View.INVISIBLE);
                       }
                   }
                }
             });
        } else {
            Toast.makeText(getActivity(), R.string.server_error_connected, Toast.LENGTH_SHORT).show();
        }

        btncost = binding.btnCost;
        btncost.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(connected()) {
                    if (from == null) {
                        Toast.makeText(getActivity(), getString(R.string.rout_from_message), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (to == null) {
                            to = from;
                            to_number.setText(from_number.getText());
                        }
                        try {
                            String urlCost = getTaxiUrlSearch(from, from_number.getText().toString(), to, to_number.getText().toString(), "costSearch", getActivity());


                            Map sendUrlMapCost = CostJSONParser.sendURL(urlCost);
                            String orderCost = (String) sendUrlMapCost.get("order_cost");

                            String message = (String) sendUrlMapCost.get("message");


                            if (orderCost.equals("0")) {

                                if (to.equals(from)) {
                                    textViewTo.setText("");
                                    to = null;
                                }
                                Toast.makeText(getActivity(), getString(R.string.error_message) + message, Toast.LENGTH_SHORT).show();
                            }
                            if (!orderCost.equals("0")) {
                                if(!verifyOrder(getContext())) {
                                    Toast.makeText(getActivity(), getString(R.string.cost_of_order) + orderCost + getString(R.string.firebase_false_message), Toast.LENGTH_SHORT).show();
                                } else {

                                    MaterialAlertDialogBuilder builderAddCost = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                                    LayoutInflater inflaterCost = getActivity().getLayoutInflater();

                                    View view_cost = inflaterCost.inflate(R.layout.add_cost_layout, null);
                                    builderAddCost.setView(view_cost);

                                    TextView costView = view_cost.findViewById(R.id.cost);

                                    cost = Long.parseLong(orderCost);
                                    long MIN_COST_VALUE = (long) ((long) Double.parseDouble(orderCost) * 0.1);
                                    long MAX_COST_VALUE = Long.parseLong(orderCost) * 3;
                                    firstCost = Long.parseLong(orderCost);

                                    addCost = 0;
                                    Button btn_minus = view_cost.findViewById(R.id.btn_minus);
                                    Button btn_plus = view_cost.findViewById(R.id.btn_plus);

                                    String discountText = logCursor(StartActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                                    long discount =  firstCost  * Integer.parseInt(discountText)/100 - firstCost;
                                    firstCost = firstCost  *  Integer.parseInt(discountText)/100;
                                    addCost = discount;
                                    costView.setText(String.valueOf(firstCost));
                                    btn_minus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            firstCost -= 5;
                                            addCost -= 5;
                                            if (firstCost <= MIN_COST_VALUE) {
                                                firstCost = MIN_COST_VALUE;
                                                addCost = MIN_COST_VALUE - firstCost;
                                            }
                                            costView.setText(String.valueOf(firstCost));

                                        }
                                    });

                                    btn_plus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            firstCost += 5;
                                            addCost += 5;
                                            if (firstCost >= MAX_COST_VALUE) {
                                                firstCost = MAX_COST_VALUE;
                                                addCost = MAX_COST_VALUE - firstCost;
                                            }
                                            costView.setText(String.valueOf(firstCost));
                                        }
                                    });

                                    if (!verifyPhone(getContext())) {
                                        getPhoneNumber();
                                    }
                                    if (!verifyPhone(getContext())) {
                                        MyPhoneDialogFragment bottomSheetDialogFragment = new MyPhoneDialogFragment();
                                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                                    }

                                    builderAddCost
                                            .setPositiveButton(getString(R.string.order), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if(connected()) {
                                                        if (verifyPhone(getContext())) {
                                                            try {
                                                                String urlOrder = getTaxiUrlSearch(from, from_number.getText().toString(), to, to_number.getText().toString(), "orderSearch", getActivity());
                                                                Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);

                                                                String orderWeb = (String) sendUrlMap.get("order_cost");
                                                                if (!orderWeb.equals("0")) {

                                                                    String from_name = (String) sendUrlMap.get("routefrom");
                                                                    String to_name = (String) sendUrlMap.get("routeto");
                                                                    if (from_name.equals(to_name)) {
                                                                        messageResult = getString(R.string.thanks_message) +
                                                                                from_name + " " + from_number.getText() + " " +  getString(R.string.on_city) +
                                                                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);


                                                                    } else {
                                                                        messageResult =  getString(R.string.thanks_message) +
                                                                                from_name + " " + from_number.getText() + " " + getString(R.string.to_message) +
                                                                                to_name + " " + to_number.getText() + "." +
                                                                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
                                                                    }
//                                                                    Toast.makeText(getActivity(), messageResult, Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(getActivity(), FinishActivity.class);
                                                                    intent.putExtra("messageResult_key", messageResult);
                                                                    intent.putExtra("UID_key", String.valueOf(sendUrlMap.get("dispatching_order_uid")));
                                                                    startActivity(intent);
                                                                    if(from_name.equals(to_name)) {
                                                                        if(!sendUrlMap.get("lat").equals("0")) {
                                                                            insertRecordsOrders(
                                                                                    from_name, from_name,
                                                                                    from_number.getText().toString(), from_number.getText().toString(),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                                    getContext()
                                                                            );
                                                                        }
                                                                    } else {

                                                                        if(!sendUrlMap.get("lat").equals("0")) {
                                                                            insertRecordsOrders(
                                                                                    from_name, to_name,
                                                                                    from_number.getText().toString(), to_number.getText().toString(),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                                    (String) sendUrlMap.get("lat"), (String) sendUrlMap.get("lng"), getContext()
                                                                            );
                                                                        }
                                                                    }
                                                                    HomeFragment newFragment = new HomeFragment();
                                                                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                                                    transaction.replace(getId(), newFragment);
                                                                    transaction.addToBackStack(null);
                                                                    transaction.commit();
                                                                } else {
                                                                    String message = (String) sendUrlMap.get("message");
                                                                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme);
                                                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                                                    View view = inflater.inflate(R.layout.free_message_layout, null);
                                                                    TextView alertMessage = view.findViewById(R.id.text_message);
                                                                    alertMessage.setText(message);
                                                                    alertDialogBuilder.setView(view);

                                                                    alertDialogBuilder.setPositiveButton(getString(R.string.help), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                                                    String phone;
                                                                                    List<String> stringList = logCursor(StartActivity.CITY_INFO, getActivity());
                                                                                    switch (stringList.get(1)){
                                                                                        case "Kyiv City":
                                                                                            phone = "tel:0674443804";
                                                                                            break;
                                                                                        case "Dnipropetrovsk Oblast":
                                                                                            phone = "tel:0667257070";
                                                                                            break;
                                                                                        case "Odessa":
                                                                                            phone = "tel:0737257070";
                                                                                            break;
                                                                                        case "Zaporizhzhia":
                                                                                            phone = "tel:0687257070";
                                                                                            break;
                                                                                        case "Cherkasy Oblast":
                                                                                            phone = "tel:0962294243";
                                                                                            break;
                                                                                        default:
                                                                                            phone = "tel:0674443804";
                                                                                            break;
                                                                                    }
                                                                                    intent.setData(Uri.parse(phone));
                                                                                    startActivity(intent);

                                                                                }
                                                                            })
                                                                            .setNegativeButton(getString(R.string.try_again),null)
                                                                            .show();
                                                                }


                                                            } catch (MalformedURLException e) {
                                                                Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(getActivity(), getString(R.string.please_phone_message), Toast.LENGTH_SHORT).show();

                                                        }
                                                    }  else {
                                                        Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                                                    }
                                                }})
                                            .setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

//                                                    HomeFragment newFragment = new HomeFragment();
//                                                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//                                                    transaction.replace(getId(), newFragment);
//                                                    transaction.addToBackStack(null);
//                                                    transaction.commit();
                                                }
                                            })
                                            .show();
                                }
                            }

                        } catch (MalformedURLException e) {
                            Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                }
            }
        });

        gpsbut = binding.gpsbut;
        gpsbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        fab_call = binding.fabCall;
        mapbut = binding.mapbut;
        mapbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception ex) {
                }

                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch(Exception ex) {
                }

                if(!gps_enabled || !network_enabled) {
                    // notify user
                    MaterialAlertDialogBuilder builder =  new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    View view_cost = inflater.inflate(R.layout.message_layout, null);
                    builder.setView(view_cost);
                    TextView message = view_cost.findViewById(R.id.textMessage);
                    message.setText(R.string.gps_info);
                    builder.setPositiveButton(R.string.gps_on, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                            .setNegativeButton(R.string.cancel_button, null)
                            .show();
                }  else  if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);

                } else if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getActivity(), OpenStreetMapActivity.class));
                }
            }
        });

        fab_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String phone;
                List<String> stringList = logCursor(StartActivity.CITY_INFO, getActivity());
                switch (stringList.get(1)){
                    case "Kyiv City":
                        phone = "tel:0674443804";
                        break;
                    case "Dnipropetrovsk Oblast":
                        phone = "tel:0667257070";
                        break;
                    case "Odessa":
                        phone = "tel:0737257070";
                        break;
                    case "Zaporizhzhia":
                        phone = "tel:0687257070";
                        break;
                    case "Cherkasy Oblast":
                        phone = "tel:0962294243";
                        break;
                    default:
                        phone = "tel:0674443804";
                        break;
                }
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });

        buttonAddServices = binding.btnAdd;
        buttonAddServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();
                bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        button = binding.btnRouts;
        listView = binding.list;
        array = arrayToRoutsAdapter ();
        if(array != null)  {
            ArrayAdapter<String> adapterRouts = new ArrayAdapter<>(getActivity(),  R.layout.custom_list_item, array);
            listView.setAdapter(adapterRouts);

            listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Обработка выбора элемента
                    selectedItem = position;
                    // Дополнительный код по обработке выбора элемента
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Код, если ни один элемент не выбран
                }
            });

            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        if(hasServer()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                dialogFromToOneRout(routChoice(selectedItem + 1));
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.server_error_connected, Toast.LENGTH_SHORT).show();
                        }
                    } catch (MalformedURLException | InterruptedException | JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                    }

            }
        });

        return root;
    }

    private boolean verifyOrder(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);

        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(StartActivity.TABLE_USER_INFO, context).get(1).equals("0")) {
                verify = false;
            }
            cursor.close();
        }

        return verify;
    }

    private boolean verifyPhone(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);
        boolean verify = true;
        if (cursor.getCount() == 1) {

            if (logCursor(StartActivity.TABLE_USER_INFO, context).get(2).equals("+380")) {
                verify = false;
            }
            cursor.close();
        }

        return verify;
    }
    public static void updateRecordsUser(String result, Context context) {
        ContentValues cv = new ContentValues();

        cv.put("phone_number", result);

        // обновляем по id
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        int updCount = database.update(StartActivity.TABLE_USER_INFO, cv, "id = ?",
                new String[] { "1" });
        Log.d("TAG", "updated rows count = " + updCount);


    }
    private void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        }
    }
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // Произвольный код для запроса разрешений


// Другой код вашего Fragment или Activity...
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("TAG", "onRequestPermissionsResult requestCode: " + requestCode);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getActivity(), OpenStreetMapActivity.class);
                // Разрешения получены, теперь можно продолжить обновление местоположения
                startActivity(intent);
            } else {
                // Пользователь не предоставил необходимые разрешения, переход на MainActivity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        }
    }


    private Map <String, String> routChoice(int i) {
        Map <String, String> rout = new HashMap<>();
        SQLiteDatabase database = getContext().openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(StartActivity.TABLE_ORDERS_INFO, null, null, null, null, null, null);
        c.move(i);
        rout.put("id", c.getString(c.getColumnIndexOrThrow ("id")));
        rout.put("from_lat", c.getString(c.getColumnIndexOrThrow ("from_lat")));
        rout.put("from_lng", c.getString(c.getColumnIndexOrThrow ("from_lng")));
        rout.put("to_lat", c.getString(c.getColumnIndexOrThrow ("to_lat")));
        rout.put("to_lng", c.getString(c.getColumnIndexOrThrow ("to_lng")));
        rout.put("from_street", c.getString(c.getColumnIndexOrThrow ("from_street")));
        rout.put("from_number", c.getString(c.getColumnIndexOrThrow ("from_number")));
        rout.put("to_street", c.getString(c.getColumnIndexOrThrow ("to_street")));
        rout.put("to_number", c.getString(c.getColumnIndexOrThrow ("to_number")));

        Log.d("TAG", "routMaps: " + rout);
        return rout;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public static ArrayList<Map> routMaps(Context context) {
        Map <String, String> routs;
        ArrayList<Map> routsArr = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor c = database.query(StartActivity.TABLE_ORDERS_INFO, null, null, null, null, null, null);
        int i = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    routs = new HashMap<>();
                    routs.put("id", c.getString(c.getColumnIndexOrThrow ("id")));
                    routs.put("from_street", c.getString(c.getColumnIndexOrThrow ("from_street")));
                    routs.put("from_number", c.getString(c.getColumnIndexOrThrow ("from_number")));
                    routs.put("to_street", c.getString(c.getColumnIndexOrThrow ("to_street")));
                    routs.put("to_number", c.getString(c.getColumnIndexOrThrow ("to_number")));
                    routsArr.add(i++, routs);
                } while (c.moveToNext());
            }
        }
        database.close();
        Log.d("TAG", "routMaps: " + routsArr);
        return routsArr;
    }
    private String[] arrayToRoutsAdapter () {
        ArrayList<Map>  routMaps = routMaps(getContext());
        String[] arrayRouts;
        if(routMaps.size() != 0) {
            arrayRouts = new String[routMaps.size()];
            for (int i = 0; i < routMaps.size(); i++) {
                if(!routMaps.get(i).get("from_street").toString().equals(routMaps.get(i).get("to_street").toString())) {
                   if (!routMaps.get(i).get("from_street").toString().equals(routMaps.get(i).get("from_number").toString())) {
                       arrayRouts[i] = routMaps.get(i).get("from_street").toString() + " " +
                               routMaps.get(i).get("from_number").toString() + " -> " +
                               routMaps.get(i).get("to_street").toString() + " " +
                               routMaps.get(i).get("to_number").toString();
                   } else if(!routMaps.get(i).get("to_street").toString().equals(routMaps.get(i).get("to_number").toString())) {
                       arrayRouts[i] = routMaps.get(i).get("from_street").toString() +
                               OpenStreetMapActivity.tom +
                               routMaps.get(i).get("to_street").toString() + " " +
                               routMaps.get(i).get("to_number").toString();
                   } else {
                       arrayRouts[i] = routMaps.get(i).get("from_street").toString()  +
                               OpenStreetMapActivity.tom +
                               routMaps.get(i).get("to_street").toString();

                   }

                } else {
                    arrayRouts[i] = routMaps.get(i).get("from_street").toString() + " " +
                            routMaps.get(i).get("from_number").toString() + " -> " +
                            getString(R.string.on_city_tv);
                }

            }
        } else {
            arrayRouts = null;
        }
        return arrayRouts;
    }
    public CompletableFuture<Boolean> checkConnectionAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ServerConnection.checkConnection("https://m.easy-order-taxi.site/", new ServerConnection.ConnectionCallback() {
            @Override
            public void onConnectionResult(boolean isConnected) {
                future.complete(isConnected);
            }
        });

        return future;
    }
    private boolean hasServer() {
        CompletableFuture<Boolean> connectionFuture = checkConnectionAsync();
        boolean isConnected = false;
        try {
            isConnected = connectionFuture.get();
        } catch (Exception e) {

        }
        return  isConnected;
    };
    private boolean connected() {

        Boolean hasConnect = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);
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

        return hasConnect;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dialogFromToOneRout(Map <String, String> rout) throws MalformedURLException, InterruptedException, JSONException {
        if(connected()) {
            Log.d("TAG", "dialogFromToOneRout: " + rout.toString());
            Double from_lat =  Double.valueOf(rout.get("from_lat"));
            Double from_lng = Double.valueOf(rout.get("from_lng"));
            Double to_lat = Double.valueOf(rout.get("to_lat"));
            Double to_lng = Double.valueOf(rout.get("to_lng"));
            FromAddressString = rout.get("from_street") + rout.get("from_number") ;
            Log.d("TAG", "dialogFromToOneRout: FromAddressString" + FromAddressString);
            ToAddressString = rout.get("to_street") + rout.get("to_number");
            if(rout.get("from_street").equals(rout.get("to_street"))) {
                ToAddressString =  getString(R.string.on_city_tv);;
            }
            Log.d("TAG", "dialogFromToOneRout: ToAddressString" + ToAddressString);

                String urlCost = getTaxiUrlSearchMarkers(from_lat, from_lng,
                        to_lat, to_lng, "costSearchMarkers", getContext());

                Map<String, String> sendUrlMapCost = ToJSONParser.sendURL(urlCost);

                String message = (String) sendUrlMapCost.get("message");
                String orderCost = (String) sendUrlMapCost.get("order_cost");

                GeoPoint startPoint = new GeoPoint(from_lat, to_lat);

                if (orderCost.equals("0")) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//                    coastOfRoad(startPoint, message);
                }
                if (!orderCost.equals("0")) {

                    MaterialAlertDialogBuilder builderAddCost = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    View view_cost = inflater.inflate(R.layout.add_cost_layout, null);
                    builderAddCost.setView(view_cost);
                    TextView costView = view_cost.findViewById(R.id.cost);

                    cost = Long.parseLong(orderCost);
                    long MIN_COST_VALUE = (long) ((long) Double.parseDouble(orderCost) * 0.1);
                    long MAX_COST_VALUE = Long.parseLong(orderCost) * 3;
                    firstCost = Long.parseLong(orderCost);

                    addCost = 0;
                    Button btn_minus = view_cost.findViewById(R.id.btn_minus);
                    Button btn_plus = view_cost.findViewById(R.id.btn_plus);

                    String discountText = logCursor(StartActivity.TABLE_SETTINGS_INFO, getContext()).get(3);
                    long discount =  firstCost  * Integer.parseInt(discountText)/100 - firstCost;
                    firstCost = firstCost  *  Integer.parseInt(discountText)/100;
                    addCost = discount;
                    costView.setText(String.valueOf(firstCost));
                    btn_minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firstCost -= 5;
                            addCost -= 5;
                            if (firstCost <= MIN_COST_VALUE) {
                                firstCost = MIN_COST_VALUE;
                                addCost = MIN_COST_VALUE - firstCost;
                            }
                            costView.setText(String.valueOf(firstCost));

                        }
                    });

                    btn_plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firstCost += 5;
                            addCost += 5;
                            if (firstCost >= MAX_COST_VALUE) {
                                firstCost = MAX_COST_VALUE;
                                addCost = MAX_COST_VALUE - firstCost;
                            }
                            costView.setText(String.valueOf(firstCost));
                        }
                    });

                    builderAddCost
                            .setPositiveButton(getString(R.string.order), new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(connected()) {
                                        try {
                                            String urlCost = getTaxiUrlSearchMarkers(from_lat, from_lng,
                                                    to_lat, to_lng, "orderSearchMarkers", getContext());

                                            Map<String, String> sendUrlMapCost = ToJSONParser.sendURL(urlCost);

                                            String message = (String) sendUrlMapCost.get("message");
                                            String orderCost = (String) sendUrlMapCost.get("order_cost");

                                            if (orderCost.equals("0")) {
                                                Toast.makeText(getActivity(), OpenStreetMapActivity.em + message, Toast.LENGTH_LONG).show();
                                            }
                                            if (!orderCost.equals("0")) {
                                                String orderWeb = (String) sendUrlMapCost.get("order_cost");
                                                if (!orderWeb.equals("0")) {

                                                    String messageResult = getString(R.string.thanks_message) +
                                                            FromAddressString + getString(R.string.to_message) + ToAddressString +
                                                            getString(R.string.call_of_order) + orderWeb + getString(R.string.UAH);

//                                                    Toast.makeText(getActivity(), messageResult, Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(getActivity(), FinishActivity.class);
                                                    intent.putExtra("messageResult_key", messageResult);
                                                    intent.putExtra("UID_key", Objects.requireNonNull(sendUrlMapCost.get("dispatching_order_uid")));
                                                    startActivity(intent);
                                                } else {
                                                    message = (String) sendUrlMapCost.get("message");

                                                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                                    View view = inflater.inflate(R.layout.free_message_layout, null);
                                                    TextView alertMessage = view.findViewById(R.id.text_message);
                                                    alertMessage.setText(message);
                                                    alertDialogBuilder.setView(view);

                                                    alertDialogBuilder.setPositiveButton(OpenStreetMapActivity.hlp, new DialogInterface.OnClickListener() {
                                                                @SuppressLint("SuspiciousIndentation")
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                                    String phone;
                                                                    List<String> stringList = logCursor(StartActivity.CITY_INFO, getActivity());
                                                                    switch (stringList.get(1)){
                                                                        case "Kyiv City":
                                                                            phone = "tel:0674443804";
                                                                            break;
                                                                        case "Dnipropetrovsk Oblast":
                                                                            phone = "tel:0667257070";
                                                                            break;
                                                                        case "Odessa":
                                                                            phone = "tel:0737257070";
                                                                            break;
                                                                        case "Zaporizhzhia":
                                                                            phone = "tel:0687257070";
                                                                            break;
                                                                        case "Cherkasy Oblast":
                                                                            phone = "tel:0962294243";
                                                                            break;
                                                                        default:
                                                                            phone = "tel:0674443804";
                                                                            break;
                                                                    }
                                                                    intent.setData(Uri.parse(phone));
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .setNegativeButton(OpenStreetMapActivity.tra, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                    getActivity().startActivity(intent);
                                                                }
                                                            })
                                                            .show();
                                                }
                                            }

                                        } catch (MalformedURLException e) {
                                            Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                                    }
                                }})
                            .setNegativeButton(getString(R.string.cancel_button), null)
                            .show();

                }

            } else {
            Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearch(String from, String from_number, String to, String to_number, String urlAPI, Context context) {

            //  Проверка даты и времени

            List<String> stringList = logCursor(StartActivity.TABLE_ADD_SERVICE_INFO, context);
            String time = stringList.get(1);
            String comment = stringList.get(2);
            String date = stringList.get(3);

        Log.d(TAG, "getTaxiUrlSearch: addCost" + addCost);

        // Origin of route
        String str_origin = from + "/" + from_number;

        // Destination of route
        String str_dest = to + "/" + to_number;

        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);

        String tarif =  logCursor(StartActivity.TABLE_SETTINGS_INFO, context).get(2);

        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(StartActivity.TABLE_USER_INFO, context).get(3);
        String displayName = logCursor(StartActivity.TABLE_USER_INFO, context).get(4);

        if(urlAPI.equals("costSearch")) {
            Cursor c = database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);
            if (c.getCount() == 1) {
                phoneNumber = logCursor(StartActivity.TABLE_USER_INFO, context).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/" + displayName + "(" + userEmail + ")";
        }

        if(urlAPI.equals("orderSearch")) {
            phoneNumber = logCursor(StartActivity.TABLE_USER_INFO, context).get(2);

            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName  + "/" + addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(StartActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
// Building the url to the web service
        List<String> services = logCursor(StartActivity.TABLE_SERVICE_INFO, context);
        List<String> servicesChecked = new ArrayList<>();
        String result;
        boolean servicesVer = false;
        for (int i = 1; i <= 14 ; i++) {
            if(services.get(i).equals("1")) {
                servicesVer = true;
                break;
            }
        }
        if(servicesVer) {
            for (int i = 0; i < arrayServiceCode().length; i++) {
                if(services.get(i+1).equals("1")) {
                    servicesChecked.add(arrayServiceCode()[i]);
                }
            }
            for (int i = 0; i < servicesChecked.size(); i++) {
                if(servicesChecked.get(i).equals("CHECK_OUT")) {
                    servicesChecked.set(i, "CHECK");
                }
            }
            result = String.join("*", servicesChecked);
            Log.d("TAG", "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d("TAG", "getTaxiUrlSearch: " + url);



        return url;
    }

    @SuppressLint("Range")
    public static List<String> logCursor(String table, Context context) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
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
    private void getPhoneNumber () {
        String mPhoneNumber;
        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Manifest.permission.READ_PHONE_NUMBERS: " + ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS));
            Log.d("TAG", "Manifest.permission.READ_PHONE_STATE: " + ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE));
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
//        mPhoneNumber = null;
        if(mPhoneNumber != null) {
            String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
            boolean val = Pattern.compile(PHONE_PATTERN).matcher(mPhoneNumber).matches();
            Log.d("TAG", "onClick No validate: " + val);
            if (val == false) {
                Toast.makeText(getActivity(), getString(R.string.format_phone) , Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onClick:phoneNumber.getText().toString() " + mPhoneNumber);
//                getActivity().finish();

            } else {
                updateRecordsUser(mPhoneNumber, getContext());
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTaxiUrlSearchMarkers(double originLatitude, double originLongitude,
                                                 double toLatitude, double toLongitude,
                                                 String urlAPI, Context context) {
        //  Проверка даты и времени

        List<String> stringList = logCursor(StartActivity.TABLE_ADD_SERVICE_INFO, context);
        String time = stringList.get(1);
        String comment = stringList.get(2);
        String date = stringList.get(3);

        // Origin of route
        String str_origin = originLatitude + "/" + originLongitude;

        // Destination of route
        String str_dest = toLatitude + "/" + toLongitude;

//        Cursor cursorDb = StartActivity.database.query(StartActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        String tarif = logCursor(StartActivity.TABLE_SETTINGS_INFO, context).get(2);


        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";
        String userEmail = logCursor(StartActivity.TABLE_USER_INFO, context).get(3);
        String displayName = logCursor(StartActivity.TABLE_USER_INFO, context).get(4);


        if(urlAPI.equals("costSearchMarkers")) {
            Cursor c = database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);

            if (c.getCount() == 1) {
                phoneNumber = logCursor(StartActivity.TABLE_USER_INFO, context).get(2);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/" + displayName + "(" + userEmail + ")";
        }

        if(urlAPI.equals("orderSearchMarkers")) {
            phoneNumber = logCursor(StartActivity.TABLE_USER_INFO, context).get(2);


            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + displayName  + "/" + addCost + "/" + time + "/" + comment + "/" + date;

            ContentValues cv = new ContentValues();

            cv.put("time", "no_time");
            cv.put("comment", "no_comment");
            cv.put("date", "no_date");

            // обновляем по id
            database.update(StartActivity.TABLE_ADD_SERVICE_INFO, cv, "id = ?",
                    new String[] { "1" });

        }

        // Building the url to the web service
        List<String> services = logCursor(StartActivity.TABLE_SERVICE_INFO, context);
        List<String> servicesChecked = new ArrayList<>();
        String result;
        boolean servicesVer = false;
        for (int i = 1; i <= 14 ; i++) {
            if(services.get(i).equals("1")) {
                servicesVer = true;
                break;
            }
        }
        if(servicesVer) {
            for (int i = 0; i < arrayServiceCode().length; i++) {
                if(services.get(i+1).equals("1")) {
                    servicesChecked.add(arrayServiceCode()[i]);
                }
            }
            for (int i = 0; i < servicesChecked.size(); i++) {
                if(servicesChecked.get(i).equals("CHECK_OUT")) {
                    servicesChecked.set(i, "CHECK");
                }
            }
            result = String.join("*", servicesChecked);
            Log.d("TAG", "getTaxiUrlSearchGeo result:" + result + "/");
        } else {
            result = "no_extra_charge_codes";
        }

        String url = "https://m.easy-order-taxi.site/" + api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d("TAG", "getTaxiUrlSearch: " + url);
        database.close();


        return url;
    }

    private static void insertRecordsOrders( String from, String to,
                                            String from_number, String to_number,
                                            String from_lat, String from_lng,
                                            String to_lat, String to_lng, Context context) {

        String selection = "from_street = ?";
        String[] selectionArgs = new String[] {from};
        SQLiteDatabase database = context.openOrCreateDatabase(StartActivity.DB_NAME, MODE_PRIVATE, null);
        Cursor cursor_from = database.query(StartActivity.TABLE_ORDERS_INFO,
                null, selection, selectionArgs, null, null, null);

        selection = "to_street = ?";
        selectionArgs = new String[] {to};

        Cursor cursor_to = database.query(StartActivity.TABLE_ORDERS_INFO,
                null, selection, selectionArgs, null, null, null);



        if (cursor_from.getCount() == 0 || cursor_to.getCount() == 0) {

            String sql = "INSERT INTO " + StartActivity.TABLE_ORDERS_INFO + " VALUES(?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = database.compileStatement(sql);
            database.beginTransaction();
            try {
                statement.clearBindings();
                statement.bindString(2, from);
                statement.bindString(3, from_number);
                statement.bindString(4, from_lat);
                statement.bindString(5, from_lng);
                statement.bindString(6, to);
                statement.bindString(7, to_number);
                statement.bindString(8, to_lat);
                statement.bindString(9, to_lng);

                statement.execute();
                database.setTransactionSuccessful();

            } finally {
                database.endTransaction();
            }

        }

        cursor_from.close();
        cursor_to.close();

    }

}