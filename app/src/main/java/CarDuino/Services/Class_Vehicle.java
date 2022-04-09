package CarDuino.Services;

import android.net.Uri;

/**
 * Description:
 * This class store the Vehicle Information
 *
 * Store:
 * car_name
 * car_model
 * vehicle_path
 * vehicle_id
 * selected_image
 * RESULT_LOAD_IMAGE <- Default Value
 * STORAGE_PERMISSION_CODE <- Default Value
 */
public class Class_Vehicle
{
    private String car_name;
    private String car_model ;
    private String vehicle_path;
    private String duinoKey;
    private int vehicle_id;
    private String ble_address;

    private static Uri selected_image;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int PERMISSION_CODE = 1;



    //Constructors
    public Class_Vehicle(String car_name , String car_model, String vehicle_path, int vehicle_id, String duinoKey, String address)
    {
        this.car_name = car_name;
        this.car_model = car_model;
        this.vehicle_path = vehicle_path;
        this.vehicle_id = vehicle_id;
        this.duinoKey = duinoKey;
        this.ble_address = address;
    }

    public int getVehicle_id() {
        return vehicle_id;
    }
    public String getCar_name() {
        return car_name;
    }
    public String getCar_model() {
        return car_model;
    }
    public String getDuinoKey() {
        return duinoKey;
    }
    public String getVehicle_path() {
        return vehicle_path;
    }
    public String getBle_address() {return ble_address; }

    public static Uri getSelected_image() {
        return selected_image;
    }

    public static void setSelected_image(Uri selected_image) {
        Class_Vehicle.selected_image = selected_image;
    }

    public static int getResultLoadImage() {
        return RESULT_LOAD_IMAGE;
    }

    public static int getPermissionCode() {
        return PERMISSION_CODE;
    }
}
