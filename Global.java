package com.imusic.android;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by Kamran Wajdani on 6/11/2018.
 */

public class Global {

    private static RequestQueue requestQueue;
    private static String ServerUri = "http://localhost/android/";

    public static Global global;
    private Context context;

    private Global(Context ctx){
        this.context = ctx;
    }

    public static Global getInstance(Context context){
        if(null == global){
            global = new Global(context);
        }
        return global;
    }


    public void Post(Context context, String mod, String data, String params, final VolleyCallback callback){

        if(null==requestQueue) requestQueue = Volley.newRequestQueue(context);
        final String body = data;
        final String uri = ServerUri + mod;
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (null != response) {
                        callback.onSuccessResponse(String.valueOf(response));
                    } else {
                        Log.i("iMusic", "Empty response");
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    callback.onErrorResponse(error.getMessage());
                }
            }){

                @Override
                public String getBodyContentType(){
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody(){
                    try{
                        return body == null ? null : body.getBytes();
                    }catch(UnsupportedCharsetException e){
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response){
                    try{
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if(null==cacheEntry){
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitBuRefreshed = 3 * 60 * 1000; //3 mints long
                        final long cacheExpired = 24 * (60 * 60 * 1000); //24 hours long
                        long now = System.currentTimeMillis();
                        final long softExpire = now + cacheHitBuRefreshed;
                        final long ttl = now + cacheExpired;

                        cacheEntry.data = response.data;
                        cacheEntry.softTtl = softExpire;
                        cacheEntry.ttl = ttl;
                        String headerValue = response.headers.get("Date");
                        if(headerValue!=null){
                            cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        headerValue = response.headers.get("Last-Modified");
                        if(headerValue!=null){
                            cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        cacheEntry.responseHeaders = response.headers;
                        final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(jsonString, cacheEntry);

                    }catch(UnsupportedEncodingException e){
                        return Response.error(new ParseError(e));
                    }catch(Exception e){
                        return Response.error(new ParseError(e));
                    }
                }


                @Override
                public void deliverError(VolleyError error){
                    super.deliverError(error);
                }

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError){
                    return super.parseNetworkError(volleyError);
                }
            };

            int socketTimeout = 30 * 1000; //30 changes
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
