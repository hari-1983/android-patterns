package in.hktc.patterns.errorhandling;

import android.app.Activity;
import android.os.Bundle;

public class CrashLandingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = getIntent().getIntExtra("layout", 0);
        if (layout > 0) {
            setContentView(layout);
        }
    }
}
