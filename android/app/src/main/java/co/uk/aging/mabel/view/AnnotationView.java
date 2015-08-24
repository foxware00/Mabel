package co.uk.aging.mabel.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.uk.aging.mabel.model.MapSubmission;
import co.uk.aging.mabel.utils.Constants;
import co.uk.aging.mabel.utils.DBConstants;
import co.uk.hackathon.mabel.R;

/**
 * Created by Ryan McClarnon (ryan@breezie.com) on 23/08/15.
 */
public class AnnotationView extends FragmentActivity {
    private SupportMapFragment mMap;
    private MapSubmission mMapSubmission;
    private TextView mPlaceName;
    private TextView mStreetName;
    private ImageView mImage;
    private TextView mDescription;
    private TextView mLastUpdated;
    private Button mItsFixedNow;
    private Geocoder mGcd;

    public AnnotationView() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.annotation_view);
        android.support.v4.app.FragmentManager fragmentManager = this.getSupportFragmentManager();
        mMap = (SupportMapFragment) fragmentManager.findFragmentById(R.id.annotation_map);
        mPlaceName = (TextView) findViewById(R.id.annotation_place_name);
        mStreetName = (TextView) findViewById(R.id.annotation_street_name);
        mDescription = (TextView) findViewById(R.id.annotation_description);
        mLastUpdated = (TextView) findViewById(R.id.annotation_last_updated);
        mImage = (ImageView) findViewById(R.id.annotation_image_view);
        mItsFixedNow = (Button) findViewById(R.id.annotation_fixed_now);

        Intent i = getIntent();
        String updated = i.getStringExtra(Constants.MAP_SUB_EXTRA_UPDATED);
        String description = i.getStringExtra(Constants.MAP_SUB_EXTRA_DESCRIPTION);
        double lat = i.getDoubleExtra(Constants.MAP_SUB_EXTRA_LATITUDE, 0);
        double lng = i.getDoubleExtra(Constants.MAP_SUB_EXTRA_LONGITUDE, 0);
        byte[] data = i.getByteArrayExtra(Constants.MAP_SUB_EXTRA_IMAGE);
        Bitmap bitmap = null;
        if (data != null && data.length > 0) {
            ParseFile bum = new ParseFile(DBConstants.FILE, data,  "bitmap");
            byte[] file;
            try {
                file = bum.getData();
                bitmap = BitmapFactory.decodeByteArray(file, 0, file.length);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        mDescription.setText(description);
        mLastUpdated.setText(updated);
        if (bitmap != null) {
            mImage.setImageBitmap(bitmap);
        }

        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.getMap().moveCamera(center);
        mMap.getMap().animateCamera(zoom);

        List<Address> addresses = getLocation(lat, lng);
        mPlaceName.setText(addresses.get(0).getAddressLine(0));
        mStreetName.setText(addresses.get(0).getAddressLine(1));
    }

    private List<Address> getLocation(double lat, double lng) {
        if (mGcd == null) {
            mGcd = new Geocoder(this, Locale.getDefault());
        }
        List<Address> addresses = null;
        try {
            addresses = mGcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0)
            return addresses;

        return null;
    }
}
