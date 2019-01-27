package grouppay.dylankilbride.com.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import grouppay.dylankilbride.com.retrofit_interfaces.ProfileAPIInterface;
import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.models.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static grouppay.dylankilbride.com.constants.Constants.LOCALHOST_SERVER_BASEURL;

public class ProfileEditEmail extends AppCompatActivity {

  String userIdStr;
  Button updateEmail;
  EditText newEmail;
  ProfileAPIInterface apiInterface;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_edit_email_new);

    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    userIdStr = getIntent().getStringExtra("userId");

    updateEmail = (Button) findViewById(R.id.profileEditEmailBTN);
    newEmail = (EditText) findViewById(R.id.profileNewEmailAddressET);

    updateEmail.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Retrofit updateEmailRequest = new Retrofit.Builder()
            .baseUrl(LOCALHOST_SERVER_BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        apiInterface = updateEmailRequest.create(ProfileAPIInterface.class);

        updateEmail();
      }
    });
  }

  private void updateEmail() {
    Users user = new Users(Long.valueOf(userIdStr), null, null, newEmail.getText().toString(), null, null);

    Call<Users> call = apiInterface.updateUserEmail(userIdStr, user);

    call.enqueue(new Callback<Users>() {
      @Override
      public void onResponse(Call<Users> call, Response<Users> response) {
        if(!response.isSuccessful()) {
          //Handle
        } else {
          Intent intent = new Intent(ProfileEditEmail.this, Profile.class);
          intent.putExtra("userId", userIdStr);
          startActivity(intent);
          finish();
        }
      }

      @Override
      public void onFailure(Call<Users> call, Throwable t) {

      }
    });
  }

  public void setUpActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.editEmailAddressToolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      LayoutInflater inflator = LayoutInflater.from(this);
      View v = inflator.inflate(R.layout.generic_titleview, null);

      ((TextView) v.findViewById(R.id.title)).setText(R.string.toolbar_edit_email_address_title);
      ((TextView) v.findViewById(R.id.title)).setTextSize(20);

      this.getSupportActionBar().setCustomView(v);
    }
  }
}
