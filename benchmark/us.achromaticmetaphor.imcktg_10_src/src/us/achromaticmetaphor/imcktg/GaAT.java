package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GaAT extends Activity {

  private final String menuAbout = "About";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_layout);
    String [] choices = new String [] {getString(R.string.for_contacts), getString(R.string.for_default), getString(R.string.for_tofile)};
    final Class<?> [] activities = new Class [] {SelectContacts.class, DefaultToneInput.class, ChooseFilename.class};
    ListView lview = (ListView) findViewById(R.id.cmdlist);
    lview.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, choices));
    lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
        startActivity(new Intent(GaAT.this, activities[pos]));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuItem about = menu.add(menuAbout);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      about.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    about.setIcon(R.drawable.ic_action_about);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem mi) {
    super.onOptionsItemSelected(mi);
    if (mi.getTitle().equals(menuAbout))
      startActivity(new Intent(this, About.class));
    return true;
  }

}
