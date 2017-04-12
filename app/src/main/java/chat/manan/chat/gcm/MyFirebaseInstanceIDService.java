package chat.manan.chat.gcm;



        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.util.Log;

        import com.google.firebase.iid.FirebaseInstanceId;
        import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Belal on 03/11/16.
 */

//Class extending FirebaseInstanceIdService
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        //calling the method store token and passing token
        storeToken(refreshedToken);
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        Log.i("chat","chat");
        startService(intent);
    }

    private void storeToken(String token) {
        //we will save the token in sharedpreferences later

    }
}