package com.example.matthew.myapplication;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by chris on 28/01/2017.
 */

public class ReceiptPack {

    private static final String JSON_RECEIPTS_HOLDER = "receipts";

    private JSONObject jsonObject;
    public static String[] requiredFields = {
            "vendor",
            "total",
            "date"
    };
    private ArrayList<Receipt> receipts = new ArrayList<Receipt>();

    public ReceiptPack() {}
    public ReceiptPack(JSONObject jsonObject) throws ReceiptException {
        if (jsonObject.has(JSON_RECEIPTS_HOLDER)) {
            this.jsonObject = jsonObject;
            populateFromJSON();
        } else {
            throw(new ReceiptException(""));
        }
    }
    public ReceiptPack(String jsonString) throws ReceiptException {
        try {
            JSONObject candidateJsonObject = new JSONObject(jsonString);
            if (candidateJsonObject.has(JSON_RECEIPTS_HOLDER)) {
                this.jsonObject = candidateJsonObject;
                populateFromJSON();
            } else {
                throw(new ReceiptException(""));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ReceiptException();
        }
    }

    public void populateFromJSON() {
        try {
            JSONObject jsonReceipts = jsonObject.getJSONObject(JSON_RECEIPTS_HOLDER);
            Iterator<String> receiptKeys = jsonReceipts.keys();
            receipts.clear();
            for (; receiptKeys.hasNext(); ) {
                String thisKey = receiptKeys.next();
                Log.d(TAG, "Trying the key: " + thisKey);
                try {
                    receipts.add(new Receipt(thisKey,jsonReceipts.getJSONObject(thisKey)));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ReceiptException e) {
                    e.printStackTrace();
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
    public Iterator<Receipt> getReceiptsIterator() {
        return receipts.iterator();
    }
    public String getJsonString() {
        repackJsonObject();
        return jsonObject.toString();
    }

    public void repackJsonObject() {
        JSONObject newJsonObject = new JSONObject();
        JSONObject receiptsHolder = new JSONObject();
        for (Iterator<Receipt> i = getReceiptsIterator(); i.hasNext(); ) {
            Receipt thisReceipt = i.next();
            try {
                receiptsHolder.put(thisReceipt.getIdentifier(), thisReceipt.getJSONObject());
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            newJsonObject.put(JSON_RECEIPTS_HOLDER, receiptsHolder);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean insertReceiptData(String identifier, JSONObject jsonObject) {
        try {
            Receipt receipt = new Receipt(identifier, jsonObject);
            receipts.add(receipt);
            repackJsonObject();
            Log.d("asd",this.jsonObject.toString());
            return true;
        } catch(ReceiptException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void readFromStorage(FileInputStream receiptsdbFile) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(receiptsdbFile);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }
            this.jsonObject = new JSONObject(stringBuilder.toString());
            populateFromJSON();
            Log.d("ReceiptPack","Managed to read in from DB file");
            receiptsdbFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void writeToStorage(FileOutputStream internalStorageFile) {
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(internalStorageFile);
//                outputStreamWriter.append((CharSequence)receiptPack.toString());
        try {
            internalStorageFile.write(getJsonString().getBytes());
            Log.d("ReceiptPack",getJsonString());
            Log.d("ReceiptPack","Managed to write to DB file");
            internalStorageFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ReceiptException extends Exception {
        public ReceiptException() {
            super();
        }
        public ReceiptException(String s) {
            super(s);
        }
    }

    public class Receipt {

        private JSONObject jsonObject;
        private String identifier;
        private boolean isValid;

        public Receipt(String identifier, JSONObject jsonReceipt) throws ReceiptException{
            jsonObject = jsonReceipt;
            this.identifier = identifier;
            validateJson();
        }
        public Receipt(String identifier, String jsonReceipt) throws ReceiptException {
            this.identifier = identifier;
            try {
                jsonObject = new JSONObject(jsonReceipt);
                validateJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void validateJson() throws ReceiptException {
            for (String thisField : requiredFields) {
                if (!jsonObject.has(thisField)) {
                    throw(new ReceiptException());
                }
            }
        }

        public String getVendor() {
            try {
                return jsonObject.getString("vendor");
            } catch(JSONException e) {
                e.printStackTrace();
                return "<Vendor missing>";
            }
        }
        public String getTotal() {
            try {
                double total = jsonObject.getDouble("total");
                return "£" + String.format(Locale.UK,"%1$.2f",total);
            } catch(JSONException e) {
                e.printStackTrace();
                return "£?.??";
            }
        }
        public String getDate() {
            try {
                String date = jsonObject.getString("date");
                return date;
            } catch (JSONException e) {
                e.printStackTrace();
                return "??/??/????";
            }
        }
        public String getIdentifier() {
            return identifier;
        }
        public JSONObject getJSONObject() {
            return jsonObject;
        }

    }
}
