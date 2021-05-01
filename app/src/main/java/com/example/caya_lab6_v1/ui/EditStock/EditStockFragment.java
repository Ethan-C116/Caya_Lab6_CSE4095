package com.example.caya_lab6_v1.ui.EditStock;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toolbar;

import com.example.caya_lab6_v1.Data.Stock;
import com.example.caya_lab6_v1.R;
import com.example.caya_lab6_v1.ui.dashboard.PortfolioViewModel;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EditStockFragment extends Fragment {
    private static final String TAG = "EDIT_STOCK_FRAG";
    private Stock stock;
    private EditText nameET;
    private EditText symbolET;
    private EditText priceET;
    private Button saveButton;
    private ImageButton cancelButton;
    private NavController navController;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_edit_stock, container, false);


        //get the current stock
        this.stock = PortfolioViewModel.getCurrentStock();
        this.context = getContext();

        //set up EditTexts
        nameET = root.findViewById(R.id.editStockNameET);
        nameET.setText(stock.getName());
        symbolET = root.findViewById(R.id.editStockSymbolET);
        symbolET.setText(stock.getSymbol());
        priceET = root.findViewById(R.id.editStockPriceET);
        priceET.setText(String.valueOf(stock.getPrice()));

        //set up Buttons
        saveButton = root.findViewById(R.id.editStockSaveButton);
        cancelButton = root.findViewById(R.id.editStockCancelButton);

        //button onClicks
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "save button pressed");
                //update stock instance
                String symbol = symbolET.getText().toString().trim();
                String name = nameET.getText().toString().trim();
                String priceString = priceET.getText().toString();
                if(name.equals("") || symbol.equals("") || priceString.equals("")){
                    Snackbar.make(v,
                            "ERROR: Stock must have all fields",
                            Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Double price = Double.parseDouble(priceString);
                    stock.setName(name);
                    stock.setSymbol(symbol);
                    stock.setPrice(price);
                    Log.d(TAG, "onClick: new stock " + name + " " + symbol + " " + price);

                    //save to database
                    Observable<Stock> updateObservable = io.reactivex.Observable.just(stock);
                    Observer<Stock> updateObserver = new Observer<Stock>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                        }

                        @Override
                        public void onNext(@NonNull Stock stock) {
                            Log.d(TAG, "onNext: ");
                            PortfolioViewModel.getStockDatabase().stockDAO().update(stock);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, "onError: " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                        }
                    };
                    updateObservable.observeOn(Schedulers.io()).subscribe(updateObserver);

                    //go back to dashboard
                    navController.navigate(R.id.action_editStockFragment_to_navigation_dashboard);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate back to dashboard
                navController.navigate(R.id.action_editStockFragment_to_navigation_dashboard);
            }
        });


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}