package com.example.arne.translogistics_device;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.arne.translogistics_device.Model.DataRecording;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arne on 29-03-2018.
 */

public class MyDataRecAdapter extends ArrayAdapter<DataRecording> {
    private ArrayList<DataRecording> dataSet;
    private int highLigtId;
    private DisplayRecordingsActivity context;
    public MyDataRecAdapter(@NonNull Context context, int resource, List<DataRecording> objects) {
        super(context, resource, objects);
        this.context = (DisplayRecordingsActivity) context;
        dataSet = (ArrayList<DataRecording>) objects;
    }

    public MyDataRecAdapter(@NonNull Context context, int resource, List<DataRecording> objects, int hightLigtId) {
        super(context, resource, objects);
        this.context = (DisplayRecordingsActivity) context;
        dataSet = (ArrayList<DataRecording>) objects;
        this.highLigtId = hightLigtId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       // return super.getView(position, convertView, parent);
        ViewHolder viewHolder;
        DataRecording dataRecording = getItem(position);
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.datarec_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(dataRecording.getId() == highLigtId){
            convertView.setBackgroundColor(Color.parseColor("#74a2ed"));
        }
        viewHolder.txtPackageId.setText(String.valueOf(dataRecording.pack.getId()));
        viewHolder.txtRecId.setText(String.valueOf(dataRecording.getId()));
        viewHolder.txtCompany.setText(dataRecording.pack.getCompany());
        viewHolder.setBtnViewDataListener(dataRecording.getId());
        viewHolder.setBtnSendDataListener(dataRecording);

        return convertView;
    }

    private class ViewHolder{
        TextView txtPackageId;
        TextView txtRecId;
        TextView txtCompany;
        Button btnViewData;
        Button btnSendData;

        public ViewHolder(View v){
            txtPackageId = v.findViewById(R.id.txtPackageId);
            txtRecId = v.findViewById(R.id.txtRecId);
            txtCompany = v.findViewById(R.id.txtCompany);
            btnSendData = v.findViewById(R.id.btnSendData);
            btnViewData = v.findViewById(R.id.btnViewData);


        }
        public void setBtnViewDataListener(final int recId){
            btnViewData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DisplayDataActivity.class);
                    intent.putExtra("recId", recId);
                    context.startActivity(intent);
                }
            });
        }
        public void setBtnSendDataListener(final DataRecording dataRecording){
            btnSendData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SendDataActivity.class);
                    intent.putExtra("datarecording", dataRecording);
                    context.startActivity(intent);
                }
            });
        }
    }
/*
    @Nullable
    @Override
    public DataRecording getItem(int position) {
        return dataSet.get(position);
    }
*/

}
