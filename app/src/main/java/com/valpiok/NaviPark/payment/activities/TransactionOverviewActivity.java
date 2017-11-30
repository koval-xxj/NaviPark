package com.valpiok.NaviPark.payment.activities;

/**
 * Created by SERHIO on 14.09.2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.valpiok.NaviPark.R;
import com.valpiok.NaviPark.payment.adapters.TransactionOverviewListAdapter;
import com.valpiok.NaviPark.payment.persistence.TransactionsDataSource;

public class TransactionOverviewActivity extends AppCompatActivity implements View.OnClickListener {

    private TransactionsDataSource transactionsDataSource;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_overview_empty);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Transactions overview");
        setSupportActionBar(toolbar);

        transactionsDataSource = new TransactionsDataSource(this);
        transactionsDataSource.open();

        if(transactionsDataSource.getAllTransactions().size() > 0) {
            setContentView(R.layout.activity_transaction_overview);
            mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new TransactionOverviewListAdapter(transactionsDataSource.getAllTransactions());
            mRecyclerView.setAdapter(mAdapter);

        }

        View createTransactionButton = findViewById(R.id.btn_create_transaction);
        createTransactionButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        transactionsDataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        transactionsDataSource.close();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TransactionActivity.class);
        startActivity(intent);
    }
}