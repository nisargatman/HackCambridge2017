package com.example.matthew.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ReceiptActivity extends AppCompatActivity {

    public String receiptID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        receiptID = getIntent().getStringExtra("receiptID");


        getSupportActionBar().setTitle("Receipt " + receiptID);     // Change to vendor name (and date?)

        // Populate table
        TableLayout table = (TableLayout) findViewById(R.id.item_table);
        table.removeAllViews();
        for (int i=0; i<10; i++) {
            addReceiptRow(table,"milk", "£1", "groceries");
        }


    }

    public void addReceiptRow(TableLayout table, final String itemName, String itemPrice, String itemTag) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView item = new TextView(this);
        item.setText(itemName);
        item.setTextSize(20);
        TextView cost = new TextView(this);
        cost.setText(itemPrice);
        cost.setTextSize(20);
        TextView tag = new TextView(this);
        tag.setText(itemTag);
        tag.setTextSize(20);

        item.setPadding(10, 10, 30, 10);
        cost.setPadding(10, 10, 30, 10);
        tag.setPadding(10, 10, 30, 10);

        row.addView(item);
        row.addView(cost);
        row.addView(tag);
        table.addView(row);
    }
    }
