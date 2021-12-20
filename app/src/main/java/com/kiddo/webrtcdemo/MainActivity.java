package com.kiddo.webrtcdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kiddo.webrtcdemo.activity.CallActivity;
import com.kiddo.webrtcdemo.bean.UserInfo;
import com.kiddo.webrtcdemo.cons.UrlCons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    EditText userIdET = null;
    EditText wsUrlET = null;
    EditText httpUrlET = null;
    ListView listView = null;
    List<UserInfo> messageList = new ArrayList<UserInfo>();
    ArrayAdapter<UserInfo> adapter;

    private String[] MY_PERMISSION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        userIdET = findViewById(R.id.et_user_id);
        wsUrlET = findViewById(R.id.et_ws_url);
        httpUrlET = findViewById(R.id.et_http_url);
        listView = findViewById(R.id.list_view_users);

        wsUrlET.setText(UrlCons.ws_url);
        httpUrlET.setText(UrlCons.http_url);

        adapter = new MyArrayAdapter(this, R.layout.user_list, messageList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        //默认每次都获取授权
        if (!EasyPermissions.hasPermissions(MainActivity.this, MY_PERMISSION)) {
            EasyPermissions.requestPermissions(MainActivity.this, "需要存储、相机、录音权限", 100, MY_PERMISSION);
        }
    }

    private class MyArrayAdapter extends ArrayAdapter {

        private final int resourceId;

        public MyArrayAdapter(Context context, int textViewResourceId, List<UserInfo> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象

            UserInfo userInfo = (UserInfo) getItem(position);

            TextView userId = (TextView) view.findViewById(R.id.user_id);
            userId.setText(userInfo.getUserId());

            return view;
        }
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg) {
            Log.d(TAG, "onItemClick pos=" + pos);

            if (userIdET.getText().toString().length() == 0) {
                Log.e(TAG, "userId cant be empty");
                return;
            }

            Intent intent = new Intent(MainActivity.this, CallActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", userIdET.getText().toString());
            bundle.putString("toUserId", messageList.get(pos).getUserId());
            bundle.putString("wsUrl", wsUrlET.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    public void connectWs(View view) {
    }

    public void refreshUserList(View view) {
        fulfillListView();
    }

    private void fulfillListView() {
        messageList.clear();
        adapter.notifyDataSetChanged();

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(httpUrlET.getText().toString() + "getUserList.json")
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.i(TAG, "onResponse result: " + result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");


                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObjTemp = jsonArray.getJSONObject(i);
                            String userId = jsonObjTemp.getString("userId");
                            UserInfo userInfo = new UserInfo();
                            userInfo.setUserId(userId);
                            messageList.add(userInfo);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}