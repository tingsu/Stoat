package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class DefaultToneInput extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_default_tone_input);
  }

  public void confirm(View view) {
    String tonestring = ((TextView) findViewById(R.id.def_ringtone_text_box)).getText().toString();
    Intent intent = new Intent(this, ConfirmContacts.class);
    intent.putExtra(Tone.extrakeyRingtone, ((CheckBox) findViewById(R.id.ringtone_checkbox)).isChecked());
    intent.putExtra(Tone.extrakeyNotification, ((CheckBox) findViewById(R.id.notification_checkbox)).isChecked());
    intent.putExtra(ConfirmContacts.extrakeyTonestring, tonestring);
    intent.putExtra(ConfirmContacts.extrakeyFordefault, true);

    startActivity(intent);
    finish();
  }

}
