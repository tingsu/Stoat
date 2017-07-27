package biz.gyrus.yaab;

import biz.gyrus.yaab.billing.util.IabHelper;
import biz.gyrus.yaab.billing.util.IabHelper.QueryInventoryFinishedListener;
import biz.gyrus.yaab.billing.util.IabResult;
import biz.gyrus.yaab.billing.util.Inventory;
import biz.gyrus.yaab.billing.util.Purchase;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DonateActivity extends ThemedActivity {

    static final String SKU_DONATE1 = "yaab.donate.1";
    //static final String SKU_DONATE1 = "android.test.purchased";
    static final String SKU_DONATE2 = "yaab.donate.2";
    static final String SKU_DONATE3 = "yaab.donate.3";
    
    static final int RC_REQUEST = 10042;
    
	IabHelper _iabHelper;
	
	TextView _txtStatus;
	Button _btnDonate1;
	Button _btnDonate2;
	Button _btnDonate3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_donate);
		_txtStatus = (TextView) findViewById(R.id.txtStatus);
		_btnDonate1 = (Button) findViewById(R.id.btnDonate1);
		_btnDonate2 = (Button) findViewById(R.id.btnDonate2);
		_btnDonate3 = (Button) findViewById(R.id.btnDonate3);
		
		String base64EncodedPublicKey = "Kilroy was here";

		_iabHelper = new IabHelper(this, base64EncodedPublicKey);
 
		_iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			
			public void onIabSetupFinished(IabResult result) 
			{
				if (!result.isSuccess()) {
					if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "In-app Billing setup FAILED: " + result);
					_txtStatus.setText(R.string.activity_donate_status_billingfailed);
				} else {             
					if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "In-app Billing is set up OK");
					
					_iabHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
						
						@Override
						public void onQueryInventoryFinished(IabResult result, Inventory inv) {
							
				            if (result.isFailure()) {
				            	if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Failed to query inventory: " + result);
				                return;
				            }

				            //if (inv.hasPurchase("android.test.purchased")) {_iabHelper.consumeAsync(inv.getPurchase("android.test.purchased"),null); }				            
				            
				            Purchase pDon1 = inv.getPurchase(SKU_DONATE1);
				            if(pDon1 != null)
				            {
				            	_btnDonate1.setEnabled(false);
				            	_btnDonate1.setText(R.string.activity_donate_btn_yours);
				            }
				            else
				            	_btnDonate1.setEnabled(true);
							
				            Purchase pDon2 = inv.getPurchase(SKU_DONATE2);
				            if(pDon2 != null)
				            {
				            	_btnDonate2.setEnabled(false);
				            	_btnDonate2.setText(R.string.activity_donate_btn_yours);
				            }
				            else
				            	_btnDonate2.setEnabled(true);
							
				            Purchase pDon3 = inv.getPurchase(SKU_DONATE3);
				            if(pDon3 != null)
				            {
				            	_btnDonate3.setEnabled(false);
				            	_btnDonate3.setText(R.string.activity_donate_btn_yours);
				            }
				            else
				            	_btnDonate3.setEnabled(true);
							
							_txtStatus.setText(R.string.activity_donate_status_billingok);
						}
					});
					
				}
			}
		});		
	
		_btnDonate1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					if(_iabHelper != null)
						_iabHelper.launchPurchaseFlow(DonateActivity.this, SKU_DONATE1, RC_REQUEST, _purchaseFinishedListener);
				} catch(IllegalStateException ise)
				{
					Log.e(Globals.TAG, "Error launching purchase flow, SKU_DONATE1", ise);
				}
				
			}
		});
		_btnDonate2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					if(_iabHelper != null)
						_iabHelper.launchPurchaseFlow(DonateActivity.this, SKU_DONATE2, RC_REQUEST, _purchaseFinishedListener);
				} catch(IllegalStateException ise)
				{
					Log.e(Globals.TAG, "Error launching purchase flow, SKU_DONATE2", ise);
				}
				
			}
		});
		_btnDonate3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					if(_iabHelper != null)
						_iabHelper.launchPurchaseFlow(DonateActivity.this, SKU_DONATE3, RC_REQUEST, _purchaseFinishedListener);
				} catch(IllegalStateException ise)
				{
					Log.e(Globals.TAG, "Error launching purchase flow, SKU_DONATE3", ise);
				}
				
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(_iabHelper == null)
			return;
		
		if(!_iabHelper.handleActivityResult(requestCode, resultCode, data))
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(_iabHelper != null)
		{
			_iabHelper.dispose();
			_iabHelper = null;
		}
	}

    IabHelper.OnIabPurchaseFinishedListener _purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

        	if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (_iabHelper == null) return;

            if (result.isFailure()) {
            	Log.e(Globals.TAG, "Error purchasing: " + result);
                return;
            }

            if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_DONATE1)) {
            	if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Donate 1");
            	_btnDonate1.setEnabled(false);
            	_btnDonate1.setText(R.string.activity_donate_btn_yours);
            }
            else if (purchase.getSku().equals(SKU_DONATE2)) {
            	if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Donate 2");
            	_btnDonate2.setEnabled(false);
            	_btnDonate2.setText(R.string.activity_donate_btn_yours);
            }
            else if (purchase.getSku().equals(SKU_DONATE3)) {
            	if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Donate 3");
            	_btnDonate3.setEnabled(false);
            	_btnDonate3.setText(R.string.activity_donate_btn_yours);
            }
        }
    };
	
}
