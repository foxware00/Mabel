package co.uk.aging.mabel.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.nio.ByteBuffer;

import co.uk.aging.mabel.model.MapSubmission;
import co.uk.aging.mabel.utils.Constants;
import co.uk.aging.mabel.utils.DBConstants;
import co.uk.hackathon.mabel.R;

/**
 * Created by Ryan McClarnon (ryan@breezie.com) on 22/08/15.
 */
public class SubmissionActivity extends Activity {
    private static final String TAG = SubmissionActivity.class.getSimpleName();

    private MapSubmission mMapSubmission;
    private TextView mTextView;
    private Button mPostButton;
    private Button mCameraButton;
    private ImageView mPictureHolder;

    private Bitmap mImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mTextView = (TextView) findViewById(R.id.post_text_view);
        mPostButton = (Button) findViewById(R.id.post_button);
        mCameraButton = (Button) findViewById(R.id.post_upload_media_button);
        mPictureHolder = (ImageView) this.findViewById(R.id.post_media_image);

        mPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                post();
            }
        });
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraActivity();
            }
        });

        mMapSubmission = new MapSubmission();

        Intent i = getIntent();
        LatLng location = i.getParcelableExtra(Constants.LOCATION_EXTRA);
        if (location != null && !TextUtils.isEmpty(location.toString())) {
            mTextView.setText(location.toString());
            mMapSubmission.setGeoPoint(new ParseGeoPoint(location.latitude, location.longitude));
            mMapSubmission.setDescription(location.toString());
        }

        mMapSubmission.setProblemOrSolution(DBConstants.SUBMISSION_NEITHER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAMERA_EXTRA) {
            if (resultCode == Activity.RESULT_OK) {
                // Display image received on the view
                Bundle b = data.getExtras(); // Kept as a Bundle to check for other things in my actual code
                mImageBitmap = (Bitmap) b.get("data");


                if (mImageBitmap != null) { // Display your image in an ImageView in your layout (if you want to test it)
                    mPictureHolder.setImageBitmap(mImageBitmap);
                    mPictureHolder.invalidate();
                    mCameraButton.setVisibility(View.GONE);
                    mPictureHolder.setVisibility(View.VISIBLE);
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Camera picker cancelled");
            }
        }
    }

    private void post () {
        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(SubmissionActivity.this);
        dialog.setMessage("Posting Post yall");
        dialog.show();

        if (mImageBitmap != null) {
            mMapSubmission.setFile(new ParseFile(DBConstants.FILE, bitmapToByteArray(mImageBitmap), "bitmap"));
        }
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        mMapSubmission.setACL(acl);

        // Save the post
        mMapSubmission.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
    }

    private void cameraActivity() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.startActivityForResult(camera, Constants.CAMERA_EXTRA);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        return buffer.array();
    }
}
