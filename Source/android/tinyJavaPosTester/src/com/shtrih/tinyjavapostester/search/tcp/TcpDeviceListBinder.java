package com.shtrih.tinyjavapostester.search.tcp;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableArrayList;

public class TcpDeviceListBinder {

    @BindingAdapter("items")
    public static void bindList(ListView view, ObservableArrayList<TcpDevice> list) {
        TcpDeviceListAdapter adapter = new TcpDeviceListAdapter(list);
        view.setAdapter(adapter);
    }

    @BindingAdapter("onItemClicked")
    public static void bindSelectedItem(ListView view, final OnItemClickedListener listener) {

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TcpDevice vm = (TcpDevice) adapterView.getItemAtPosition(position);
                listener.onItemClicked(vm);
            }
        });
    }

    public interface OnItemClickedListener {

        void onItemClicked(TcpDevice item);
    }
}
