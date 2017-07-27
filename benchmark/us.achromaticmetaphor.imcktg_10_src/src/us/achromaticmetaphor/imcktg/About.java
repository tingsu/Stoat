package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class About extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    ((TextView) findViewById(R.id.About)).setMovementMethod(new ScrollingMovementMethod());
  }

}
