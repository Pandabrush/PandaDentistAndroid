package com.pandadentist.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.listener.OnItemClickListener;
import com.pandadentist.bleconnection.utils.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by fudaye on 2017/8/21.
 * Updated by zhangwy on 2017/12/02.
 */

public class BlueToothDeviceAdapter extends RecyclerView.Adapter<BlueToothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private OnItemClickListener<BluetoothDevice> onItemClickListener;

    public BlueToothDeviceAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_blue_tooth_device, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BluetoothDevice device = devices.get(position);
        holder.tv.setText(device.getName());
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, device, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv)
        TextView tv;
        @Bind(R.id.btn_add)
        Button btn;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void replace(Collection<BluetoothDevice> collection) {
        if (Util.isEmpty(collection)) {
            return;
        }
        this.devices.clear();
        this.devices.addAll(collection);
        this.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<BluetoothDevice> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
