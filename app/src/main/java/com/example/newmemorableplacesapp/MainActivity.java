package com.example.newmemorableplacesapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static ArrayList<LatLng> coords;
    int savedPlacesIndex;
    ArrayAdapter arrayAdapter;
    ListView listView;
    SharedPreferences sharedPreferences;
    ArrayList<String> places;
    ArrayList<Double> latitudes, longitudes;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        savedPlacesIndex=0;

        places = new ArrayList<String>();
        coords = new ArrayList<LatLng>();
        latitudes = new ArrayList<Double>();
        longitudes = new ArrayList<Double>();


        sharedPreferences = this.getSharedPreferences("com.example.newmemorableplacesapp", Context.MODE_PRIVATE);

        if(!sharedPreferences.contains("places")||!sharedPreferences.contains("latitudes")||!sharedPreferences.contains("longitudes"))
            try {
                Log.i("Status", "does not contain places");

                places.add("+ Add New Place");
                coords.add(new LatLng(18.474, 73.56));
                latitudes.add((coords.get(0)).latitude);
                longitudes.add((coords.get(0)).longitude);


                sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
                sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
                sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

            } catch (IOException e) {
                e.printStackTrace();
            }

        Log.i("Contains lats", String.valueOf(sharedPreferences.contains("latitudes")));

        //Retrieve data from shared preferences
        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<Double>())));
            longitudes = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<Double>())));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("OC newplaces:", places.toString());
        Log.i("OC newlats:", latitudes.toString());
        Log.i("OC newlongs:", longitudes.toString());

        coords.clear();
        for(int i=0; i<latitudes.size(); i++)
        {
            coords.add(i, new LatLng(latitudes.get(i), longitudes.get(i)));
        }

        Log.i("retreived coords:", coords.toString());
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("lat", coords.get(position).latitude);
                intent.putExtra("long", coords.get(position).longitude);
                intent.putExtra("savedAddress", places.get(position));
                intent.putExtra("placeNumber", position);

                Log.d("OICL:", "sent latlng: "+coords.get(position).latitude +", "+coords.get(position).longitude);
                startActivity(intent);

            }
        });


        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String receivedAddress = intent.getStringExtra("markedAddress");
                Double markedLatitude = intent.getDoubleExtra("latitude", 0.00);
                Double markedLongitude = intent.getDoubleExtra("longitude", 0.00);

                Log.i("LBM LatLang", String.valueOf(markedLatitude) + ", " + String.valueOf(markedLongitude));

                places.add(receivedAddress);
                coords.add(new LatLng(markedLatitude, markedLongitude));
                latitudes.add(markedLatitude);
                longitudes.add(markedLongitude);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                try {
                    editor.putString("places", ObjectSerializer.serialize(places));
                    editor.putString("latitudes", ObjectSerializer.serialize(latitudes));
                    editor.putString("longitudes", ObjectSerializer.serialize(longitudes));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                editor.commit();

                //ArrayList<String> newPlaces = new ArrayList<String>();

                try {
                    places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
                    latitudes = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<Double>())));
                    longitudes = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<Double>())));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                coords.clear();
                for(int i=0; i<latitudes.size(); i++)
                {
                    coords.add(i, new LatLng(latitudes.get(i), longitudes.get(i)));
                }


                Log.i("BR NP Size:", String.valueOf(places.size()));
                Log.i("BR places:", places.toString());
                Log.i("BR coords:", coords.toString());

                savedPlacesIndex++;

                listView.setAdapter(arrayAdapter);
                //ArrayList<Double> tempAL = (ArrayList<Double>) getIntent().getSerializableExtra("markedLatLng");


                //Log.i("SizeoftempAL: ", String.valueOf(tempAL.size()));

                // listOfCoords.add((ArrayList<Double>) getIntent().getSerializableExtra("markedLatLng"));
                // places.add(receivedAddress);
                // Log.i("ReceivedAddress:", String.valueOf(tempAL.get(0))+ ", " + String.valueOf(tempAL.get(1)));
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Send New Address Back"));


    }
}
