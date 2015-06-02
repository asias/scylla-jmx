/*
 * Copyright 2015 Cloudius Systems
 */
package com.cloudius.urchin.api;

import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.cloudius.urchin.utils.SnapshotDetailsTabularData;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.MediaType;

public class APIClient {
    JsonReaderFactory factory = Json.createReaderFactory(null);

    public static String getBaseUrl() {
        return "http://" + System.getProperty("apiaddress", "localhost") + ":"
                + System.getProperty("apiport", "10000");
    }

    public Builder get(String path) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(UriBuilder.fromUri(getBaseUrl())
                .build());
        return service.path(path).accept(MediaType.APPLICATION_JSON);
    }

    public Builder get(String path, MultivaluedMap<String, String> queryParams) {
        if (queryParams == null) {
            return get(path);
        }
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(UriBuilder.fromUri(getBaseUrl())
                .build());
        return service.queryParams(queryParams).path(path)
                .accept(MediaType.APPLICATION_JSON);
    }

    public void post(String path, MultivaluedMap<String, String> queryParams) {
        if (queryParams != null) {
            get(path, queryParams).post();
            return;
        }
        get(path).post();
    }

    public void post(String path) {
        post(path, null);
    }

    public void delete(String path, MultivaluedMap<String, String> queryParams) {
        if (queryParams != null) {
            get(path, queryParams).delete();
            return;
        }
        get(path).delete();
    }

    public void delete(String path) {
        delete(path, null);
    }

    public String getStringValue(String string,
            MultivaluedMap<String, String> queryParams) {
        if (!string.equals("")) {
            return get(string, queryParams).get(String.class);
        }
        return "";
    }

    public String getStringValue(String string) {
        return getStringValue(string, null);
    }

    public JsonReader getReader(String string,
            MultivaluedMap<String, String> queryParams) {
        return factory.createReader(new StringReader(getStringValue(string,
                queryParams)));
    }

    public JsonReader getReader(String string) {
        return getReader(string, null);
    }

    public String[] getStringArrValue(String string) {
        List<String> val = getListStrValue(string);
        return val.toArray(new String[val.size()]);
    }

    public int getIntValue(String string,
            MultivaluedMap<String, String> queryParams) {
        return Integer.parseInt(getStringValue(string, queryParams));
    }

    public int getIntValue(String string) {
        return getIntValue(string, null);
    }

    public boolean getBooleanValue(String string) {
        return Boolean.parseBoolean(getStringValue(string));
    }

    public double getDoubleValue(String string) {
        return Double.parseDouble(getStringValue(string));
    }

    public List<String> getListStrValue(String string,
            MultivaluedMap<String, String> queryParams) {
        JsonReader reader = getReader(string, queryParams);
        JsonArray arr = reader.readArray();
        List<String> res = new ArrayList<String>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            res.add(arr.getString(i));
        }
        reader.close();
        return res;

    }

    public List<String> getListStrValue(String string) {
        return getListStrValue(string, null);

    }

    public static List<String> listStrFromJArr(JsonArray arr) {
        List<String> res = new ArrayList<String>();
        for (int i = 0; i < arr.size(); i++) {
            res.add(arr.getString(i));
        }
        return res;
    }

    public static String join(String[] arr, String joiner) {
        String res = "";
        if (arr != null) {
            for (String name : arr) {
                if (name != null && !name.equals("")) {
                    if (!res.equals("")) {
                        res = res + ",";
                    }
                    res = res + name;
                }
            }
        }
        return res;
    }

    public static String join(String[] arr) {
        return join(arr, ",");
    }

    public static boolean set_query_param(
            MultivaluedMap<String, String> queryParams, String key, String value) {
        if (queryParams != null && key != null && value != null
                && !value.equals("")) {
            queryParams.add(key, value);
            return true;
        }
        return false;
    }

    public static boolean set_bool_query_param(
            MultivaluedMap<String, String> queryParams, String key,
            boolean value) {
        if (queryParams != null && key != null && value) {
            queryParams.add(key, "true");
            return true;
        }
        return false;
    }

    public Map<List<String>, List<String>> getMapListStrValue(String string,
            MultivaluedMap<String, String> queryParams) {
        if (string.equals("")) {
            return null;
        }
        JsonReader reader = getReader(string, queryParams);
        JsonArray arr = reader.readArray();
        Map<List<String>, List<String>> map = new HashMap<List<String>, List<String>>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.getJsonObject(i);
            if (obj.containsKey("key") && obj.containsKey("value")) {
                map.put(listStrFromJArr(obj.getJsonArray("key")),
                        listStrFromJArr(obj.getJsonArray("value")));
            }
        }
        reader.close();
        return map;
    }

    public Map<List<String>, List<String>> getMapListStrValue(String string) {
        return getMapListStrValue(string, null);
    }

    public Map<String, String> getMapStrValue(String string,
            MultivaluedMap<String, String> queryParams) {
        if (string.equals("")) {
            return null;
        }
        JsonReader reader = getReader(string, queryParams);
        JsonArray arr = reader.readArray();
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.getJsonObject(i);
            if (obj.containsKey("key") && obj.containsKey("value")) {
                map.put(obj.getString("key"), obj.getString("value"));
            }
        }
        reader.close();
        return map;
    }

    public Map<String, String> getMapStrValue(String string) {
        return getMapStrValue(string, null);
    }

    public List<InetAddress> getListInetAddressValue(String string,
            MultivaluedMap<String, String> queryParams) {
        List<String> vals = getListStrValue(string, queryParams);
        List<InetAddress> res = new ArrayList<InetAddress>();
        for (String val : vals) {
            try {
                res.add(InetAddress.getByName(val));
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return res;
    }

    public List<InetAddress> getListInetAddressValue(String string) {
        return getListInetAddressValue(string, null);
    }

    public Map<String, TabularData> getMapStringTabularDataValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    private TabularDataSupport getSnapshotData(String ks, JsonArray arr) {
        TabularDataSupport data = new TabularDataSupport(
                SnapshotDetailsTabularData.TABULAR_TYPE);

        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.getJsonObject(i);
            if (obj.containsKey("key") && obj.containsKey("cf")) {
                SnapshotDetailsTabularData.from(obj.getString("key"), ks,
                        obj.getString("cf"), obj.getInt("total"),
                        obj.getInt("live"), data);
            }
        }
        return data;
    }

    public Map<String, TabularData> getMapStringSnapshotTabularDataValue(
            String string, MultivaluedMap<String, String> queryParams) {
        if (string.equals("")) {
            return null;
        }
        JsonReader reader = getReader(string, queryParams);
        JsonArray arr = reader.readArray();
        Map<String, TabularData> map = new HashMap<>();

        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.getJsonObject(i);
            if (obj.containsKey("key") && obj.containsKey("value")) {
                String key = obj.getString("key");
                map.put(key, getSnapshotData(key, obj.getJsonArray("value")));
            }
        }
        reader.close();
        return map;
    }

    public long getLongValue(String string) {
        return Long.parseLong(getStringValue(string));
    }

    public Map<InetAddress, Float> getMapInetAddressFloatValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Long> getMapStringLongValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public long[] getLongArrValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Integer> getMapStringIntegerValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public int[] getIntArrValue(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Long> getListMapStringLongValue(String string,
            MultivaluedMap<String, String> queryParams) {
        if (string.equals("")) {
            return null;
        }
        JsonReader reader = getReader(string, queryParams);
        JsonArray arr = reader.readArray();
        Map<String, Long> map = new HashMap<String, Long>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.getJsonObject(i);
            Iterator<String> it = obj.keySet().iterator();
            String key = "";
            long val = -1;
            while (it.hasNext()) {
                String k = it.next();
                if (obj.get(k) instanceof JsonString) {
                    key = obj.getString(k);
                } else {
                    val = obj.getInt(k);
                }
            }
            if (val > 0 && !key.equals("")) {
                map.put(key, val);
            }

        }
        reader.close();
        return map;
    }
    
    public Map<String, Long> getListMapStringLongValue(String string) {
        return getListMapStringLongValue(string, null);
    }

    public JsonArray getJsonArray(String string,
            MultivaluedMap<String, String> queryParams) {
        if (string.equals("")) {
            return null;
        }
        JsonReader reader = getReader(string, queryParams);
        JsonArray res = reader.readArray();
        reader.close();
        return res;
    }
    
    public JsonArray getJsonArray(String string) {
        return getJsonArray(string, null);
    }
}