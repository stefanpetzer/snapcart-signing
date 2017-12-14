package co.za.snapcart.signing;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import {{appId}}.R;


import za.co.impression.ReadFragment;
import za.co.impression.ReadFragmentListener;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import java.io.File;

public class ImpressionActivity extends Activity
{
	//String pdfTestPath = Environment.getDataDirectory() + "/invoice.pdf";
	
	private static final String TAG = "Impression";
	private ReadFragment readFragment = null;
	private FragmentManager fm;

	private static final String KEY_USE_SPEN = "SPEN";
	private static final String KEY_SIGRECT_COLOUR = "SIGRECT_COLOUR";

	
	public ImpressionActivity()
	{
		
	}
	

	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());;

		preferences.edit().putString(KEY_SIGRECT_COLOUR, "#AA2233").commit();
		preferences.edit().putBoolean(KEY_USE_SPEN, true).commit();


		Intent intent = getIntent();
		
		//DisplayToast("Trying to open up : " + pdfTestPath);
		String pdfFile = intent.getStringExtra("pdfFile");
	//	pdfTestPath = pdfFile;
		pdfFile = pdfFile.replace("file://", "");
		Log.d(TAG, "Opening Up File : " + pdfFile);

		//pdfTestPath
        setContentView(R.layout.activity_impression);

		fm = getFragmentManager(); //setContentView(R.layout.activity_impression);

        getActionBar().setDisplayHomeAsUpEnabled(true);
		Log.d(TAG,"SPEN: " + preferences.getBoolean(KEY_USE_SPEN, true));


        readFragment = (ReadFragment) fm.findFragmentById(R.id.read_fragment);
		readFragment.addListener(new DocumentLoadListener());


		/*File[] files = this.getApplication().getFilesDir().listFiles();
		File invoiceFile = null;
		for(int i = 0;i<files.length;i++)
		{
			Log.d(TAG, "found file[" + i + "] - " + files[i]);
			DisplayToast("found file[" + i + "] - " + files[i]);
		}*/
		File invoiceFile = new File(this.getApplication().getFilesDir() + pdfFile);
		Log.d(TAG, "using file " + invoiceFile + " - exists - " + invoiceFile.exists());
		DisplayToast("using file " + invoiceFile + " - exists - " + invoiceFile.exists());
		new LoadFile().execute(new String[] { invoiceFile.getAbsolutePath()});

		/*fm.beginTransaction()
        .replace(R.layout.activity_impression, readFragment)
        .addToBackStack(null)
        .commit();*/
	}
	
	private Boolean addAction = false;
	private enum MENUITEMS 
	{
		CANCEL,
		DONE,
		ADD_SIG,
		ABOUT
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause - Main Enter");
		if(readFragment.isVisible())
		{
			Log.d(TAG, "onPause - Document is open");
			if(readFragment.impAlertDlg != null) {
				readFragment.impAlertDlg.dismiss();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState - Enter");
		readFragment.closePDF();
		super.onSaveInstanceState(outState);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);

		menu.add(Menu.NONE, MENUITEMS.CANCEL.ordinal(), Menu.NONE, "Cancel");
		menu.getItem(MENUITEMS.CANCEL.ordinal()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menu.getItem(MENUITEMS.CANCEL.ordinal()).setIcon(R.drawable.ic_close);
		menu.add(Menu.NONE, MENUITEMS.DONE.ordinal(), Menu.NONE, "Done");
		menu.getItem(MENUITEMS.DONE.ordinal()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.getItem(MENUITEMS.DONE.ordinal()).setIcon(R.drawable.ic_check);
		
		menu.add(Menu.NONE, MENUITEMS.ADD_SIG.ordinal(), Menu.NONE, "Add Signature");
		menu.getItem(MENUITEMS.ADD_SIG.ordinal()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.getItem(MENUITEMS.ADD_SIG.ordinal()).setIcon(R.drawable.ic_add_white_24dp);
		
		menu.add(Menu.NONE, MENUITEMS.ABOUT.ordinal(), Menu.NONE, "About");
		menu.getItem(MENUITEMS.ABOUT.ordinal()).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		
		
		if(!addAction)
		{

			menu.getItem(MENUITEMS.CANCEL.ordinal()).setEnabled(false);
			menu.getItem(MENUITEMS.CANCEL.ordinal()).setVisible(false);
			menu.getItem(MENUITEMS.DONE.ordinal()).setEnabled(false);
			menu.getItem(MENUITEMS.DONE.ordinal()).setVisible(false);
			
		}
		else
		{
			
			menu.getItem(MENUITEMS.ADD_SIG.ordinal()).setEnabled(false);
			menu.getItem(MENUITEMS.ADD_SIG.ordinal()).setVisible(false);
			menu.getItem(MENUITEMS.ABOUT.ordinal()).setEnabled(false);
			menu.getItem(MENUITEMS.ABOUT.ordinal()).setVisible(false);
	
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		if(item.getItemId() == android.R.id.home)
		{
			readFragment.closePDF();
			super.onBackPressed();
	         return false;
		}
		
		else if(item.getItemId() == MENUITEMS.ADD_SIG.ordinal())
		{
			readFragment.SetSignatureDraw(true);
			DisplayToast("Tap on the document to place your signature.");
			
			addAction = true;
			this.invalidateOptionsMenu();
			return true;
		}
		else if(item.getItemId() ==  MENUITEMS.DONE.ordinal())
		{
			addAction = false;
			
			if(readFragment.SignNewTag())
			{
				this.invalidateOptionsMenu();
				return true;
			}
			return false;
		}
		else if(item.getItemId() ==  MENUITEMS.CANCEL.ordinal())
		{
			addAction = false;
			readFragment.SetSignatureDraw(false);
			this.invalidateOptionsMenu();
			return true;
		}
		
		else if(item.getItemId() ==  MENUITEMS.ABOUT.ordinal())
		{
			String versionName = "unknown";
			try
			{
				versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			}
			catch (NameNotFoundException e)
			{
				
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{

						}
					});

			builder.setMessage("Impression (TM)\r\n"
							+ "Version: " + versionName + " \r\n\r\n"
							+ "Patents: 2011/09542 \r\n\r\n"
							+ "Copyright (C) 2015-2016 Impression Signature (Pty) Ltd.\r\n"
							+ "All rights reserved.")
							
					.setTitle("About Impression");
			// 3. Get the AlertDialog from create()
			builder.setIcon(R.drawable.impression_small);
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	
		
	}
	

	private void DisplayToast(final String Message)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private class LoadFile extends AsyncTask<String, Void, Void>
	{
		private static final String TAG = "Impression.LoadFile";
    
		public LoadFile() {}
    
		protected Void doInBackground(String... params)
		{
		  Log.d("Impression.LoadFile", "doInBackground - Enter");
		  try
		  {
			ImpressionActivity.this.readFragment.setPathtoDocument(params[0]);
			byte[] Input = ImpressionActivity.this.readFragment.highlightSigFields();
			DisplayToast("using file size " + Input.length+ "");

			Log.d("Impression.LoadFile", "Size of Byte !" + Integer.toString(Input.length));
			ByteArrayInputStream Q = new ByteArrayInputStream(Input, 0, 
			  Input.length);
        
			BufferedInputStream pdfBufferedInputStream = new BufferedInputStream(Q);
        
			ImpressionActivity.this.readFragment.loadPDFFromInputStream(pdfBufferedInputStream);
			DisplayToast("Loaded the pdf into the read fragment");
		  }
		  catch (Exception e)
		  {
			Log.d("Impression.LoadFile", "Error loading PDF: ", e);
			e.printStackTrace();
			DisplayToast("Error Loading the File!");
		  }
		  Log.d("Impression.LoadFile", "doInBackground - Exit");
		  return null;
		}
	  }

	public void LoadPdf(String file)
	{
		this.readFragment = new ReadFragment();
		new LoadFile().execute(new String[] { file });
	}
	
	private class DocumentLoadListener implements ReadFragmentListener
	{
		private static final String TAG = "Impression.DocumentLoadListener";
    
		private DocumentLoadListener() {}
    
		public void onDocumentLoaded()
		{
		  Log.d("Impression.DocumentLoadListener", "onDocumentLoaded - Enter");
		  if (ImpressionActivity.this.readFragment != null) {
			ImpressionActivity.this.readFragment.removeListener(this);
		  }
		  ImpressionActivity.this.invalidateOptionsMenu();

		  Log.d("Impression.DocumentLoadListener", "onDocumentLoaded - Exit");
		}
    
		public void onDocumentLoadFailed()
		{
		  Log.d("Impression.DocumentLoadListener", "onDocumentLoadFailed - Enter");
		  if (ImpressionActivity.this.readFragment != null) {
			ImpressionActivity.this.readFragment.removeListener(this);
		  }
		  Log.d("Impression.DocumentLoadListener", "onDocumentLoadFailed - Exit");
		}
  }


  public void Hello(Context context)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    
    LinearLayout.LayoutParams nomrgLayout = new LinearLayout.LayoutParams(-1, -1);
    nomrgLayout.setMargins(0, 0, 0, 0);
    
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(1);
    ll.setBackgroundColor(-1);
    ll.setPadding(0, 0, 0, 0);
    
    RelativeLayout spenViewLayout = new RelativeLayout(ll.getContext());
    spenViewLayout.setBackgroundColor(-1);
    
    ImageView mSpenView = new ImageView(context);
    if (mSpenView == null)
    {
      Toast.makeText(context, "Cannot create new SpenView.", 0).show();
      return;
    }
    mSpenView.setPadding(0, 0, 0, 0);
    spenViewLayout.setPadding(0, 0, 0, 0);
    spenViewLayout.addView(mSpenView, nomrgLayout);
    
    ll.setGravity(17);
    ll.addView(spenViewLayout, nomrgLayout);
    
    builder = builder.setView(ll);
    builder.setTitle("Place Signature");
    
    builder.setNeutralButton("CLEAR", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int id) {}
    });
    builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int id) {}
    });
    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int id) {}
    });
    AlertDialog impAlertDlg = builder.create();
    impAlertDlg.show();
  }




  public static int getResourseIdByName(String packageName, String className, String name)
  {
    Class r = null;
    int id = 0;
    try
    {
      r = Class.forName(packageName + ".R");
      
      Class[] classes = r.getClasses();
      Class desireClass = null;
      for (int i = 0; i < classes.length; i++) {
        if (classes[i].getName().split("\\$")[1].equals(className))
        {
          desireClass = classes[i];
          
          break;
        }
      }
      if (desireClass != null) {
        id = desireClass.getField(name).getInt(desireClass);
      }
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (SecurityException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    return id;
  }

	
}
