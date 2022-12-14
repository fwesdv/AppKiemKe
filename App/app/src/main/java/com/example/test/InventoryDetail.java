package com.example.test;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.test.Adapter.ListAssetAdapter;
import com.example.test.Adapter.ListKiemKeAdapter;
import com.example.test.Api.ApiService;
import com.example.test.Model.Asset;
import com.example.test.Model.Inventory;
import com.example.test.Model.InventoryDetailResData;
import com.example.test.Model.InventoryPutModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryDetail extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Asset> assetList;
    ListAssetAdapter adapter;
    Inventory inventory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thong_tin_ho_so);
        Intent intent = getIntent();
        String invenId = intent.getStringExtra("invenId");
        assetList=new ArrayList<>();
        recyclerView=(RecyclerView) findViewById(R.id.recyclerView2);

        RecyclerView.ItemDecoration decoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchData(invenId);
        Button back=findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SearchManager manager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView= (SearchView) findViewById(R.id.searchView2);
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        FloatingActionButton qrScan=findViewById(R.id.btnQRScan);
        qrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle=new Bundle();
                bundle.putSerializable("assetList", (Serializable) assetList);
                Intent intentGet=new Intent(InventoryDetail.this, ScannerActivity.class);
                intentGet.putExtras(bundle);
                startActivity(intentGet);
            }
        });
        Button btnComplete=findViewById(R.id.btnHoanThanh);
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryPutModel model=new InventoryPutModel(inventory,true);
                ApiService.apiService.editInven(inventory.getId(),model).enqueue(new Callback<InventoryDetailResData>() {
                    @Override
                    public void onResponse(Call<InventoryDetailResData> call, Response<InventoryDetailResData> response) {
                        if(response.isSuccessful())
                        {
                            Toast.makeText(InventoryDetail.this,response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(InventoryDetail.this, HomePage.class));
                        }
                        else{
//                            String res= response.errorBody().;
                            Toast.makeText(InventoryDetail.this,"???? c?? l???i x???y ra, c?? t??i s???n ch??a ki???m k?? ho???c h??? s?? ???? ho??n th??nh ki???m k??", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<InventoryDetailResData> call, Throwable t) {
                        Toast.makeText(InventoryDetail.this,"Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void fetchData(String invenId){
        ApiService.apiService.getInven(invenId).enqueue(new Callback<InventoryDetailResData>() {
            @Override
            public void onResponse(Call<InventoryDetailResData> call, Response<InventoryDetailResData> response) {
                Inventory res=response.body().getData();
                inventory=res;
                TextView invenId=findViewById(R.id.invenId);
                invenId.setText(res.getId());
                TextView date=findViewById(R.id.assetStatus);
                String dateCreate=new SimpleDateFormat("dd-MM-yyyy").format(res.getDate());
                date.setText(dateCreate);
                TextView baseId=findViewById(R.id.invenBaseId);
                baseId.setText(res.getInvenBase());
                TextView unitId=findViewById(R.id.invenUnitId);
                unitId.setText(res.getInvenUnit());

                assetList=res.getInventoryAssetsList();
                adapter=new ListAssetAdapter(assetList);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<InventoryDetailResData> call, Throwable t) {
                Toast.makeText(InventoryDetail.this,"Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}