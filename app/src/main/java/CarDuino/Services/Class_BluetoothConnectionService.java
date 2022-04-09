package CarDuino.Services;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import CarDuino.Activity.Activity_CarDetail;

public class Class_BluetoothConnectionService
{
    private static final String TAG = "BluetoothConnectionSer";
    private static final String appName = "CarDuino";
    public static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final static int REQUEST_ENABLE_BT = 1; //<-- Default Value is 1
    public final static int REQUEST_ACCESS_COARSE_LOCATION = 1; //<-- Default Value is 1
    private final BluetoothAdapter bluetoothAdapter;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice bluetoothDevice;
    private ConnectedThread connectedThread;
    private UUID deviceUUID;
    private ProgressDialog progressDialog;

    Context context;

    public Class_BluetoothConnectionService(Context context)
    {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        start();
    }

    /**
     * This is the AcceptThread for Bluetooth Connection
     */
    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;
            try
            {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,uuid);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        @Override
        public void run()
        {
            super.run();
            BluetoothSocket socket = null;
            try
            {
                Log.d(TAG,"RUNNING: RFCOM Server Socket Start....");
                socket = serverSocket.accept();
                Log.d(TAG,"RUNNING: RFCOM Server Socket Accepted Connection....");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if(socket != null)
            {
                connected(socket,bluetoothDevice);
            }
        }
        public void cancel()
        {
            Log.d(TAG,"CANCEL: Cancelling AcceptThread");
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is the ConnectThread Class for Bluetooth Connection
     */
    private class ConnectThread extends Thread
    {
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device,UUID uuid)
        {
            Log.d(TAG,"ConnectedThread Started");
            bluetoothDevice = device;
            deviceUUID = uuid;
        }

        @Override
        public void run()
        {
            super.run();

            BluetoothSocket tmp = null;
            Log.i(TAG,"RUN ConnectThread");

            try
            {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            socket = tmp;
            bluetoothAdapter.cancelDiscovery();


            try
            {
                socket.connect();
                Log.d(TAG,"Connection Successful!");
            } catch (IOException e)
            {
                e.printStackTrace();
                try
                {
                    socket.close();
                    Log.d(TAG,"Socket Close");
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            connected(socket,bluetoothDevice);
        }

        public void cancel()
        {
            Log.d(TAG,"CANCEL: Cancelling AcceptThread");
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is the Connected Thread that handles the connection thread after device is connected
     */
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            progressDialog.dismiss();

            try
            {
                tempIn = mmSocket.getInputStream();
                tempOut = mmSocket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }


        @Override
        public void run()
        {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;

            while(true)
            {
                try
                {
                    bytes = inputStream.read(buffer);
                    String incommingMessage = new String(buffer,0,bytes);

                    switch (incommingMessage)
                    {
                        case "1":
                            Activity_CarDetail.SELECTED_DEVICE = true;
                            break;
                        default:
                            Activity_CarDetail.SELECTED_DEVICE = false;
                            break;
                    }
                    Log.d(TAG,"InputStream: "+incommingMessage);
                } catch (IOException e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }


        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"Writing to OutputStream"+text);

            try
            {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid)
    {
        progressDialog = ProgressDialog.show(context,"Connect Bluetooth","Connecting Bluetooth Device...",true);

        connectThread = new ConnectThread(device,uuid);
        connectThread.start();

    }

    private void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice)
    {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public synchronized void start()
    {
        Log.d(TAG,"Start!");

        //Cancel any thread that is trying to make a connection
        if(connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        if(acceptThread == null)
        {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void write(byte[] out)
    {

        connectedThread.write(out);

    }
    public void cancel()
    {

        connectedThread.cancel();

    }
}
