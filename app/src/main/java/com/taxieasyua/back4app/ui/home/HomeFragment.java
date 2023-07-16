package com.taxieasyua.back4app.ui.home;


import static android.graphics.Color.RED;

import static com.taxieasyua.back4app.R.string.address_error_message;
import static com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity.coastOfRoad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taxieasyua.back4app.MainActivity;
import com.taxieasyua.back4app.R;
import com.taxieasyua.back4app.databinding.FragmentHomeBinding;
import com.taxieasyua.back4app.ui.maps.CostJSONParser;
import com.taxieasyua.back4app.ui.maps.OrderJSONParser;
import com.taxieasyua.back4app.ui.maps.ToJSONParser;
import com.taxieasyua.back4app.ui.open_map.OpenStreetMapActivity;
import com.taxieasyua.back4app.ui.start.ResultSONParser;
import com.taxieasyua.back4app.ui.start.StartActivity;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String from, to;
    EditText from_number, to_number;
    String messageResult;
    private Spinner listView;
    Button button;
    private String[] array;
    public String[] arrayStreet = StartActivity.arrayStreet;
    static FloatingActionButton fab_call;
    private final String TAG = "TAG";
    private static final int CM_DELETE_ID = 1;
    String from_street_rout, to_street_rout;
    private int selectedPosition = -1;
    Button mapbut, gpsbut, buttonAddServices;
    AppCompatButton btncost;
    String FromAddressString, ToAddressString;
    Integer selectedItem;

    public static String[] arrayServiceCode() {
        return new String[]{
                "BAGGAGE",
                "ANIMAL",
                "CONDIT",
                "MEET",
                "COURIER",
                "TERMINAL",
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, arrayStreet);

        AutoCompleteTextView textViewFrom =binding.textFrom;
        textViewFrom.setAdapter(adapter);
        from_number = binding.fromNumber;
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

                    String url = "https://m.easy-order-taxi.site/" + StartActivity.api + "/android/autocompleteSearchComboHid/" + from;

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

        AutoCompleteTextView textViewTo =binding.textTo;
        textViewTo.setAdapter(adapter);
        to_number = binding.toNumber;
        textViewTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                              @Override
                                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                  if (connected()) {
                                                      to = String.valueOf(adapter.getItem(position));
                                                      if (to.indexOf("/") != -1) {
                                                          to = to.substring(0, to.indexOf("/"));
                                                      }
                                                      ;
                                                      String url = "https://m.easy-order-taxi.site/" + StartActivity.api + "/android/autocompleteSearchComboHid/" + to;

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

        btncost = binding.btnCost;
        btncost.setOnClickListener(new View.OnClickListener() {
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
                            String urlCost = getTaxiUrlSearch(from, from_number.getText().toString(), to, to_number.getText().toString(), "costSearch");

                            Log.d("TAG", "onClick urlCost: " + urlCost);

                            Map sendUrlMapCost = CostJSONParser.sendURL(urlCost);
                            String orderCost = (String) sendUrlMapCost.get("order_cost");
                            String message = (String) sendUrlMapCost.get("message");


                            if (orderCost.equals("0")) {
                                Log.d("TAG", "onClick 9998465465465: ");
                                if (to.equals(from)) {
                                    textViewTo.setText("");
                                    to = null;
                                }
                                Toast.makeText(getActivity(), getString(R.string.error_message) + message, Toast.LENGTH_SHORT).show();
                            }
                            if (!orderCost.equals("0")) {
                                if(!MainActivity.verifyOrder) {
                                    Toast.makeText(getActivity(), getString(R.string.cost_of_order) + orderCost + getString(R.string.firebase_false_message), Toast.LENGTH_SHORT).show();
                                } else {

                                    MaterialAlertDialogBuilder builderAddCost = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                                    LayoutInflater inflaterCost = getActivity().getLayoutInflater();

                                    View view_cost = inflaterCost.inflate(R.layout.add_cost_layout, null);
                                    builderAddCost.setView(view_cost);
                                    TextView costView = view_cost.findViewById(R.id.cost);
                                    costView.setText(orderCost);
                                    StartActivity.cost = Long.parseLong(orderCost);
                                    StartActivity.addCost = 0;
                                    Button btn_minus = view_cost.findViewById(R.id.btn_minus);
                                    Button btn_plus = view_cost.findViewById(R.id.btn_plus);

                                    btn_minus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if(StartActivity.addCost != 0) {
                                                StartActivity.addCost -= 5;
                                                StartActivity.cost -= 5;
                                                costView.setText(String.valueOf(StartActivity.cost));
                                            }
                                        }
                                    });
                                    btn_plus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            StartActivity.addCost += 5;
                                            StartActivity.cost += 5;
                                            Log.d(TAG, "onClick StartActivity.addCost " + StartActivity.addCost);
                                            costView.setText(String.valueOf(StartActivity.cost));
                                        }
                                    });
                                    if (!StartActivity.verifyPhone) {
                                        getPhoneNumber();
                                    }
                                    if (!StartActivity.verifyPhone) {
                                        MyPhoneDialogFragment bottomSheetDialogFragment = new MyPhoneDialogFragment();
                                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                                    }

                                    builderAddCost
                                            .setPositiveButton(getString(R.string.order), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if(connected()) {
                                                        if (StartActivity.verifyPhone) {
                                                            try {
                                                                String urlOrder = getTaxiUrlSearch(from, from_number.getText().toString(), to, to_number.getText().toString(), "orderSearch");
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
                                                                    Toast.makeText(getActivity(), messageResult, Toast.LENGTH_SHORT).show();

                                                                    if(from_name.equals(to_name)) {
                                                                        if(!sendUrlMap.get("lat").equals("0")) {
                                                                            StartActivity.insertRecordsOrders(
                                                                                    from_name, from_name,
                                                                                    from_number.getText().toString(), from_number.getText().toString(),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng")
                                                                            );
                                                                        }
                                                                    } else {

                                                                        if(!sendUrlMap.get("lat").equals("0")) {
                                                                            StartActivity.insertRecordsOrders(
                                                                                    from_name, to_name,
                                                                                    from_number.getText().toString(), to_number.getText().toString(),
                                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                                    (String) sendUrlMap.get("lat"), (String) sendUrlMap.get("lng")
                                                                            );
                                                                        }
                                                                    }
                                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                                } else {
                                                                    String message = (String) sendUrlMap.get("message");
                                                                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme);
                                                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                                                    View view = inflater.inflate(R.layout.free_message_layout, null);
                                                                    TextView alertMessage = view.findViewById(R.id.text_message);
                                                                    alertMessage.setText(message + getString(R.string.try_again));
                                                                    alertDialogBuilder.setView(view);

                                                                    alertDialogBuilder.setPositiveButton(getString(R.string.help), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                                                    intent.setData(Uri.parse("tel:0674443804"));
                                                                                    startActivity(intent);

                                                                                }
                                                                            })
                                                                            .setNegativeButton(getString(R.string.try_again),null)
                                                                            .show();
                                                                }


                                                            } catch (MalformedURLException |
                                                                     InterruptedException |
                                                                     JSONException e) {
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
                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                }
                                            })
                                            .show();
                                }
                            }

                        } catch (MalformedURLException | InterruptedException |
                                 JSONException e) {
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
                startActivity(new Intent(getActivity(), OpenStreetMapActivity.class));
            }
        });

        fab_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0674443804"));
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

                        dialogFromToOneRout(StartActivity.routChoice(selectedItem + 1));
                    } catch (MalformedURLException | InterruptedException | JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                    }

            }
        });

        return root;
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
    public static ArrayList<Map> routMaps() {
        Map <String, String> routs;
        ArrayList<Map> routsArr = new ArrayList<>();
        Cursor c = StartActivity.database.query(StartActivity.TABLE_ORDERS_INFO, null, null, null, null, null, null);
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

        Log.d("TAG", "routMaps: " + routsArr);
        return routsArr;
    }
    private String[] arrayToRoutsAdapter () {
        ArrayList<Map>  routMaps = routMaps();
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

        if (!hasConnect) {
            Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "connected: " + hasConnect);
        return hasConnect;
    }
    private void dialogFromToOneRout(Map <String, String> rout) throws MalformedURLException, InterruptedException, JSONException {
        if(connected()) {
            Log.d("TAG", "dialogFromToOneRout: " + rout.toString());
            Double from_lat =  Double.valueOf(rout.get("from_lat"));
            Double from_lng = Double.valueOf(rout.get("from_lng"));
            Double to_lat = Double.valueOf(rout.get("to_lat"));
            Double to_lng = Double.valueOf(rout.get("to_lng"));
            FromAddressString = rout.get("from_street");
            Log.d("TAG", "dialogFromToOneRout: FromAddressString" + FromAddressString);
            ToAddressString = rout.get("to_street");
            if(rout.get("from_street").equals(rout.get("to_street"))) {
                ToAddressString =  getString(R.string.on_city_tv);;
            }
            Log.d("TAG", "dialogFromToOneRout: ToAddressString" + ToAddressString);

                String urlCost = OpenStreetMapActivity.getTaxiUrlSearchMarkers(from_lat, from_lng,
                        to_lat, to_lng, "costSearchMarkers");

                Map<String, String> sendUrlMapCost = ToJSONParser.sendURL(urlCost);

                String message = (String) sendUrlMapCost.get("message");
                String orderCost = (String) sendUrlMapCost.get("order_cost");

                GeoPoint startPoint = new GeoPoint(from_lat, to_lat);

                if (orderCost.equals("0")) {
                    coastOfRoad(startPoint, message);
                }
                if (!orderCost.equals("0")) {

                    MaterialAlertDialogBuilder builderAddCost = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    View view_cost = inflater.inflate(R.layout.add_cost_layout, null);
                    builderAddCost.setView(view_cost);
                    TextView costView = view_cost.findViewById(R.id.cost);
                    costView.setText(orderCost);
                    StartActivity.cost = Long.parseLong(orderCost);
                    StartActivity.addCost = 0;
                    Button btn_minus = view_cost.findViewById(R.id.btn_minus);
                    Button btn_plus = view_cost.findViewById(R.id.btn_plus);

                    btn_minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(StartActivity.addCost != 0) {
                                StartActivity.addCost -= 5;
                                StartActivity.cost -= 5;
                                costView.setText(String.valueOf(StartActivity.cost));
                            }
                        }
                    });
                    btn_plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StartActivity.addCost += 5;
                            StartActivity.cost += 5;
                            costView.setText(String.valueOf(StartActivity.cost));
                        }
                    });

                    builderAddCost
                            .setPositiveButton(getString(R.string.order), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(connected()) {
                                        try {
                                            String urlCost = OpenStreetMapActivity.getTaxiUrlSearchMarkers(from_lat, from_lng,
                                                    to_lat, to_lng, "orderSearchMarkers");

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

                                                    Toast.makeText(getActivity(), messageResult, Toast.LENGTH_SHORT).show();
                                                    Log.d("TAG", "onClick9889768465465465464: " );
                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                } else {
                                                    message = (String) sendUrlMapCost.get("message");
                                                    new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme)
                                                            .setMessage(message + OpenStreetMapActivity.ntr)
                                                            .setPositiveButton(OpenStreetMapActivity.hlp, new DialogInterface.OnClickListener() {
                                                                @SuppressLint("SuspiciousIndentation")
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                                    intent.setData(Uri.parse("tel:0674443804"));
                                                                    getActivity().startActivity(intent);
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

                                        } catch (MalformedURLException | InterruptedException |
                                                 JSONException e) {
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

    private String getTaxiUrlSearch(String from, String from_number, String to, String to_number, String urlAPI) {

        // Origin of route
        String str_origin = from + "/" + from_number;

        // Destination of route
        String str_dest = to + "/" + to_number;

        StartActivity.cursorDb = StartActivity.database.query(StartActivity.TABLE_SETTINGS_INFO, null, null, null, null, null, null);
        String tarif =  StartActivity.logCursor(StartActivity.TABLE_SETTINGS_INFO).get(2);


        // Building the parameters to the web service

        String parameters = null;
        String phoneNumber = "no phone";

        if(urlAPI.equals("costSearch")) {
            Cursor c = StartActivity.database.query(StartActivity.TABLE_USER_INFO, null, null, null, null, null, null);
            if (c.getCount() == 1) {
                phoneNumber = StartActivity.logCursor(StartActivity.TABLE_USER_INFO).get(1);
                c.close();
            }
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/" + StartActivity.displayName ;
        }

        if(urlAPI.equals("orderSearch")) {
            phoneNumber = StartActivity.logCursor(StartActivity.TABLE_USER_INFO).get(1);
            parameters = str_origin + "/" + str_dest + "/" + tarif + "/" + phoneNumber + "/"
                    + StartActivity.displayName + "/" + StartActivity.addCost;

        }

        // Building the url to the web service
// Building the url to the web service
        List<String> services = StartActivity.logCursor(StartActivity.TABLE_SERVICE_INFO);
        List<String> servicesChecked = new ArrayList<>();
        String result;
        boolean servicesVer = false;
        for (int i = 1; i <= 15 ; i++) {
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

        String url = "https://m.easy-order-taxi.site/" + StartActivity.api + "/android/" + urlAPI + "/" + parameters + "/" + result;

        Log.d("TAG", "getTaxiUrlSearch: " + url);



        return url;
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
                StartActivity.insertRecordsUser(mPhoneNumber);
            }
        }

    }
    private void phoneNumber() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);

        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.phone_verify_layout, null);

        builder.setView(view);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText phoneNumber = view.findViewById(R.id.phoneNumber);
        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                phoneNumber.setHint("");


            }
        });


//        String result = phoneNumber.getText().toString();
        builder.setTitle(getString(R.string.verify_phone))
                .setPositiveButton(getString(R.string.sent_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(connected()) {
                        Log.d("TAG", "onClick befor validate: ");
                        String PHONE_PATTERN = "((\\+?380)(\\d{9}))$";
                        boolean val = Pattern.compile(PHONE_PATTERN).matcher(phoneNumber.getText().toString()).matches();
                        Log.d("TAG", "onClick No validate: " + val);
                        if (val == false) {
                            Toast.makeText(getActivity(), getString(R.string.format_phone) , Toast.LENGTH_SHORT).show();
                            phoneNumber();
                        } else {
                            StartActivity.insertRecordsUser(phoneNumber.getText().toString());
                            String urlOrder = getTaxiUrlSearch(from, from_number.getText().toString(), to, to_number.getText().toString(), "orderSearch");

                            try {
                                Map<String, String> sendUrlMap = ToJSONParser.sendURL(urlOrder);

                                String orderWeb = (String) sendUrlMap.get("order_cost");
                                if (!orderWeb.equals("0")) {

                                    MaterialAlertDialogBuilder builderAddCost = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
                                    LayoutInflater inflater = getActivity().getLayoutInflater();

                                    View view_cost = inflater.inflate(R.layout.add_cost_layout, null);
                                    builderAddCost.setView(view_cost);
                                    TextView costView = view_cost.findViewById(R.id.cost);
                                    costView.setText(orderWeb);
                                    StartActivity.cost = Long.parseLong(orderWeb);
                                    StartActivity.addCost = 0;
                                    Button btn_minus = view_cost.findViewById(R.id.btn_minus);
                                    Button btn_plus = view_cost.findViewById(R.id.btn_plus);

                                    btn_minus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (StartActivity.addCost != 0) {
                                                StartActivity.addCost -= 5;
                                                StartActivity.cost -= 5;
                                                costView.setText(String.valueOf(StartActivity.cost));
                                            }
                                        }
                                    });
                                    btn_plus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            StartActivity.addCost += 5;
                                            StartActivity.cost += 5;
                                            Log.d(TAG, "onClick StartActivity.addCost " + StartActivity.addCost);
                                            costView.setText(String.valueOf(StartActivity.cost));
                                        }
                                    });


                                    builderAddCost
                                            .setMessage(getString(R.string.cost_of_order))
                                            .setPositiveButton(getString(R.string.order), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    String from_name = (String) sendUrlMap.get("routefrom");
                                                    String to_name = (String) sendUrlMap.get("routeto");
                                                    if (from_name.equals(to_name)) {
                                                        messageResult = getString(R.string.thanks_message) +
                                                                from_name + " " + from_number.getText() + " " + getString(R.string.on_city) +
                                                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);


                                                    } else {
                                                        messageResult = getString(R.string.thanks_message) +
                                                                from_name + " " + from_number.getText() + " " + getString(R.string.to_message) +
                                                                to_name + " " + to_number.getText() + "." +
                                                                getString(R.string.cost_of_order) + orderWeb + getString(R.string.UAH);
                                                    }
                                                    Log.d(TAG, "onClick sendUrlMap: " + from_name + " " + to_name +
                                                            from_number.getText().toString() + " " + to_number.getText().toString() + " " +
                                                            (String) sendUrlMap.get("from_lat") + " " + (String) sendUrlMap.get("from_lng") + " " +
                                                            (String) sendUrlMap.get("lat") + " " + (String) sendUrlMap.get("lng"));

                                                    if (from_name.equals(to_name)) {
                                                        if (!sendUrlMap.get("lat").equals("0")) {
                                                            StartActivity.insertRecordsOrders(
                                                                    from_name, from_name,
                                                                    from_number.getText().toString(), from_number.getText().toString(),
                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng")
                                                            );
                                                        }
                                                    } else {

                                                        if (!sendUrlMap.get("lat").equals("0")) {
                                                            StartActivity.insertRecordsOrders(
                                                                    from_name, to_name,
                                                                    from_number.getText().toString(), to_number.getText().toString(),
                                                                    (String) sendUrlMap.get("from_lat"), (String) sendUrlMap.get("from_lng"),
                                                                    (String) sendUrlMap.get("lat"), (String) sendUrlMap.get("lng")
                                                            );
                                                        }
                                                    }
                                                    //                                                                        StartActivity.insertRecordsOrders(from_name, to_name,
                                                    //                                                                                from_number.getText().toString(), to_number.getText().toString());

                                                    new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme)
                                                            .setMessage(messageResult)
                                                            .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .show();
                                                }
                                            })
                                            .setNegativeButton(OpenStreetMapActivity.cbt, null)
                                            .show();
                                                } else
                                    {
                                                    String message = (String) sendUrlMap.get("message");
                                                    new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme)
                                                            .setMessage(message + getString(R.string.try_again))
                                                            .setPositiveButton(getString(R.string.help), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                                    intent.setData(Uri.parse("tel:0674443804"));
                                                                    startActivity(intent);

                                                                }
                                                            })
                                                            .setNegativeButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    if (connected()) {
                                                                        button.setVisibility(View.VISIBLE);
                                                                        getActivity().finish();
                                                                        //                                                                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                        //                                                                                            startActivity(intent);
                                                                    }
                                                                }
                                                            })
                                                            .show();

                                     }

                            } catch (MalformedURLException |
                                     InterruptedException |
                                     JSONException e) {
                                Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                            }
                        }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.verify_internet), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getString(R.string.please_phone_message), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                })
                .show();

    }
    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        }
        Log.d(TAG, "checkPermission: +++ " +  ContextCompat.checkSelfPermission(getActivity(), permission));
    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            StartActivity.reIndexOrders();
            // извлекаем id записи и удаляем соответствующую запись в БД
            long del_id = acmi.id+1;
            int i_del =  StartActivity.database.delete(StartActivity.TABLE_ORDERS_INFO, "id = " + del_id, null);
            StartActivity.reIndexOrders();
            getActivity().finish();

            return true;
        }
        return super.onContextItemSelected(item);
    }

}