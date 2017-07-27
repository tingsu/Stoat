package com.bretternst.URLazy;

import java.io.IOException;
import java.util.*;
import java.net.*;
import java.util.Map.Entry;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.database.DataSetObservable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import org.json.*;

public class MainActivity extends Activity implements ExpandableListAdapter, OnChildClickListener {
    final String MULTICAST_ADDR = "239.255.41.1";
    final int SERVER_PORT = 4111;
    final int TIMEOUT_DELAY = 3000;
    final int TIMEOUT_RETRIES = 3;
    InetAddress multicastAddr;
    private List<String> hosts;
    private List<ArrayList<Entry<String, String>>> content;
    private DataSetObservable observable;
    private ExpandableListView list;
    private MulticastSocket socket;
    private ProgressDialog progressDialog;
    private WifiManager.MulticastLock mcLock;
    private Thread socketThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            multicastAddr = InetAddress.getByName(MULTICAST_ADDR);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        hosts = new ArrayList<String>();
        content = new ArrayList<ArrayList<Entry<String,String>>>();
        observable = new DataSetObservable();
        list = (ExpandableListView)this.findViewById(R.id.list);
        list.setAdapter(this);
        list.setOnChildClickListener(this);

        // Check for wi-fi
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(!wifiInfo.isConnected()) {
            showError("This app requires a Wi-Fi connection.");
            return;
        }

        // Build progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void start() {
        progressDialog.show();
        stop();

        socketThread = new Thread() {
            @Override
            public void run() {
                // Acquire multicast lock
                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                if(wifi != null) {
                    mcLock = wifi.createMulticastLock("urlazy");
                    mcLock.acquire();
                }

                // set up the socket
                try {
                    socket = new MulticastSocket();
                    socket.setLoopbackMode(false);
                    socket.setSoTimeout(TIMEOUT_DELAY);
                    socket.joinGroup(multicastAddr);
                } catch (SocketException e) {
                    onError("Network error.");
                    return;
                } catch (IOException e) {
                    onError("Network error.");
                    return;
                }

                try {
                    byte[] buf = new byte[65536];
                    int timeouts = 0;
                    onSend();
                    while(true) {
                        if(this.isInterrupted())
                            return;
                        try {
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);
                            if(this.isInterrupted())
                                return;
                            final JSONObject payload = new JSONObject(new String(packet.getData(), 0, packet.getLength(), "UTF-8"));
                            onReceived(payload);
                        } catch (SocketTimeoutException e) {
                            timeouts++;
                            if(timeouts < TIMEOUT_RETRIES)
                                onSend();
                            else
                                break;
                        } catch (JSONException e) {
                            continue;
                        }
                    }
                }
                catch (IOException e) {
                    return;
                }
                onFinish();
            }
        };
        socketThread.start();
    }

    private void onSend() throws IOException {
        byte[] bytes = "query".getBytes();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, multicastAddr, 4111);
        socket.send(packet);
    }

    public void stop() {
        if(socketThread != null) {
            socketThread.interrupt();
        }
        if(socket != null) {
            socket.close();
        }
        socket = null;
        socketThread = null;

        if(mcLock != null) {
            mcLock.release();
            mcLock = null;
        }
    }

    private void onError(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                showError(message);
            }
        });
    }

    private void onFinish() {
        runOnUiThread((new Runnable() {
            public void run() {
                progressDialog.hide();
                stop();
                if(hosts.size() == 0) {
                    showMessage("No collections found. Make sure a URLazy server is running on your network.");
                }
            }
        }));
    }

    private void showError(String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void showMessage(String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void onReceived(final JSONObject payload) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String host = payload.getString("host");
                    if(hosts.contains(host))
                        return;
                    ArrayList<Entry<String,String>> pairs = new ArrayList<Entry<String,String>>();
                    hosts.add(host);
                    content.add(pairs);
                    JSONObject data = payload.getJSONObject("content");
                    Iterator keys = data.keys();
                    List<String> orderedKeys = new ArrayList<String>();
                    while (keys.hasNext()) {
                        orderedKeys.add((String)keys.next());
                    }
                    java.util.Collections.sort(orderedKeys);
                    keys = orderedKeys.iterator();
                    while(keys.hasNext()) {
                        String key = (String)keys.next();
                        String val = data.getString(key);
                        pairs.add(new AbstractMap.SimpleEntry<String,String>(key, val));
                    }
                    observable.notifyChanged();
                    list.expandGroup(hosts.size() - 1);
                    progressDialog.hide();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onRefresh(View view) {
        if(progressDialog == null)
            return;
        hosts.clear();
        content.clear();
        observable.notifyInvalidated();
        start();
    }

    @Override
    public void onStop() {
        super.onStop();
        stop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh(null);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        observable.registerObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        observable.unregisterObserver(dataSetObserver);
    }

    @Override
    public int getGroupCount() {
        return hosts.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return content.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return hosts.get(i);
    }

    @Override
    public Object getChild(int i, int i2) {
        return content.get(i).get(i2).getKey();
    }

    @Override
    public long getGroupId(int i) {
        return ((Integer)i).longValue();
    }

    @Override
    public long getChildId(int i, int i2) {
        return ((Integer)i2).longValue();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        view = this.getLayoutInflater().inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
        TextView text = (TextView)view.findViewById(android.R.id.text1);
        text.setText(hosts.get(i));
        return view;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        view = this.getLayoutInflater().inflate(android.R.layout.simple_expandable_list_item_2, viewGroup, false);
        TextView text = (TextView)view.findViewById(android.R.id.text1);
        text.setText(content.get(i).get(i2).getKey());
        text = (TextView)view.findViewById(android.R.id.text2);
        text.setText(content.get(i).get(i2).getValue());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return hosts.isEmpty();
    }

    @Override
    public void onGroupExpanded(int i) {
    }

    @Override
    public void onGroupCollapsed(int i) {
    }

    @Override
    public long getCombinedChildId(long l, long l2) {
        return l * 1000L + l2;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return l * 1000L;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
        String url = content.get(groupPosition).get(childPosition).getValue();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
        return true;
    }
}
