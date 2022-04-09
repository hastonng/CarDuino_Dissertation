package CarDuino.Services;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import CarDuino.Activity.Activity_CarDetail;
import CarDuino.Activity.Activity_Main;

public class Class_BluetoothLeService extends Service
{

    private final static String TAG = Class_BluetoothLeService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private int connectionState = STATE_DISCONNECTED;
    public static boolean AUTO_LOCK_INDICATOR = true;
    public static Timer rssiTimer;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_CAR_DUINO =
            UUID.fromString(Class_GattAttributes.CAR_DUINO_UUID);

    //0000ffe0-0000-1000-8000-00805f9b34fb <-- Service
    //0000ffe1-0000-1000-8000-00805f9b34fb <-- Characteristic

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());
                Activity_Main.bluetoothGatt = gatt;
                Class_BLE_BroadcastReceiver.bluetoothDevice = gatt.getDevice();


//                if(AUTO_LOCK_INDICATOR == true)
//                {
//                    TimerTask readRssi = new TimerTask()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            if(bluetoothGatt != null)
//                            {
//                                bluetoothGatt.readRemoteRssi();
//                            }
//
//                        }
//                    };
//                    Timer startTask = new Timer();
//                    startTask.schedule(readRssi,1000,1000);
//                }

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                Activity_Main.bluetoothGatt = null;
                Class_BLE_BroadcastReceiver.bluetoothDevice = null;


                gatt.disconnect();
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            super.onDescriptorWrite(gatt, descriptor, status);

//            writeCharacteristic = gatt.getService(UUID_CAR_DUINO).getCharacteristic(UUID_CAR_DUINO);
//            String key = SaveSharedPreference.getPREF_PREVIOUS_DEVICE(context);
//            byte[] value = key.getBytes();
//            writeCharacteristic.setValue(value);
//            gatt.writeCharacteristic(writeCharacteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            super.onDescriptorRead(gatt, descriptor, status);
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                //If RSSI of BLE device is out of range (-65), send close
                if (rssi <= -65)
                {
                    Log.d(TAG,"This is the RSSI:"+rssi);
                    Log.d(TAG,"You are leaving!");

                    String v1 = "0";
                    byte[] bytes = v1.getBytes(Charset.defaultCharset());
                    writeCharacteristic = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID_CAR_DUINO);
                    writeCharacteristic.setValue(bytes);
                    writeCharacteristic(writeCharacteristic);

                    readCharacteristic = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID_CAR_DUINO);
                    readCharacteristic(readCharacteristic);
                    setCharacteristicNotification(readCharacteristic,true);


                }
                //If RSSI of BLE device is within of range (-65), send open
                if(rssi > -65)
                {
                    Log.d(TAG,"This is the RSSI:"+rssi);
                    Log.d(TAG,"You are here!");

                    String v1 = "1";
                    byte[] bytes = v1.getBytes(Charset.defaultCharset());
                    writeCharacteristic = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID_CAR_DUINO);
                    writeCharacteristic.setValue(bytes);
                    writeCharacteristic(writeCharacteristic);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {

//                if(Arrays.toString(readCharacteristic.getValue()) == "N" )
//                {
//                    rssiTimer.cancel();
//                }
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_CAR_DUINO.equals(characteristic.getUuid()))
        {
            if (((characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) |
                    (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)
            {
                writeCharacteristic = characteristic;
            }

            if(((characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) |
            (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
            {

                readCharacteristic = characteristic;
                Log.d(TAG,characteristic.getStringValue(0)+"HERE");

            }

        }

        Log.w(TAG, "broadcastUpdate()");

        final byte[] data = characteristic.getValue();

        Log.v(TAG, "data.length: " + data.length);

        if (data != null && data.length > 0)
        {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
            {
                stringBuilder.append(String.format("%02X ", byteChar));

                Log.v(TAG, String.format("%02X ", byteChar));
            }
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public void setTimerTask(boolean autoNot)
    {
        if(autoNot == true)
        {
            rssiTimer = new Timer();
        }
        else
        {
            rssiTimer = null;
        }
    }

    public class LocalBinder extends Binder
    {
        public Class_BluetoothLeService getService() {
            return Class_BluetoothLeService.this;
        }
    }

    public int getConnectionState()
    {
        return connectionState;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
//        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.

        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }




    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public BluetoothGatt connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return null;
        }

        // Previously connected device.  Try to reconnect.
        if (bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress)
                && bluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (bluetoothGatt.connect())
            {
                connectionState = STATE_CONNECTING;
                return bluetoothGatt;
            } else {
                return null;
            }
        }


        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return null;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to true.
        bluetoothGatt = device.connectGatt(this, true, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return bluetoothGatt;
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect()
    {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null)
        {
            Log.w(TAG,"BluetoothAdapter not initialized");
            return;
        }


        bluetoothGatt.writeCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if (bluetoothAdapter == null || bluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Car Duino UUID.
        if (UUID_CAR_DUINO.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(Class_GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }

    }


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;

        return bluetoothGatt.getServices();
    }


}
