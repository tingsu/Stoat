package com.threedlite.urforms;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.BlobData;
import com.threedlite.urforms.data.BlobDataDao;
import com.threedlite.urforms.data.DataDao;
import com.threedlite.urforms.data.Entity;
import com.threedlite.urforms.data.UrSqlHelper;

public class EditViewFactory {
	
	private static final String TAG = "urforms_EditViewFactory";
	
	private BaseActivity activity;
	private UrSqlHelper sqlHelper;
	private Map<String, Object> mEdits;
	private Map<String, View> mDisplays;
	private Map<String, String> mValues;

	public EditViewFactory(BaseActivity activity, UrSqlHelper sqlHelper, Map<String, Object> edits, Map<String, View> displays, Map<String, String> values) {
		this.activity = activity;
		this.sqlHelper = sqlHelper;
		this.mEdits = edits;
		this.mDisplays = displays;
		this.mValues = values;
	}
	
	
	public View getEdit(Attribute attribute, final String value) {

		View view = null;
		String dataType = attribute.getDataType();


		if (dataType.equals(Attribute.CHOICES_TYPE)) {

			Spinner sp = new Spinner(activity);
			String[][] choices = BaseActivity.parseChoices(attribute);
			String[] desc = new String[choices.length];
			int position = 0;
			for (int i = 0; i < choices.length; i++) {
				if (choices[i][0].equals(value)) { // saved value
					position = i;
				}
				desc[i] = choices[i][1];  // desc
			}
			ArrayAdapter<CharSequence> adapter = 
					new ArrayAdapter<CharSequence>(activity,
							android.R.layout.simple_spinner_item,
							desc);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp.setAdapter(adapter);
			sp.setSelection(position);
			view = sp;
			mEdits.put(attribute.getAttributeName(), view);

		} else if (dataType.equals(Attribute.CHECKBOX_TYPE)) {

			CheckBox cb = new CheckBox(activity);
			cb.setChecked("true".equals(value));
			view = cb;
			mEdits.put(attribute.getAttributeName(), view);

		} else if (dataType.equals(Attribute.DATE_TYPE)) {

			LinearLayout dp = new LinearLayout(activity);
			dp.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(activity);
			tv.setTextSize(20);
			tv.setText(value);
			dp.addView(tv);
			Button ed = new Button(activity);
			ed.setText("Choose Date");
			ed.setOnClickListener(new DateEditClickListener(activity, tv));
			dp.addView(ed);
			view = dp;
			mEdits.put(attribute.getAttributeName(), tv);

		} else if (dataType.equals(Attribute.TIME_TYPE)) {

			LinearLayout dp = new LinearLayout(activity);
			dp.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(activity);
			tv.setTextSize(20);
			tv.setText(value);
			dp.addView(tv);
			Button ed = new Button(activity);
			ed.setText("Choose Time");
			ed.setOnClickListener(new TimeEditClickListener(activity, tv));
			dp.addView(ed);
			view = dp;
			mEdits.put(attribute.getAttributeName(), tv);

		} else if (dataType.equals(Attribute.EDIT_TIMESTAMP_TYPE)) {

			TextView tv = new TextView(activity);
			tv.setTextSize(15);
			tv.setText(value);
			view = tv;

		} else if (dataType.equals(Attribute.REF_TYPE)) {

			LinearLayout dp = new LinearLayout(activity);
			dp.setOrientation(LinearLayout.HORIZONTAL);

			TextView tv1 = new TextView(activity);
			tv1.setText("(");
			dp.addView(tv1);

			TextView tv = new TextView(activity);
			tv.setText(value);
			dp.addView(tv);

			TextView tv2 = new TextView(activity);
			tv2.setText(") ");
			dp.addView(tv2);

			TextView tvDesc = new TextView(activity);
			if (value != null && value.trim().length() > 0) {
				tvDesc.setText(activity.getTitle(attribute.getRefEntityName(), Long.valueOf(value), null));
			}
			dp.addView(tvDesc);
			Button ed = new Button(activity);
			ed.setText("Search");
			Entity refEntity = new Entity();
			refEntity.setName(attribute.getRefEntityName());
			ed.setOnClickListener(new ReferenceEditClickListener(activity, refEntity, attribute.getAttributeName()));
			dp.addView(ed);
			view = dp;
			mEdits.put(attribute.getAttributeName(), tv);
			mDisplays.put(attribute.getAttributeName(), tvDesc);

		} else if (dataType.equals(Attribute.REF_BY_TYPE)) {

			LinearLayout dp = new LinearLayout(activity);
			dp.setOrientation(LinearLayout.VERTICAL);
			
			mEdits.put(attribute.getAttributeName(), dp);
			view = dp;
			
			TableLayout table = new TableLayout(activity);
			dp.addView(table);
			
			TableRow tablerow;

			String ref = attribute.getRefEntityName();
			if (ref == null || ref.trim().length() == 0 || ref.indexOf(" ") == -1) return new TextView(activity);
			String[] sa = ref.split(" ");
			String refEntityName = sa[0];
			String refAttributeName = sa[1];
			Entity refEntity = new Entity();
			refEntity.setName(refEntityName);
			List<Attribute> refAttributes = activity.getAttributes(refEntity);
			String sid = mValues.get("_id");

			List<RefByData> list = new ArrayList<RefByData>();
			if (sid != null) list = getRefByData(refEntityName, refAttributeName, sid);

			for (RefByData rbd: list) {

				tablerow = new TableRow(activity);
				table.addView(tablerow);

				final Entity fEntity = new Entity();
				fEntity.setName(refEntityName);
				Map<String, String> fvalues = new HashMap<String, String>();
				fvalues.put("_id", rbd.id);
				fEntity.setValues(fvalues);

				Button bv = new Button(activity);
				bv.setText("Edit");
				bv.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						activity.startEdit(fEntity);
					}
				});
				tablerow.addView(bv);

				TextView tv1 = new TextView(activity);
				tv1.setText("("+rbd.id+") "+rbd.title);
				tablerow.addView(tv1);

			}

			// Add new
			final Entity fEntity = new Entity();
			fEntity.setName(refEntityName);
			fEntity.setAttributes(refAttributes);
			Map<String, String> fvalues = new HashMap<String, String>();
			fvalues.put("_id", "0");
			fvalues.put(refAttributeName, sid);
			fEntity.setValues(fvalues);
			tablerow = new TableRow(activity);
			table.addView(tablerow);
			Button bv = new Button(activity);
			bv.setText("+");
			bv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					activity.startEdit(fEntity);
				}
			});
			tablerow.addView(bv);


		} else if ( dataType.equals(Attribute.FILE_TYPE) || dataType.equals(Attribute.IMAGE_TYPE) ) {

			LinearLayout dp = new LinearLayout(activity);
			dp.setOrientation(LinearLayout.HORIZONTAL);

			BlobData blobData;
			if (value != null && value.trim().length() > 0) {
				blobData = getBlob(value);
				if (blobData == null) {
					blobData = new BlobData();
				}
			} else {
				blobData = new BlobData();
			}

			TextView tvFileName = null;
			ImageView imImage = null;

			if (dataType.equals(Attribute.FILE_TYPE)) {
				tvFileName = new TextView(activity);
				tvFileName.setText(blobData.getFileName());
				dp.addView(tvFileName);
			} else {
				imImage = new ImageView(activity);
				byte[] b = blobData.getBlobData();
				if (b != null) {
					try {
						Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
						imImage.setImageBitmap(bm);
					} catch (Exception e) {
						Log.e(TAG, "Unable to display image "+e.getMessage());
					}
				}
				dp.addView(imImage);
			}

			LinearLayout dp2 = new LinearLayout(activity);
			dp2.setOrientation(LinearLayout.VERTICAL);
			dp.addView(dp2);

			Button ed = new Button(activity);
			ed.setText("Choose File");
			Entity refEntity = new Entity();
			refEntity.setName(attribute.getRefEntityName());
			ed.setOnClickListener(new ChooseFileClickListener(activity, refEntity, attribute.getAttributeName()));
			dp2.addView(ed);

			if (blobData.getBlobData() != null) {
				Button dl = new Button(activity);
				dl.setText("Save copy");
				final BlobData fblobData = blobData;
				dl.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						saveCopyFile(fblobData);
					}
				});
				dp2.addView(dl);
			}
			
			view = dp;
			mEdits.put(attribute.getAttributeName(), blobData);
			if (dataType.equals(Attribute.FILE_TYPE)) {
				mDisplays.put(attribute.getAttributeName(), tvFileName);
			} else {
				mDisplays.put(attribute.getAttributeName(), imImage);
			}

		} else {

			EditText et = new EditText(activity);
			et.setText(value);
			view = et;
			mEdits.put(attribute.getAttributeName(), view);

		}

		return view;
	}

	
	private void saveCopyFile(BlobData blobData) {
		try {
			if (blobData.getSize() == 0) return;
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + blobData.getFileName();
			File file = new File(filePath);
			if (file.exists()) {
				activity.makeToast("File already exists: "+filePath);
				return;
				//file.delete(); // add conf dialog?
			}
			FileOutputStream fout = new FileOutputStream(file);
			try {
				fout.write(blobData.getBlobData());
				fout.flush();
			} finally {
				fout.close();
			}
			activity.makeToast("File saved "+file.getAbsolutePath());
		} catch (Exception e) {
			activity.makeToast("Unable to save file: "+e.getMessage());
		}
	}


	private BlobData getBlob(String guid) {
		BlobDataDao dataDao = new BlobDataDao(sqlHelper.getWritableDatabase());
		try {
			return dataDao.getByGuid(guid);
		} finally {
			sqlHelper.close();
		}
	}
	
	private static final class RefByData {
		public String id;
		public String title;
	}

	private List<RefByData> getRefByData(String refEntityName, String refAttributeName, String entityid) {
		List<RefByData> result = new ArrayList<RefByData>();
		try {
			Entity refEntity = new Entity();
			refEntity.setName(refEntityName);
			refEntity.setAttributes(activity.getAttributes(refEntity));
			Map<String, String> refValues = new HashMap<String, String>();
			refValues.put(refAttributeName, entityid);

			List<Map<String, String>> results;
			DataDao dataDao = new DataDao(sqlHelper.getWritableDatabase());
			try {
				results = dataDao.search(refEntity, refValues);
			} finally {
				sqlHelper.close();
			}
			for (Map<String, String> row: results) {
				RefByData rbd = new RefByData();
				rbd.id = row.get("_id");
				rbd.title = activity.getTitle(refEntityName, Long.valueOf(rbd.id), refAttributeName);
				result.add(rbd);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}

	protected static void startSelectForResult(Activity activity, Entity entity, String attributeName) {
		Intent intent = new Intent(activity, SearchResultsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(EnterDataActivity.ENTITY_NAME, entity.getName());
		bundle.putString(EnterDataActivity.ATTRIBUTE_NAME, attributeName);
		bundle.putString(EnterDataActivity.SELECT_MODE, EnterDataActivity.SELECT_MODE_SELECT);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, EnterDataActivity.SELECT_REQUEST_CODE);
	}

	protected static void startChooseFileForResult(Activity activity, Entity entity, String attributeName) {
		Intent intent = new Intent(activity, UrformsFileChooserActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(EnterDataActivity.ENTITY_NAME, entity.getName());
		bundle.putString(EnterDataActivity.ATTRIBUTE_NAME, attributeName);
		intent.putExtras(bundle);
		intent.setAction(Intent.ACTION_MAIN);
		activity.startActivityForResult(intent, EnterDataActivity.CHOOSE_FILE_REQUEST_CODE);
	}


	public static class DateEditClickListener implements View.OnClickListener {

		private TextView tv;
		private Activity activity;
		private DatePickerDialog dialog;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		public DateEditClickListener(Activity activity, TextView tv) {
			this.activity = activity;
			this.tv = tv;
		}

		@SuppressWarnings("deprecation")
		public void onClick(View v) {

			String sdate = tv.getText().toString();
			java.util.Date date = new java.util.Date();
			if (sdate != null && sdate.trim().length() > 0) {
				try {
					date = sdf.parse(sdate);
				} catch (Exception e) {}
			}

			dialog = new DatePickerDialog(
					activity,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							tv.setText(sdf.format(new java.util.Date(year-1900, monthOfYear, dayOfMonth)));
							dialog.hide();
						}
					},
					date.getYear(), 
					date.getMonth(), 
					date.getDate()
					);

			dialog.updateDate(date.getYear()+1900,date.getMonth(), date.getDate());
			dialog.show();

		}

	}

	public static class TimeEditClickListener implements View.OnClickListener {

		private TextView tv;
		private Activity activity;
		private TimePickerDialog dialog;

		public TimeEditClickListener(Activity activity, TextView tv) {
			this.activity = activity;
			this.tv = tv;
		}

		public void onClick(View v) {

			String stime = tv.getText().toString();
			if (stime == null || stime.trim().length() == 0) {
				stime = "12:00";
			}
			int[] time = parseTime(stime);

			dialog = new TimePickerDialog(
					activity,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hour, int minute) {
							tv.setText(formatTime(hour,minute));
							dialog.hide();
						}
					},
					time[0], 
					time[1], 
					true
					);
			
			dialog.updateTime(time[0], time[1]);
			dialog.show();

		}

	}
	
	protected static String formatTime(int hour, int minute) {
		String sminute = ""+minute;
		if (sminute.length() == 1) sminute = "0" + sminute;
		return hour + ":" + sminute;
	}

	protected static int[] parseTime(String s) {
		int[] result = new int[2];
		if (s == null || s.length() == 0) return result;
		try {
			int ind = s.indexOf(":");
			if (ind == -1) return result;
			String[] sa = s.split(":");
			result[0] = Integer.valueOf(sa[0].trim());
			result[1] = Integer.valueOf(sa[1].trim());
		} catch (Exception e) {}
		return result;
	}

	public static class ReferenceEditClickListener implements View.OnClickListener {

		private Activity activity;
		private Entity refEntity;
		private String attributeName;

		public ReferenceEditClickListener(Activity activity, Entity refEntity, String attributeName) {
			this.activity = activity;
			this.refEntity = refEntity;
			this.attributeName = attributeName;
		}

		public void onClick(View v) {
			startSelectForResult(activity, refEntity, attributeName);
		}

	}

	public static class ChooseFileClickListener implements View.OnClickListener {

		private Activity activity;
		private Entity refEntity;
		private String attributeName;

		public ChooseFileClickListener(Activity activity, Entity refEntity, String attributeName) {
			this.activity = activity;
			this.refEntity = refEntity;
			this.attributeName = attributeName;
		}

		public void onClick(View v) {
			startChooseFileForResult(activity, refEntity, attributeName);
		}

	}


}
