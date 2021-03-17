package com.example.rescuemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;

public class SearchPlacesFragment extends Fragment {
    private AppSetup appSetup;

    private  EditText mSearchText;
    private FusedLocationProviderClient fusedLocationClient ;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_call, container, false);
        return view;

//       return inflater.inflate(R.layout.fragment_place,null);


    }
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//
//        view.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(),"You are inside Import Fragment",Toast.LENGTH_SHORT).show();
//            }
//        } );
//
//    }




}
