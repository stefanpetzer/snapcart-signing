package co.za.snapcart.signing;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.*;

public class SnapcartSigningPlugin extends CordovaPlugin {

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try
		{
			final int duration = Toast.LENGTH_SHORT;
			final String fileName = args.getString(0);


			String location = preferences.getString("androidpersistentfilelocation", "internal");

			 String persistentRoot = cordova.getActivity().getFilesDir().getAbsolutePath();


			Log.v("Snapcart","Starting ImpressionActivity "+ action + ":" + args.toString());

			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {

					Toast toast = Toast.makeText(cordova.getActivity().getApplicationContext(), action, duration);
					toast.show();
					Context context=cordova.getActivity().getApplicationContext();
					Intent intent = new Intent(context, co.za.snapcart.signing.ImpressionActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("pdfFile", fileName);
					context.startActivity(intent);
				}
			});
			callbackContext.success(args.getString(0));
			return true;
		}catch(Throwable T)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			T.printStackTrace(pw);
			callbackContext.error(sw.toString());	
			return false;		
		}
    }
}