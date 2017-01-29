package com.example.matthew.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PHOTO_FOR_SENDING = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            //ReceiptPack receiptPack = new ReceiptPack("{ 'receipts': [{ '0': [{'vendor':'a','total':'2','date':'1994'}] }] }");
            ReceiptPack receiptPack = new ReceiptPack(
                    "{" +
                            "'receipts': {" +
                                "'0': {" +
                                    "'vendor':'a','total':'2','date':'1994'" +
                                "}," +
                                "'2': {" +
                                "'vendor':'a','total':'2','date':'1994'" +
                                "}" +
                            "}" +
                    "}");
        } catch(ReceiptPack.ReceiptException e) {
            ((TextView)findViewById(R.id.mytext)).setText(e.getMessage());
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending test data...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                sendImageToServer();
            }
        });
    }

    public void sendImageToServer() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_SENDING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_SENDING && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = findViewById(R.id.app_bar).getContext().getContentResolver().openInputStream(data.getData());
                //Log.d(data.getType());
                Bitmap testImage = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                testImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                try {
                    JSONObject jsonObject = new JSONObject("{ 'CustomerId': 'matt', 'id': '000000123' }");
                    jsonObject.put("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
                    Snackbar.make((View) findViewById(R.id.app_bar), "Found in JSON: " + jsonObject.getString("image"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    RequestQueue queue = Volley.newRequestQueue(this);
                    String url = "http://35.167.15.229:5000/image/v1/read_text";
                    final TextView sillyTextView = (TextView) findViewById(R.id.mytext);

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    sillyTextView.setText("Response is: " + response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            sillyTextView.setText("Error :(");
                        }
                    });
                    queue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    public void sendTestDataToServer() {

        try {
            int[] testColours = {100,120,140,150,100,300,100,300};
            Bitmap testImage = Bitmap.createBitmap(testColours, 4, 2, Bitmap.Config.RGB_565);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            testImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            /*
            ByteBuffer byteBuffer = ByteBuffer.allocate(testImage.getByteCount());
            testImage.copyPixelsToBuffer(byteBuffer);
            stream.write(byteBuffer.array());
            */
            JSONObject jsonObject = new JSONObject("{ 'CustomerId': 'matt', 'id': '000000123' }");
            jsonObject.put("image", Base64.encode(stream.toByteArray(),Base64.DEFAULT));
            Snackbar.make((View)findViewById(R.id.app_bar), "Found in JSON: " + jsonObject.getString("image"), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://35.167.15.229:5000/image/v1/read_text";
            final TextView sillyTextView = (TextView) findViewById(R.id.mytext);
            /*
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    sillyTextView.setText("Response is: " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    sillyTextView.setText("Error :(");
                }
            });*/
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            sillyTextView.setText("Response is: " + response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    sillyTextView.setText("Error :(");
                }
            });
            queue.add(jsonObjectRequest);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
