package CarDuino.Services;

/**
 * Description:
 * This is the Connection URL for the JSON RESTFUL API to Database
 *
 * *************-NOTE-*************: VERY IMPORTANT!!!!
 *  Please ensure the IP Address is changed to the IP Address of your Database Connection
 *  before running the Application. Follow the steps below to ensure you have connection to the
 *  Database:
 *
 *  1) Go to Start > Command Prompt
 *  2) In the Command Prompt, type "ipconfig" to get connection details
 *  3) Copy the "IPv4" of your machine and paste it in the usersUrl and devicesUrl below
 *
 *      For example:
 *      if your IPv4 is:                            192.168.0.110
 *
 *      your usersUrl and devicesUrl will be:       http://192.168.0.110/CarDuinoApi/users
 *                                                  http://192.168.0.110/CarDuinoApi/devices
 *
 *  4) If you are running on the Emulator (Not Recommended), you can set usersUrl and devicesUrl:
 *
 *      http://localhost/CarDuinoApi/users
 *      http://localhost/CarDuinoApi/devices
 *
 *      However, running on Emulator is not recommended for certain features Eg: Bluetooth is not
 *      available on Emulator
 *
 *  5) IMPORTANT !!!!!!
 *      If you are running on your android mobile device, please ensure your mobile device and
 *      Database is connected to the same Network (Wi-Fi or Mobile Data/Hotspot).
 *
 *  6) This configuration only requires when:
 *      1) Your first time Running this App
 *      2) Each time you restart your Database Machine, because your IP Address will change
 *          after you restart your Machine
 *
 *  7) This issue will not occur if we purchase a public IP for your Database Server or owning
 *      a private Network
 */
public class Connection
{
    private String usersUrl = "http://192.168.0.101/CarDuinoApi/users";
    private String devicesUrl = "http://192.168.0.101/CarDuinoApi/devices";

    public String getUsersUrl() {
        return usersUrl;
    }

    public String getDevicesUrl() {
        return devicesUrl;
    }
}
