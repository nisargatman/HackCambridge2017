package com.example.matthew.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.DataFormatException;

import static com.example.matthew.myapplication.R.id.toolbar;

public class MainActivity extends AppCompatActivity {

    ReceiptPack receiptPack;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ReceiptPack receiptPack = new ReceiptPack("{ 'receipts': [{ '0': [{'vendor':'a','total':'2','date':'1994'}] }] }");
        boolean populateFake = false;
        try {
            FileInputStream receiptsdbFile = openFileInput("receiptsdb");
            try {
                Log.d(TAG,receiptsdbFile.toString());
                InputStreamReader inputStreamReader = new InputStreamReader(receiptsdbFile);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                Log.i("info", stringBuilder.toString());
                receiptPack = new ReceiptPack(stringBuilder.toString());
            } catch (ReceiptPack.ReceiptException e) {
                e.printStackTrace();
                populateFake = true;
            }
            receiptsdbFile.close();
        } catch(IOException fileE) {
            populateFake = true;
        }
        if (populateFake) {
            try {
                receiptPack = new ReceiptPack(
                        "{" +
                                "'receipts': {" +
                                "'0': {" +
                                "'vendor':'Sains','total':'2','date':'1994/01/01'" +
                                "}," +
                                "'2': {" +
                                "'vendor':'Amazon','total':'2','date':'1994/01/02'" +
                                "}" +
                                "}" +
                                "}");
                FileOutputStream internalStorageFile = openFileOutput("receiptsdb", Context.MODE_PRIVATE);
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(internalStorageFile);
//                outputStreamWriter.append((CharSequence)receiptPack.toString());
                internalStorageFile.write(receiptPack.getJsonString().getBytes());
                internalStorageFile.close();
            } catch (ReceiptPack.ReceiptException e) {
                e.printStackTrace();
                receiptPack = new ReceiptPack();
            } catch (IOException e) {
                e.printStackTrace();
                receiptPack = new ReceiptPack();
            }
        }

        updateTable();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean updateTable() {
        // Update items in table
        TableLayout table = (TableLayout) findViewById(R.id.reciepts_table);
        // Remove previous items
        table.removeAllViews();

        for (int i=0; i<0; i++) {
            addReceiptRow(table,"Cambridge Wine Merchants","£300", "28-01-2017");
        }
        for (Iterator<ReceiptPack.Receipt> i = receiptPack.getReceiptsIterator(); i.hasNext(); ) {
            ReceiptPack.Receipt receipt = i.next();
            addReceiptRow(table,receipt.getVendor(),receipt.getTotal(),receipt.getDate());
        }
        return true;
    }

    public void addReceiptRow(TableLayout table, String vendorName, String total, String timestamp) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView vendor = new TextView(this);
        vendor.setText(vendorName);
        TextView cost = new TextView(this);
        cost.setText(total);
        row.addView(vendor);
        row.addView(cost);
        table.addView(row);
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

    private static final int SELECT_PICTURE = 1;

    public void startCamera(View v) {

//        Snackbar.make(v," Button pressed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//        Intent intent = new Intent(this, CameraActivity.class);
//        startActivity(intent);


        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[] { takePhotoIntent }
                );

        startActivityForResult(chooserIntent, SELECT_PICTURE);

        return;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                Snackbar.make(findViewById(R.id.toolbar),"Picture loaded", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Bitmap testImage = BitmapFactory.decodeStream(inputStream);
                double w = testImage.getWidth();
                double h = testImage.getHeight();
                Bitmap scaledImage;
                int maxDim = 2000;
                if (w > h) {
                    if (w > maxDim) {
                        scaledImage = Bitmap.createScaledBitmap(testImage,maxDim,(int)(h*w/maxDim),false);
                    } else {
                        scaledImage = testImage;
                    }
                } else {
                    if (h > maxDim) {
                        scaledImage = Bitmap.createScaledBitmap(testImage,(int)(h*w/maxDim),maxDim,false);
                    } else {
                        scaledImage = testImage;
                    }
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                scaledImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                try {
                    JSONObject jsonObject = new JSONObject("{ 'CustomerId': 'matt', 'id': '000000123' }");
                    jsonObject.put("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
                    Snackbar.make((View) findViewById(R.id.toolbar), "Found in JSON: " + jsonObject.getString("image"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //Log.d(TAG,"Base64-encoded image length: " + String.format("%1$d",jsonObject.getString("image").length()));

                    RequestQueue queue = Volley.newRequestQueue(this);
                    String url = "http://35.167.15.229:5000/image/v1/read_text";
                    //final TextView sillyTextView = (TextView) findViewById(R.id.mytext);

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Snackbar.make((View) findViewById(R.id.toolbar), "Response: " + response.toString(), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Snackbar.make((View) findViewById(R.id.toolbar), "Response: Error :(", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
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
}
