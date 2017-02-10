package com.example.chinhnb.placemap.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chinhnb.placemap.Activity.CheckedActivity;
import com.example.chinhnb.placemap.Activity.LocaltionDetailActivity;
import com.example.chinhnb.placemap.Activity.MapActivity;
import com.example.chinhnb.placemap.Adapter.AutoCompleteAdapter;
import com.example.chinhnb.placemap.Adapter.DividerItemDecoration;
import com.example.chinhnb.placemap.Adapter.LocaltionAdapter;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Event.RecyclerTouchListener;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CHINHNB on 11/17/2016.
 */

public class LocaltionFragment extends Fragment {
    private static final String TAG = LocaltionFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private List<Localtion> localtionList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LocaltionAdapter mAdapter;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private AutoCompleteTextView actv;
    private OnFragmentInteractionListener mListener;
    private List<Localtion> listAutocomplete = new ArrayList<>();
    private  boolean isAutocomplete=false;

    public LocaltionFragment() {
    }

    public static LocaltionFragment newInstance(String param1, String param2) {
        LocaltionFragment fragment = new LocaltionFragment();
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

        return inflater.inflate(R.layout.fragment_localtion, container, false);
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
        actv = (AutoCompleteTextView) view.findViewById(R.id.autocompleteSearch);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Localtion localtion = localtionList.get(position);
                if(isAutocomplete){
                    localtion = listAutocomplete.get(position);
                }
                Intent intent = new Intent(getActivity(), LocaltionDetailActivity.class);
                if(localtion!=null) {
                    intent.putExtra("Id", localtion.getId());
                    intent.putExtra("AccountId", localtion.getAccountId());
                    intent.putExtra("Lag", localtion.getLag());
                    intent.putExtra("Lng", localtion.getLng());
                }
                startActivity(intent);
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
                AppConfig.URL_LIST_LOCALTION, new Response.Listener<String>() {

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
                                        obj.getBoolean("StatusEdit")
                                );
                                localtionList.add(localtion);
                                listAutocomplete.add(localtion);
                            }
                            mAdapter = new LocaltionAdapter(localtionList);
                            mRecyclerView.setAdapter(mAdapter);

                            AutoCompleteAdapter adapter = new AutoCompleteAdapter(getActivity(), R.layout.fragment_localtion, R.id.lbl_name, listAutocomplete);
                            actv.setAdapter(adapter);
                            //actv.setThreshold(3);
                            actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
                                                        long arg3) {
                                    Localtion selected = (Localtion) adapterView.getAdapter().getItem(position);

                                    listAutocomplete.clear();
                                    listAutocomplete.add(selected);
                                    mAdapter = new LocaltionAdapter(listAutocomplete);
                                    mRecyclerView.setAdapter(mAdapter);
                                }
                            });

                            actv.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    isAutocomplete=true;
                                    if(charSequence.toString().equals("")){
                                        mAdapter = new LocaltionAdapter(localtionList);
                                        mRecyclerView.setAdapter(mAdapter);
                                        isAutocomplete=false;
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
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
