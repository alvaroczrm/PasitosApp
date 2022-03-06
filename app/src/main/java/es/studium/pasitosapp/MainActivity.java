package es.studium.pasitosapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    GoogleMap mapa;
    private TextView txtCoordenadas;
    int batLevel=0;
    private TextView BatLvl;
    private LocationManager locManager;
    private Location loc;
    Double Longitud=-5.933873333333334;
    Double Latitud=37.2963866666666;
    AyudanteBaseDeDatos ayudanteBaseDeDatos = new AyudanteBaseDeDatos(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //desactiva la rotacion
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //finds layout
        txtCoordenadas= findViewById(R.id.txtCoordenadasValue);
        BatLvl = findViewById(R.id.BatLvl);
        //Obtenemos el mapa de forma asincrona (notificará cuando este listo)
        SupportMapFragment mapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        //Lanza hilo
       // new Thread(new Task()).start();
        hilo();
        //GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        else
        {
            locationStart();
        }

       Bateria();
}
//HILO
   /*
    class Task implements Runnable{
    @Override
    public void run() {
        Looper.prepare();
        for (int i = 0; i <= 100; i++){
            Bateria();
            new Localizacion(); //hilo no puede interactuar con la view
            if (i>2){
                marcadores(mapa);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
*/
public void hilo(){
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable(){
        @Override
        public void run() {
            locationStart();
            Bateria();
            marcadores(mapa);
            sql();
            handler.postDelayed(this, 10000);
        }
    }, 10000);
}
//GPS
    private void locationStart() {
        LocationManager mlocManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled)
        {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener)
                Local);
        txtCoordenadas.setText("Localización agregada");


    }
    public class Localizacion implements LocationListener
    {
        MainActivity mainActivity;
        public MainActivity getMainActivity()
        {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity)
        {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc)
        {
            // Este método se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la detección de un cambio de ubicación
            loc.getLatitude();
            loc.getLongitude();
            String Text = "Mi ubicación actual es: " + "\n Latitud = "
                    + loc.getLatitude() + "\n Longitud = " + loc.getLongitude();
            Latitud=loc.getLatitude();
            Longitud=loc.getLongitude();
            txtCoordenadas.setText("Longitud "+"\n"+Longitud.toString()+"\n Latitud "+"\n" + Latitud.toString());

            this.mainActivity.setLocation(loc);
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            // Este método se ejecuta cuando el GPS es desactivado
            txtCoordenadas.setText("GPS Desactivado");
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            // Este método se ejecuta cuando el GPS es activado
            txtCoordenadas.setText("GPS Activado");
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            switch (status)
            {
                case 0:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case 1:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
                case 2:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
            }
        }
    }
    public void setLocation(Location loc)
    {
        //Obtener la dirección de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0)
        {
            try
            {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty())
                {
                    Address DirCalle = list.get(0);

                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
//GOOGLE MAPS
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        LatLng myUbi = new LatLng(Latitud, Longitud);// ubicacion acutal
        mapa.moveCamera(CameraUpdateFactory.newLatLng(myUbi));//situa la camara

    }
    public void marcadores(GoogleMap mapa){
        LatLng CorMarcador = new LatLng(Latitud, Longitud);

        mapa.addMarker(new MarkerOptions()
                .position(CorMarcador)
                .title(Latitud+" "+Longitud)
                .snippet(BatLvl+"")
                .icon(BitmapDescriptorFactory
                        .fromResource(android.R.drawable.presence_online))
                .anchor(0.5f, 0.5f));
    }
//Bateria
    public void Bateria(){
        BatteryManager bm = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
        batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        BatLvl.setText((batLevel+"")+"%");
    }
//SQLITE


    public long sql(){
    //Se usa writable por ser un insert
        SQLiteDatabase baseDeDatos = ayudanteBaseDeDatos.getWritableDatabase();
        ContentValues valoresParaInsertar = new ContentValues();
        valoresParaInsertar.put("latitud", Latitud);
        valoresParaInsertar.put("longitud", Longitud);
        valoresParaInsertar.put("bateria", batLevel);
        return baseDeDatos.insert("coordenadas_bat",null,valoresParaInsertar);
    }
}