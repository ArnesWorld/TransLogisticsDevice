package com.example.arne.translogistics_device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.arne.translogistics_device.DAL.AppDataBase;
import com.example.arne.translogistics_device.Model.Package;


public class PackageDialogFragment extends DialogFragment {

    private AppDataBase database;
    private Button btnTest, btnOk, btnCancel;
    private EditText desc;
    private EditText qty;
    private EditText company;
    private EditText shipTo;
    private EditText shipFrom;
    private EditText currier;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_package_dialog, container, false);
        database = AppDataBase.getInstance(getContext());
        desc= view.findViewById(R.id.editTextDescription);
        qty= view.findViewById(R.id.editTextQty);
        company= view.findViewById(R.id.editTextCompany);
        shipTo = view.findViewById(R.id.editTextShipTo);
        shipFrom= view.findViewById(R.id.editTextShipFrom);
        currier= view.findViewById(R.id.editTextCurrier);
        btnTest = view.findViewById(R.id.btnTest);
        btnOk = view.findViewById(R.id.btnOk);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTestPackageData(view);

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRecordDataActivity();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });


        return view;
    }
/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        database = AppDataBase.getInstance(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_package_dialog,null);
        builder.setView(view);

        desc= view.findViewById(R.id.editTextDescription);
        qty= view.findViewById(R.id.editTextQty);
        company= view.findViewById(R.id.editTextCompany);
        shipTo = view.findViewById(R.id.editTextShipTo);
        shipFrom= view.findViewById(R.id.editTextShipFrom);
        currier= view.findViewById(R.id.editTextCurrier);
        btnTest = view.findViewById(R.id.btnTest);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTestPackageData(view);
            }
        });
        TextView id = view.findViewById(R.id.txtPackageId);
        int packId = database.packageModel().getLastId();
        id.setText(String.valueOf(packId));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchRecordDataActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });




        return builder.create();
    }
*/
    public void launchRecordDataActivity(){
        Intent intent = new Intent(getContext(), RecordDataActivity.class);
        String des = desc.getText().toString();
        int q = Integer.parseInt(qty.getText().toString());
        String comp = company.getText().toString();
        String sFrom = shipFrom.getText().toString();
        String sTo = shipTo.getText().toString();
        String curr = currier.getText().toString();

        Package pack = new Package(des,q,comp,sFrom,sTo,curr);
        int packID = (int)database.packageModel().insertPackage(pack);

        intent.putExtra("packageId", packID);
        startActivity(intent);
    }

    private void loadTestPackageData(View view) {


        desc.setText("Tomatoes");
                qty.setText("500");
                company.setText("GreenFoods A/S");
                shipFrom.setText("Malaga");
                shipTo.setText("Aalborg");
                currier.setText("Danske Fragtmænd A/S");

    }
}
