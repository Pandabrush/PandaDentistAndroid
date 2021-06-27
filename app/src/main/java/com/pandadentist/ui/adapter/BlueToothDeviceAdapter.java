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
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by fudaye on 2017/8/21.
 * Updated by zhangwy on 2017/12/02.
 */

@SuppressWarnings("ConstantConditions")
public class BlueToothDeviceAdapter extends RecyclerView.Adapter<BlueToothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private HashMap<String, Boolean> connected = new HashMap<>();
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

        if (connected.get(device.getAddress())) {
            holder.req.setVisibility(View.VISIBLE);
            holder.setting.setVisibility(View.VISIBLE);
            holder.req.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, device, holder.getAdapterPosition(), 1);
                }
            });
            holder.btn.setText("断开连接");
            holder.btn.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, device, holder.getAdapterPosition(), 2);
                }
            });
            holder.setting.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, device, holder.getAdapterPosition(), 4);
                }
            });
        } else {
            holder.req.setVisibility(View.GONE);
            holder.setting.setVisibility(View.GONE);
            holder.req.setOnClickListener(null);
            holder.setting.setOnClickListener(null);
            holder.btn.setText(R.string.addToothBrush);
            holder.btn.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, device, holder.getAdapterPosition(), 3);
                }
            });
        }
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
        @Bind(R.id.btn_reqData)
        Button req;
        @Bind(R.id.btn_setting)
        Button setting;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void put(BluetoothDevice device) {
        this.devices.add(device);
        this.connected.put(device.getAddress(), false);
        this.notifyDataSetChanged();
    }

    public void connected(String deviceId, boolean connected) {
        this.connected.put(deviceId, connected);
        this.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<BluetoothDevice> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
