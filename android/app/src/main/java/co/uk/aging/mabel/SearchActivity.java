package co.uk.aging.mabel;

/**
 * Created by oliverfox on 22/08/2015.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.nio.ByteBuffer;

import co.uk.aging.mabel.model.MapSubmission;
import co.uk.aging.mabel.places.search.GPSTracker;
import co.uk.aging.mabel.utils.Constants;
import co.uk.aging.mabel.utils.DBConstants;
import co.uk.hackathon.mabel.R;


public class SearchActivity extends FragmentActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteTextView;
    private TextView mNameTextView;
    private TextView mAddressTextView;
    private TextView mIdTextView;
    private TextView mPhoneTextView;
    private TextView mWebTextView;
    private TextView mAttTextView;
    private TextView mRatingTextView;
    private ImageView mImage;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    private static final String TAG = SearchActivity.class.getSimpleName();
    private Button mMapSearch;
    private Button mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places);
        mGoogleApiClient = new GoogleApiClient.Builder(SearchActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        mAutocompleteTextView.setThreshold(3);
        mNameTextView = (TextView) findViewById(R.id.name);
        mAddressTextView = (TextView) findViewById(R.id.address);
        mIdTextView = (TextView) findViewById(R.id.place_id);
        mPhoneTextView = (TextView) findViewById(R.id.phone);
        mWebTextView = (TextView) findViewById(R.id.web);
        mAttTextView = (TextView) findViewById(R.id.att);
        mRatingTextView = (TextView) findViewById(R.id.rating);
        mImage = (ImageView) findViewById(R.id.image);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mAddButton = (Button) findViewById(R.id.add);
        mAddButton.setVisibility(View.INVISIBLE);
        GPSTracker gps = new GPSTracker(getApplicationContext());
        LatLngBounds mBounds = new LatLngBounds(new LatLng(gps.getLatitude() - 2, gps.getLongitude() - 2), new LatLng(gps.getLatitude() + 2, gps.getLongitude() + 2));
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                mBounds, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        mMapSearch = (Button) findViewById(R.id.map_search);
        mMapSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(SearchActivity.this);
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, 4);
                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), SearchActivity.this, 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(SearchActivity.this, "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    /**
     * Extracts data from PlacePicker result.
     * This method is called when an Intent has been started by calling
     * {@link #startActivityForResult(android.content.Intent, int)}. The Intent for the
     * {@link com.google.android.gms.location.places.ui.PlacePicker} is started with
     * {@link #4} request code. When a result with this request code is received
     * in this method, its data is extracted by converting the Intent data to a {@link Place}
     * through the
     * {@link com.google.android.gms.location.places.ui.PlacePicker#getPlace(android.content.Intent,
     * android.content.Context)} call.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BEGIN_INCLUDE(activity_result)
        if (requestCode == 4) {
            // This result is from the PlacePicker dialog.

            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, SearchActivity.this);

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */// Selecting the first object buffer.
                CharSequence attributions = null;
                mNameTextView.setText("Name = " + Html.fromHtml(place.getName() + ""));
                mAddressTextView.setText("Address = " + Html.fromHtml(place.getAddress() + ""));
                mIdTextView.setText("Place Id = " + Html.fromHtml(place.getId() + ""));
                mPhoneTextView.setText("Phone = " + Html.fromHtml(place.getPhoneNumber() + ""));
                mWebTextView.setText("Website = " + place.getWebsiteUri() + "");
                if (attributions != null) {
                    mAttTextView.setText(Html.fromHtml(attributions.toString()));
                }
                float rating = place.getRating();
                mRatingTextView.setText("Rating = " + rating);

                new PhotoTask(300, 300).execute(place.getId());
                mAddButton.setVisibility(View.VISIBLE);
                mAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // todo Save place information to hash map
                        Toast.makeText(SearchActivity.this, "Added " + Html.fromHtml(place.getName() + "") + " to your itinerary", Toast.LENGTH_SHORT).show();
                        finish();
                        Bitmap b = drawableToBitmap(mImage.getDrawable());
                        addSubmission(place.getLatLng().longitude, place.getLatLng().latitude, place.getName().toString(), b);
                    }
                });

            } else {
                // User has not selected a place, hide the card.
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // END_INCLUDE(activity_result)
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            mNameTextView.setText("Name = " + Html.fromHtml(place.getName() + ""));
            mAddressTextView.setText("Address = " + Html.fromHtml(place.getAddress() + ""));
            mIdTextView.setText("Place Id = " + Html.fromHtml(place.getId() + ""));
            mPhoneTextView.setText("Phone = " + Html.fromHtml(place.getPhoneNumber() + ""));
            mWebTextView.setText("Website = " + place.getWebsiteUri() + "");
            if (attributions != null) {
                mAttTextView.setText(Html.fromHtml(attributions.toString()));
            }
            float rating = place.getRating();
            mRatingTextView.setText("Rating = " + rating);

            new PhotoTask(300, 300).execute(place.getId());

            mAddButton.setVisibility(View.VISIBLE);
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // todo Save place information to hash map
                    Toast.makeText(SearchActivity.this, "Added " + Html.fromHtml(place.getName() + "") + " to your itinerary", Toast.LENGTH_SHORT).show();
                    finish();
                    Bitmap b = drawableToBitmap(mImage.getDrawable());
                    addSubmission(place.getLatLng().longitude, place.getLatLng().latitude, place.getName().toString(), b);
                }
            });
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    private void addSubmission(double lon, double lat, String description, Bitmap bitmap) {
        MapSubmission mMapSubmission = new MapSubmission();

        mMapSubmission.setGeoPoint(new ParseGeoPoint(lat, lon));
        mMapSubmission.setDescription(description);
        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
        dialog.setMessage("Posting Post yall");
        dialog.show();

        if (bitmap != null) {
            mMapSubmission.setFile(new ParseFile(DBConstants.FILE, bitmapToByteArray(bitmap), "bitmap"));
        }
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        mMapSubmission.setACL(acl);
        mMapSubmission.setProblemOrSolution(DBConstants.SUBMISSION_SOLUTION);

        // Save the post
        mMapSubmission.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });

    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        return buffer.array();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        @Override
        protected void onPostExecute(AttributedPhoto attributedPhoto) {
            super.onPostExecute(attributedPhoto);
            if (mImage != null && attributedPhoto != null && attributedPhoto.bitmap != null) {
                mImage.setImageBitmap(attributedPhoto.bitmap);
            }
        }

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }
}
