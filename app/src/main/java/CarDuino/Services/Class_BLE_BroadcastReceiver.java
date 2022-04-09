package CarDuino.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Class_BLE_BroadcastReceiver extends BroadcastReceiver
{
    Context activityContext;
    static BluetoothDevice bluetoothDevice;

    public Class_BLE_BroadcastReceiver(Context ctx)
    {
        this.activityContext = ctx;
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
        {
            final int state  = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
            switch (state)
            {
                case BluetoothAdapter.STATE_OFF:
                    break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                        case BluetoothAdapter.STATE_ON:
                            break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                break;
            }
        }
//        else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
//        {
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            if(device.getBondState() == BluetoothDevice.BOND_BONDED)
//            {
//                Toast.makeText(activityContext, "Paired", Toast.LENGTH_LONG).show();
//            }
//            if(device.getBondState() == BluetoothDevice.BOND_BONDING)
//            {
//                Toast.makeText(activityContext, "Pairing", Toast.LENGTH_LONG).show();
//            }
//            if(device.getBondState() == BluetoothDevice.BOND_NONE)
//            {
//                Toast.makeText(activityContext, "Not Paired", Toast.LENGTH_LONG).show();
//            }
//        }
//        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
//        {
//            Toast.makeText(activityContext, "Connected", Toast.LENGTH_LONG).show();
//            Adapter_VehicleCard.connection_status = "Connected";
//            SaveSharedPreference.setPREF_PREVIOUS_DEVICE(activityContext,bluetoothDevice.getAddress());
//        }
//        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
//        {
//            Toast.makeText(activityContext, "Disconnected", Toast.LENGTH_LONG).show();
//            Adapter_VehicleCard.connection_status = "Disconnected";
//        }
//        else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
//        {
//            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//
//            switch(state)
//            {
//                case BluetoothAdapter.STATE_OFF:
//                    Adapter_VehicleCard.connection_status = "Not Available";
//                    Toast.makeText(activityContext, "Bluetooth is Off", Toast.LENGTH_LONG).show();
//                    break;
//
//                case BluetoothAdapter.STATE_ON:
//                    Adapter_VehicleCard.connection_status = "Not Connected";
//                    Toast.makeText(activityContext, "Bluetooth is On", Toast.LENGTH_LONG).show();
//                    break;
//
//                case BluetoothAdapter.STATE_TURNING_OFF:
//                    Adapter_VehicleCard.connection_status = "Turning off Bluetooth...";
//                    Toast.makeText(activityContext, "Turning off Bluetooth...", Toast.LENGTH_LONG).show();
//                    break;
//                case BluetoothAdapter.STATE_TURNING_ON:
//                    Adapter_VehicleCard.connection_status = "Turning on Bluetooth...";
//                    Toast.makeText(activityContext, "Turning on Bluetooth...", Toast.LENGTH_LONG).show();
//                    break;
//            }
//
//        }

    }
}
