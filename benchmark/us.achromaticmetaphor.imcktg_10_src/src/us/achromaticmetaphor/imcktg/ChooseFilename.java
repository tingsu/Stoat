package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ChooseFilename extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_choose_filename);
  }

  public void confirm(View view) {
    String tonestring = ((TextView) findViewById(R.id.def_ringtone_text_box)).getText().toString();
    String filename = ((TextView) findViewById(R.id.filename_text_box)).getText().toString();
    Intent intent = new Intent(this, ConfirmContacts.class);
    intent.putExtra(ConfirmContacts.extrakeyTonestring, tonestring);
    intent.putExtra(ConfirmContacts.extrakeyFilename, filename);
    intent.putExtra(ConfirmContacts.extrakeyFordefault, true);

    startActivity(intent);
    finish();
  }

}
