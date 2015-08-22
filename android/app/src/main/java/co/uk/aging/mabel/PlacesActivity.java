package co.uk.aging.mabel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.HashMap;

import co.uk.aging.mabel.places.SinglePlaceActivity;
import co.uk.aging.mabel.places.common.logger.Log;
import co.uk.hackathon.mabel.R;


public class PlacesActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();

    private static final String TAG = PlacesActivity.class.getSimpleName();
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d(TAG, "onCreate() value savedInstanceState = " + savedInstanceState);

        lv = (ListView) findViewById(R.id.list);
        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);

                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra("reference", reference);
                startActivity(in);
            }
        });

/** Button click event for shown on map */
        Button b = (Button) findViewById(R.id.btn_show_map);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "onClick() value arg0 = " + arg0);
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                        .getCurrentPlace(mGoogleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));

                            // loop through each place
                            HashMap<String, String> map = new HashMap<String, String>();

                            // Place reference won't display in listview - it will be hidden
                            // Place reference is used to get "place full details"
                            map.put("name", String.valueOf(placeLikelihood.getPlace().getName()));
                            String type = String.valueOf(placeLikelihood.getPlace().getPlaceTypes());
                            Log.d(TAG, "onResult() value type = " + type);

                            // Place name
                            map.put("reference", String.valueOf(placeLikelihood.getPlace().getRating()));


                            // adding HashMap to ArrayList
                            placesListItems.add(map);
                        }
                        // list adapter
                        ListAdapter adapter = new SimpleAdapter(PlacesActivity.this, placesListItems,
                                R.layout.list_item,
                                new String[]{"reference", "name"}, new int[]{
                                R.id.reference, R.id.name});

                        // Adding data into listview
                        lv.setAdapter(adapter);
                        likelyPlaces.release();
                    }
                });
                // todo show on map
//                Intent i = new Intent(getApplicationContext(),
//                        PlacesMapActivity.class);
//                // Sending user current geo location
//                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
//                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
//
//                // passing near places to map activity
//                i.putExtra("near_places", nearPlaces);
//                // staring activity
//                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() value  = " + bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d(TAG, "onStart() value mGoogleApiClient = ");
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
