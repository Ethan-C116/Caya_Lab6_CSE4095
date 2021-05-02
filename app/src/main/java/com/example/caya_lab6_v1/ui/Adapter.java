package com.example.caya_lab6_v1.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caya_lab6_v1.Data.Stock;
import com.example.caya_lab6_v1.R;
import com.example.caya_lab6_v1.ui.dashboard.DashboardViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {
    private static final String TAG = "ADAPTER";
    private List<Stock> stockList;
    private List<Stock> allStocks;
    private View view;
    private Integer IDToDelete;
    private Stock stockToDelete;
    private Fragment fragment;

    public Adapter(List<Stock> stocks, View root, Fragment fragment){
        stockList = stocks;
        //create copy of all stocks so we can filter later
        allStocks = new ArrayList<>(stocks);
        view = root;
        this.fragment = fragment;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //views from ListItem to hold
        private TextView stockNameTV;
        private TextView stockSymbolTV;
        private TextView stockPriceTV;
        private ImageButton saveButton;
        private ImageButton deleteButton;
        private ImageButton editButton;

        //constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //set the views
            stockNameTV = itemView.findViewById(R.id.stockNameTV);
            stockSymbolTV = itemView.findViewById(R.id.stockSymbolTV);
            stockPriceTV = itemView.findViewById(R.id.priceTV);
            saveButton = itemView.findViewById(R.id.saveButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }

    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View stockView = inflater.inflate(R.layout.list_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(stockView);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        Stock thisStock = stockList.get(position);

        //fill out our View layout with info from our ViewHolder
        TextView stockNameTV = holder.stockNameTV;
        TextView stockSymbolTV = holder.stockSymbolTV;
        TextView priceTV = holder.stockPriceTV;
        ImageButton deleteButton = holder.deleteButton;
        ImageButton saveButton = holder.saveButton;
        ImageButton editButton = holder.editButton;

        if(thisStock.isSaved) {
            saveButton.setColorFilter(R.color.yellow, PorterDuff.Mode.SRC_ATOP);
            //saveButton.setBackgroundColor(Color.YELLOW);
        }
        else{
            //saveButton.setColorFilter(R.color.white, PorterDuff.Mode.SRC_IN);
            saveButton.clearColorFilter();
        }

        stockNameTV.setText(thisStock.getName());
        stockSymbolTV.setText(thisStock.getSymbol());
        String price = "$ " + String.valueOf(thisStock.getPrice());
        priceTV.setText(price);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "delete button pressed for " + thisStock.getSymbol());
                deleteStock(thisStock.getId());
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "edit button pressed for " + thisStock.getSymbol());
                DashboardViewModel.setCurrentStock(thisStock);
                NavHostFragment.findNavController(fragment).
                        navigate(R.id.action_navigation_dashboard_to_editStockFragment);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "save button pressed for " + thisStock.getSymbol());
                if(!thisStock.isSaved) {
                    saveButton.setColorFilter(R.color.yellow, PorterDuff.Mode.SRC_ATOP);
                    //saveButton.setBackgroundColor(Color.YELLOW);
                }
                else{
                    //saveButton.setColorFilter(R.color.white, PorterDuff.Mode.SRC_IN);
                    saveButton.clearColorFilter();
                }
                thisStock.toggleSave();
                //update stock in DB
                Observable<Stock> updateObservable = io.reactivex.Observable.just(thisStock);

                Observer<Stock> updateObserver = new Observer<Stock>() {
                    @Override

                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Stock stock) {
                        //delete the stock
                        Log.d(TAG, "onNext: deleted stock with ID: " + stock.getId());
                        DashboardViewModel.getStockDatabase().stockDAO().update(thisStock);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                };

                updateObservable.observeOn(Schedulers.io()).subscribe(updateObserver);

            }
        });

    }

    @Override
    public int getItemCount() {
        if(stockList == null){
            return 0;
        }
        else {
            return stockList.toArray().length;
        }
    }

    public void stockAdded(List<Stock> stocks){
        int position = this.stockList.toArray().length;
        this.stockList = stocks;
        this.allStocks = stocks;
        notifyDataSetChanged();
    }

    public void updateAllStocks(List<Stock> stocks){
        this.stockList = stocks;
        notifyDataSetChanged();
    }

    public void restoreAllStocks(){
        this.stockList = this.allStocks;
        notifyDataSetChanged();
    }


    public void deleteStock(int stockID) {
        //find stock by ID
        //pass adapter instance to observable to get data out
        Observable<Adapter> IDObservable = io.reactivex.Observable.just(this);
        this.IDToDelete = stockID;

        Observer<Adapter> IDObserver = new Observer<Adapter>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Adapter adapter) {
                adapter.stockToDelete = DashboardViewModel.
                        getStockDatabase()
                        .stockDAO()
                        .getStockByID(adapter.IDToDelete);
                removeStock();
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };

        IDObservable.observeOn(Schedulers.io()).subscribe(IDObserver);
    }

    private void removeStock(){
        //remove from recyclerview
        Log.d(TAG, "deleteStock: " + this.stockToDelete.getName());
        //have to update views on UI thread
        /*
        stockList.remove(stockToDelete);
        allStocks.remove(stockToDelete);
        view.post(new Runnable() {
            @Override
            public void run() {

                //notifyDataSetChanged();
            }
        });
        */

        //remove from database
        Log.d(TAG, "deleteStock: saw stock" + stockToDelete.getName() + " " + stockToDelete.getSymbol());
        Observable<Stock> deleteObservable = io.reactivex.Observable.just(stockToDelete);

        Observer<Stock> deleteObserver = new Observer<Stock>() {
            @Override

            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Stock stock) {
                //delete the stock
                Log.d(TAG, "onNext: deleted stock with ID: " + stock.getId());
                DashboardViewModel.getStockDatabase().stockDAO().delete(stockToDelete);
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };

        deleteObservable.observeOn(Schedulers.io()).subscribe(deleteObserver);

        //show delete message
        Snackbar.make(view,
                "Stock successfully deleted",
                Snackbar.LENGTH_SHORT);
    }

    @Override
    public Filter getFilter() {
        return stockFilter;
    }

    private Filter stockFilter = new Filter() {

        //filters on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Stock> filterResults = new ArrayList<>();
            //search using user input
            //if search empty show all items
            if(constraint == null || constraint.length() ==0){
                filterResults.addAll(allStocks);
            }
            else{
                //if name or symbol contains search add to results
                String filter = constraint.toString().toLowerCase().trim();
                for(Stock stock : allStocks){
                    if(stock.getName().toLowerCase().contains(filter) ||
                            stock.getSymbol().toLowerCase().contains(filter)){
                        filterResults.add(stock);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterResults;

            return results;
        }

        //publish to UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            updateAllStocks((List<Stock>) results.values);
        }
    };


}
