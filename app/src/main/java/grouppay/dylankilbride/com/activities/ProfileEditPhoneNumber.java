package grouppay.dylankilbride.com.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import grouppay.dylankilbride.com.grouppay.R;

public class ProfileEditPhoneNumber extends AppCompatActivity {

  String userIdStr;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_edit_phone_number);

    userIdStr = getIntent().getStringExtra("userId");
  }
}