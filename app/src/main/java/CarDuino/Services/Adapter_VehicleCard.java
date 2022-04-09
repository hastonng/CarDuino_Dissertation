package CarDuino.Services;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import CarDuino.Activity.Activity_CarDetail;
import CarDuino.R;

import java.util.List;

/**
 * Created by Haston Ng on 3 December 2018.
 */
public class Adapter_VehicleCard extends RecyclerView.Adapter<Adapter_VehicleCard.MyViewHolder>
{
    public static List<Class_Vehicle> vehicle;
    private Context context;
    public static String connection_status;

    /**
     * Description:
     * This Constructor for the Adapter RecyclerView
     *
     * Function:
     * This Constructor set the default value
     *
     * @param vehicle
     * @param context
     */
    public Adapter_VehicleCard(List<Class_Vehicle> vehicle, Context context)
    {
        this.vehicle = vehicle;
        this.context = context;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView car_list_name_id;
        public ImageView card_image_id;

        /**
         * Description:
         * This method/constructor will set the View of the RecyclerView
         *
         * Function:
         * This method/constructor will the action of the item in the RecyclerView
         *
         * @param view
         */
        public MyViewHolder(final View view)
        {
            super(view);
            car_list_name_id =(TextView) view.findViewById(R.id.car_list_name_id);
            card_image_id =(ImageView) view.findViewById(R.id.card_image_id);

            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    //Get the adapter position
                    int adapter_position = getAdapterPosition();
                    Log.e("Adapter Position",Integer.toString(adapter_position));
                    //Set intent value
                    Intent intent = new Intent(context, Activity_CarDetail.class);
                    intent.putExtra("position", adapter_position);
                    intent.putExtra("vehicle_id", vehicle.get(adapter_position).getVehicle_id());
                    intent.putExtra("vehicle_name",vehicle.get(adapter_position).getCar_name());
                    intent.putExtra("vehicle_model",vehicle.get(adapter_position).getCar_model());
                    intent.putExtra("vehicle_path",vehicle.get(adapter_position).getVehicle_path());
                    intent.putExtra("duino_key",vehicle.get(adapter_position).getDuinoKey());
                    intent.putExtra("address",vehicle.get(adapter_position).getBle_address());
                    intent.putExtra("conn_status",connection_status);
                    //Bring to the new Activity
                    context.startActivity(intent);

                }
            });
//            //Long Press
//            view.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v)
//                {
//
//                    return false;
//                }
//            });

        }
    }

    /**
     * Description:
     * This method will create the View in the RecyclerView
     *
     * Function:
     * This method will create the View in the RecyclerView using LayoutInflater
     *
     * @param parent
     * @param viewType
     * @return viewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_vehicle_card, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(itemView);
        return viewHolder;
    }

    /**
     * Description:
     * This method is to bind the data with the RecyclerView
     *
     * Function:
     * This method is to bind the data with the RecyclerView
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        //Get the vehicle data with the Vehicle List Array
        Class_Vehicle cd = vehicle.get(position);
        //Get the Vehicle Data
        holder.car_list_name_id.setText(cd.getCar_name());
        holder.card_image_id.setImageBitmap(BitmapFactory.decodeFile(cd.getVehicle_path()));
    }

    /**
     * Description:
     * The the length/count of Item in the RecyclerView
     *
     * Function:
     * The the length/count of Item in the RecyclerView
     *
     * @return int of itemCount
     */
    @Override
    public int getItemCount()
    {
        return vehicle.size();
    }

}