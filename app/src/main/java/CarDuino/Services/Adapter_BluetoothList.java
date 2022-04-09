package CarDuino.Services;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import CarDuino.R;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter_BluetoothList extends ArrayAdapter<BluetoothDevice>
{
    private LayoutInflater inflater;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private int  mViewResourceId;

    public Adapter_BluetoothList(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices)
    {
        super(context,tvResourceId,devices);
        this.bluetoothDevices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public BluetoothDevice getDevice(int position)
    {
        return bluetoothDevices.get(position);
    }

    public void clear() {
        bluetoothDevices.clear();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = inflater.inflate(mViewResourceId, null);

        BluetoothDevice device = bluetoothDevices.get(position);

        if (device != null)
        {
            TextView bluetooth_name_id = (TextView) convertView.findViewById(R.id.bluetooth_name_id);
            TextView bluetooth_address_id = (TextView) convertView.findViewById(R.id.bluetooth_address_id);

            if(bluetooth_name_id != null)
            {
                //Set Bluetooth Name on the Array List
                bluetooth_name_id.setText(device.getName());
            }

            if(bluetooth_address_id != null)
            {
                bluetooth_address_id.setText(device.getAddress());
            }
        }

        return convertView;
    }

}
