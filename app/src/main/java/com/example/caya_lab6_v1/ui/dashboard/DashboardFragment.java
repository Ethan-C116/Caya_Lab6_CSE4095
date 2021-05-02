package com.example.caya_lab6_v1.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caya_lab6_v1.Data.Stock;
import com.example.caya_lab6_v1.R;
import com.example.caya_lab6_v1.ui.Adapter;
import com.example.caya_lab6_v1.ui.DataOperation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DASHBOARD_FRAG";
    private DashboardViewModel dashboardViewModel;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private FloatingActionButton addStockFAB;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private DataOperation dataOperation;
    private int stockID;
    private static List<Stock> stockList = new ArrayList<>();
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //allow option menu for search
        setHasOptionsMenu(true);
        navController =  NavHostFragment.findNavController(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
        //set up search in the toolbar
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("Search stock name or symbol");
        //expand and get focus
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        //https://stackoverflow.com/a/12975254
        //searchView.setOnCloseListener doesn't work
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand: menu collapsed");
                //when search collapses put everything back in recyclerView
                adapter.restoreAllStocks();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //add observer to live data stock list
        DashboardViewModel.getStockList().observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<Stock>>() {
            @Override
            public void onChanged(List<Stock> stocks) {
                Log.d(TAG, "onChanged: stock list changed");
                //Log.d(TAG, "stockList: " + stocks.toString());
                DashboardFragment.stockList = stocks;
                adapter.stockAdded(stockList);
            }
        });

        //set up RecyclerView
        recyclerView = root.findViewById(R.id.dashboardRecyclerView);
        adapter = new Adapter(stockList, root, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        //set up add stock button
        addStockFAB = root.findViewById(R.id.addStockFAB);
        addStockFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: addStation button");
                //trigger popup dialog
                newStockDialog();
            }
        });

        return root;
    }

    private void newStockDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        //inflate the popup
        View addStockView = getLayoutInflater().inflate(R.layout.add_stock_popup, null);

        //set up any views on the popup
        EditText newStockName = addStockView.findViewById(R.id.nameET);
        EditText newStockSymbol = addStockView.findViewById(R.id.symbolET);
        EditText newStockPrice = addStockView.findViewById(R.id.priceET);
        Button submitButton = addStockView.findViewById(R.id.submitButton);
        ImageButton cancelButton = addStockView.findViewById(R.id.cancelButton);

        dialogBuilder.setView(addStockView);
        dialog = dialogBuilder.create();
        dialog.show();

        submitButton.setOnClickListener(new View.OnClickListener() {
            double price;
            String name;
            String symbol;
            String priceString;
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: new stock submitted");
                name = newStockName.getText().toString().trim();
                symbol = newStockSymbol.getText().toString().trim();
                priceString = newStockPrice.getText().toString();
                //if not all info given display error
                if(name.equals("") || symbol.equals("") || priceString.equals("")){
                    Snackbar.make(getView(),
                            "ERROR: Stock must have all fields",
                            Snackbar.LENGTH_SHORT).show();
                }
                else {
                    price = Double.parseDouble(newStockPrice.getText().toString());
                    dataOperation = DataOperation.INSERT;
                    Stock stock = new Stock(symbol, name, price);

                    Observable<Stock> observable = io.reactivex.Observable.just(stock);

                    //create observer to process DB action on another thread
                    Observer<Stock> observer = getStockObserver(stock);
                    //subscribe observer to observable
                    observable.observeOn(Schedulers.io()).subscribe(observer);

                    dialog.dismiss();
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: new stock cancel");
                dialog.dismiss();
            }
        });
    }

    private Observer<Stock> getStockObserver(Stock stock) { // OBSERVER
        return new Observer<Stock>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Stock stock) {
                switch(dataOperation) {
                    case INSERT:
                        //check if stock exists
                        List<Stock> results = DashboardViewModel.getStockDatabase().stockDAO().getStockBySymbol(stock.getSymbol());
                        if(results.toArray().length != 0){
                            Log.d(TAG, "Duplicate stock found when inserting");
                            showSnackbar("ERROR: Stock with that symbol already exists");
                        }
                        else{
                            DashboardViewModel.getStockDatabase().stockDAO().insert(stock);
                            showSnackbar("New stock added: " + stock.getName());
                        }
                        break;
                    case DELETE:
                        if (-1 == stockID) {
                            Log.e(TAG, "Don't delete a non-existent stock");
                            showSnackbar("Stock doesn't exist");
                        } else {
                            DashboardViewModel.getStockDatabase().stockDAO().delete(stock);
                            showSnackbar("Stock successfully deleted");
                        }
                        break;
                    case GET_STOCK:
                        Stock actualStock = DashboardViewModel.getStockDatabase().stockDAO().getStock(stock.getName()).get(0);
                        if (null == actualStock){
                            Log.e(TAG, "No stock found! " );
                            showSnackbar("ERROR: No stock found");
                            // handle error!
                            stockID = -1;
                        } else {
                            Log.i(TAG, "Get stock ID: " + String.valueOf(actualStock.getId()).toString());
                            stockID = actualStock.getId();
                        }
                        break;
                    case UPDATE:
                        Log.i(TAG, "Delete");
                        break;
                    default:
                        Log.i(TAG, "Default");
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All items are emitted!");
            }
        };
    }

    //public so Adapter can use it
    public void showSnackbar(String msg){
        Snackbar.make(
                getView(),
                msg,
                Snackbar.LENGTH_SHORT).show();
    }
}