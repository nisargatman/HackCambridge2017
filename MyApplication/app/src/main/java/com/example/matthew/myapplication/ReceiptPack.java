package com.example.matthew.myapplication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
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
            for (; receiptKeys.hasNext(); ) {
                String thisKey = receiptKeys.next();
                Log.d(TAG, "Trying the key: " + thisKey);
                try {
                    receipts.add(new Receipt(jsonReceipts.getJSONObject(thisKey)));
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
        return jsonObject.toString();
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
        private boolean isValid;

        public Receipt(JSONObject jsonReceipt) throws ReceiptException{
            jsonObject = jsonReceipt;
            validateJson();
        }
        public Receipt(String jsonReceipt) throws ReceiptException {
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

    }
}
