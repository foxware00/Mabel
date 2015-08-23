package co.uk.aging.mabel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import co.uk.hackathon.mabel.R;

/**
 * Created by oliverfox on 23/08/2015.
 */
public class FrontPageActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_page);

        Button search = (Button) findViewById(R.id.search);
        Button write = (Button) findViewById(R.id.write);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FrontPageActivity.this, SearchActivity.class));
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FrontPageActivity.this, AddActivity.class));
            }
        });



        if (getActionBar() != null)
            getActionBar().hide();
    }
}
