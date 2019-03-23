package grouppay.dylankilbride.com.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.models.StripeCharge;
import grouppay.dylankilbride.com.models.StripeChargeReceipt;
import grouppay.dylankilbride.com.retrofit_interfaces.CardManagerAPI;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import static grouppay.dylankilbride.com.constants.Constants.LOCALHOST_SERVER_BASEURL;

public class EnterPaymentMethodDetails extends AppCompatActivity {

  public final int MY_SCAN_REQUEST_CODE = 1234;
  private EditText cardholderName, cardNumber, expiryDate, cvv;
  private Button usePaymentDetailsBTN;
  private String expiryMonth, expiryYear, amountToDepositStr, userId, groupAccountId;
  private double amountToDeposit;
  private ProgressWheel paymentProgressSpinner;
  private FrameLayout progressOverlay;
  private CardManagerAPI cardManagerApiInterface;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_enter_payment_method_details);

    userId = getIntent().getStringExtra("userIdStr");
    groupAccountId = getIntent().getStringExtra("groupAccountIdStr");

    amountToDepositStr = getIntent().getStringExtra("amountToDepositStr");
    amountToDeposit = Double.parseDouble(amountToDepositStr);

    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    cardholderName = (EditText) findViewById(R.id.enterPaymentMethodDetailsCardholderNameET);
    cardNumber = (EditText) findViewById(R.id.enterPaymentMethodDetailsNumberET);
    expiryDate = (EditText) findViewById(R.id.enterPaymentMethodDetailsExpiryET);
    cvv = (EditText) findViewById(R.id.enterPaymentMethodDetailsCvvET);
    usePaymentDetailsBTN = (Button) findViewById(R.id.enterPaymentMethodDetailsContinueBTN);
    paymentProgressSpinner = (ProgressWheel) findViewById(R.id.progressWheel);
    progressOverlay = (FrameLayout) findViewById(R.id.progress_overlay);

    usePaymentDetailsBTN.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        parseCardExpiryDate();
        Card cardToAdd = new Card(cardNumber.getText().toString(),
            Integer.valueOf(expiryMonth),
            Integer.valueOf(expiryYear),
            cvv.getText().toString());
        if(!cardToAdd.validateCard()) {
          Toast.makeText(getApplicationContext(), "Card Details Invalid!", Toast.LENGTH_LONG).show();
        }
        startSpinnerOverlay();
        stripeProcess(cardToAdd);
      }
    });
  }

  private void stripeProcess(Card cardToAdd){
    Stripe stripe = new Stripe(this, "pk_test_kwfy65ynBeJFLDiklvYHV2tI00fUxcehhP");
    stripe.createToken(
        cardToAdd,
        new TokenCallback() {
          public void onSuccess(Token token) {
            setUpTokenToServerCall(new StripeCharge(token.getId(), amountToDeposit, userId, groupAccountId));
          }
          public void onError(Exception error) {
            // Show localized error message
            Log.e("Stripe Error on Token: ", error.getLocalizedMessage());
          }
        }
    );
  }

  private void startSpinnerOverlay() {
    progressOverlay.setBackground(new ColorDrawable(Color.TRANSPARENT));
    progressOverlay.setVisibility(View.VISIBLE);
    paymentProgressSpinner.spin();
    paymentProgressSpinner.setSpinSpeed(0.75f);
  }

  private void stopSpinnerOverlay() {
    progressOverlay.setVisibility(View.INVISIBLE);
    paymentProgressSpinner.stopSpinning();
  }

  private void setUpTokenToServerCall(StripeCharge stripeCharge){
    Retrofit sendToken = new Retrofit.Builder()
        .baseUrl(LOCALHOST_SERVER_BASEURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    cardManagerApiInterface = sendToken.create(CardManagerAPI.class);
    sendTokenToServer(stripeCharge);
  }

  private void sendTokenToServer(StripeCharge stripeCharge) {
    Call<StripeChargeReceipt> call = cardManagerApiInterface.sendStripeTokenToServer(stripeCharge);
    call.enqueue(new Callback<StripeChargeReceipt>() {
      @Override
      public void onResponse(Call<StripeChargeReceipt> call, Response<StripeChargeReceipt> response) {
        stopSpinnerOverlay();
        if(response.body().getAmountPaid() != 0L &&
            response.body().getFailureCode() == null) {
          Intent groupAccountDetailed = new Intent(EnterPaymentMethodDetails.this, GroupAccountDetailed.class);
          groupAccountDetailed.putExtra("groupAccountId", groupAccountId);
          startActivity(groupAccountDetailed);
          finish();
        } else {
          Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
        }
      }
      @Override
      public void onFailure(Call<StripeChargeReceipt> call, Throwable t) {

      }
    });
  }

  public void onScanPress(View v) {
    Intent scanIntent = new Intent(this, CardIOActivity.class);

    // customize these values to suit your needs.
    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

    // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
    startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
  }

  private void parseCardExpiryDate() {
    String[] expiryDateSegments = expiryDate.getText().toString().split("/"); //App will crash if no / exists
    expiryMonth = expiryDateSegments[0];
    expiryYear = "20" + expiryDateSegments[1];
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MY_SCAN_REQUEST_CODE) {
      String cardNumberResultStr, cardExpiryResultStr;
      if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
        CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

        // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
        cardNumberResultStr = scanResult.getRedactedCardNumber();


        cardNumber.setText(cardNumberResultStr);
        if(scanResult.getCardType().toString().equals("American Express")) {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_amex_icon, 0, 0, 0);
        } else if(scanResult.getCardType().toString().equals("Discover")) {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_discover_icon, 0, 0, 0);
        } else if(scanResult.getCardType().toString().equals("Visa")) {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_visa_icon, 0, 0, 0);
        } else if(scanResult.getCardType().toString().equals("MasterCard")) {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_mastercard_icon, 0, 0, 0);
        } else if(scanResult.getCardType().toString().equals("Maestro")) {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_maestro_icon, 0, 0, 0);
        } else {
          cardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_card_cardtype_generic_icon, 0, 0, 0);
        }

        if (scanResult.isExpiryValid()) {
          cardExpiryResultStr = scanResult.expiryMonth + "/" + scanResult.expiryYear;
          expiryDate.setText(cardExpiryResultStr);
        }

        if (scanResult.cvv != null) {
          // Never log or display a CVV
          //resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
        }

        if (scanResult.postalCode != null) {
          //resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
        }
      }
      else {
        //resultDisplayStr = "Scan was canceled.";
      }
      // do something with resultDisplayStr, maybe display it in a textView
      // resultTextView.setText(resultDisplayStr);
    }
    // else handle other activity results
  }

  public void setUpActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.enterPaymentMethodDetailsToolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      LayoutInflater inflator = LayoutInflater.from(this);
      View v = inflator.inflate(R.layout.generic_titleview, null);

      ((TextView) v.findViewById(R.id.title)).setText(R.string.toolbar_enter_payment_details_title);
      ((TextView) v.findViewById(R.id.title)).setTextSize(20);

      this.getSupportActionBar().setCustomView(v);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}