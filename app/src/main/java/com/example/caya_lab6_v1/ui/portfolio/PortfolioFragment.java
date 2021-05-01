package com.example.caya_lab6_v1.ui.portfolio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.caya_lab6_v1.R;

public class PortfolioFragment extends Fragment {

    private PortfolioViewModel portfolioViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        portfolioViewModel =
                new ViewModelProvider(this).get(PortfolioViewModel.class);
        View root = inflater.inflate(R.layout.fragment_portfolio, container, false);





        return root;
    }
}