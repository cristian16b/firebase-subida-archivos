package com.example.firebase_subida_archivos;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class AtomPayListAdapter  extends BaseAdapter {

    private List<miFile> items;
    private Context context;

    public AtomPayListAdapter(MainActivity mainActivity, List<miFile> items) {
//        super(context, layoutResourceId, items);
        this.context = mainActivity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        AtomPaymentHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(R.layout.atom_pay_list_item,parent, false);

        holder = new AtomPaymentHolder();
        holder.atomPayment = items.get(position);
        holder.removePaymentButton = (ImageButton)row.findViewById(R.id.atomPay_removePay);
        holder.removePaymentButton.setTag(holder.atomPayment);

        holder.name = (TextView)row.findViewById(R.id.atomPay_name);
        holder.value = (TextView)row.findViewById(R.id.atomPay_value);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(AtomPaymentHolder holder) {
        holder.name.setText(holder.atomPayment.toStringItem());
        holder.value.setText(String.valueOf(holder.atomPayment.toStringItem()));
    }

    public void remove(miFile itemToRemove) {
//        todo
    }

    public static class AtomPaymentHolder {
        miFile atomPayment;
        TextView name;
        TextView value;
        ImageButton removePaymentButton;
    }
}