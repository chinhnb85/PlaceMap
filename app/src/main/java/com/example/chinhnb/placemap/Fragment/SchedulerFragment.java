package com.example.chinhnb.placemap.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chinhnb.placemap.Activity.LocaltionDetailActivity;
import com.example.chinhnb.placemap.Adapter.DividerItemDecoration;
import com.example.chinhnb.placemap.Adapter.SchedulerAdapter;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.HeaderItem;
import com.example.chinhnb.placemap.Entity.ListItem;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Entity.LocaltionItem;
import com.example.chinhnb.placemap.Event.RecyclerTouchListener;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.example.chinhnb.placemap.Utils.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by CHINHNB on 11/17/2016.
 */

public class SchedulerFragment extends Fragment {
    private static final String TAG = SchedulerFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    @NonNull
    private List<ListItem> items = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SchedulerAdapter mAdapter;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private OnFragmentInteractionListener mListener;
    private Map<Date, List<Localtion>> map = new TreeMap<>();

    public SchedulerFragment() {
    }

    public static SchedulerFragment newInstance(String param1, String param2) {
        SchedulerFragment fragment = new SchedulerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        db = new SQLiteHandler(getActivity());

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_scheduler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DateFormat df = new SimpleDateFormat("EEE, dd/MM/yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        TextView txtTime = (TextView) view.findViewById(R.id.txtTime);
        if(date.contains("Sun")){
            txtTime.setText("Hôm nay: "+date.replace("Sun","Chủ nhật"));
        }else{
            txtTime.setText("Hôm nay: Thứ "+date.replace("Mon","2").replace("Tue","3").replace("Wed","4").replace("Thu","5").replace("Fri","6").replace("Sat","7"));
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ListItem listItem=items.get(position);
                if(listItem.getType()==ListItem.TYPE_LOCALTION) {
                    Localtion localtion = ((LocaltionItem)listItem).getLocaltion();
                    Intent intent = new Intent(getActivity(), LocaltionDetailActivity.class);
                    if (localtion != null) {
                        intent.putExtra("Id", localtion.getId());
                        intent.putExtra("AccountId", localtion.getAccountId());
                        intent.putExtra("Lag", localtion.getLag());
                        intent.putExtra("Lng", localtion.getLng());
                    }
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        String uid=db.getUserDetails().get("uid");
        prepareLocaltionData(uid);

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void prepareLocaltionData(final String userId) {

        // Tag used to cancel the request
        String tag_string_req = "req_localtion";
        pDialog.setMessage("Đang tải dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LIST_SCHEDULER_BY_ACCOUNT_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");

                    if (status) {
                        JSONArray array=jObj.getJSONArray("Data");
                        if(array.length()>0){
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String ischeck=obj.getString("IsCheck");
                                if(ischeck==null){
                                    ischeck="false";
                                }
                                Localtion localtion=new Localtion(
                                        obj.getInt("Id"),
                                        obj.getInt("AccountId"),
                                        Boolean.valueOf(ischeck),
                                        obj.getString("Name"),
                                        obj.getString("Address"),
                                        obj.getString("Email"),
                                        obj.getString("Phone"),
                                        obj.getString("Avatar"),
                                        obj.getDouble("Lag"),
                                        obj.getDouble("Lng"),
                                        obj.getString("Code"),
                                        obj.getString("RepresentActive"),
                                        obj.getInt("CountCheckIn"),
                                        obj.getInt("MinCheckin"),
                                        obj.getBoolean("StatusEdit"),
                                        obj.getString("StatusName"),
                                        DateUtils.formatDate(obj.getString("StartDate"))
                                );
                                Date date=localtion.getStartDate();
                                List<Localtion> value = map.get(date);
                                if (value == null) {
                                    value = new ArrayList<>();
                                    map.put(date, value);
                                }
                                value.add(localtion);
                            }
                            for (Date date : map.keySet()) {
                                HeaderItem header = new HeaderItem(date);
                                items.add(header);
                                for (Localtion localtion : map.get(date)) {
                                    LocaltionItem item = new LocaltionItem(localtion);
                                    items.add(item);
                                }
                            }

                            mAdapter = new SchedulerAdapter(items);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getActivity(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Json error", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.not_network), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("Id", userId);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
