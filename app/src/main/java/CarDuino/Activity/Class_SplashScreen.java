package CarDuino.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;

import CarDuino.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Class_SplashScreen extends AppCompatActivity
{

    private static int SPLASH_TIME_OUT = 1000;
    private LottieAnimationView material_wave_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.view_splash_screen);

        material_wave_id = (LottieAnimationView) findViewById(R.id.material_wave_id);
        material_wave_id.playAnimation();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {


                Intent intent = new Intent (Class_SplashScreen.this,Activity_Main.class);
                startActivity(intent);
                finish();

            }
        },SPLASH_TIME_OUT);
    }
}
