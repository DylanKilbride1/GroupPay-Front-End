package grouppay.dylankilbride.com.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.models.StripeCharge;
import grouppay.dylankilbride.com.models.StripeChargeReceipt;
import grouppay.dylankilbride.com.retrofit_interfaces.CardManagerAPI;
import grouppay.dylankilbride.com.text_watchers.CardExpiryDateTextWatcher;
import grouppay.dylankilbride.com.text_watchers.CardNumberTextWatcher;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static grouppay.dylankilbride.com.constants.Constants.LOCALHOST_SERVER_BASEURL;

public class AddPaymentMethod extends AppCompatActivity {

  public final int MY_SCAN_REQUEST_CODE = 1234;
  private EditText cardholderName, cardNumber, expiryDate, cvv;
  private Button addPaymentMethodContinueBTN;
  private String expiryMonth, expiryYear, userId;
  private CardManagerAPI cardManagerApiInterface;
  private ProgressBar addPaymentMethodPB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_payment_method);

    userId = getIntent().getStringExtra("userIdStr");

    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    cardholderName = (EditText) findViewById(R.id.addCardCardholderNameET);
    cardNumber = (EditText) findViewById(R.id.addCardNumberET);
    expiryDate = (EditText) findViewById(R.id.addCardExpiryET);
    cvv = (EditText) findViewById(R.id.addCardCvvET);
    addPaymentMethodContinueBTN = (Button) findViewById(R.id.addPaymentMethodContinueBTN);
    disablePayButton();
    addPaymentMethodPB = findViewById(R.id.addPaymentMethodProgress);


   cardNumber.addTextChangedListener(new CardNumberTextWatcher(cardNumber));
   expiryDate.addTextChangedListener(new CardExpiryDateTextWatcher());

    //For validating all fields hold some value
    cardNumber.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        checkForValidFieldEntries();
      }
    });

    cardholderName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        checkForValidFieldEntries();
      }
    });

    expiryDate.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        checkForValidFieldEntries();
      }
    });

    cvv.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        checkForValidFieldEntries();
      }
    });

    addPaymentMethodContinueBTN.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        parseCardExpiryDate();
        addPaymentMethodPB.setVisibility(View.VISIBLE);
        Card cardToAdd = new Card(cardNumber.getText().toString(),
            Integer.valueOf(expiryMonth),
            Integer.valueOf(expiryYear),
            cvv.getText().toString());
        if(!cardToAdd.validateCard()) {
          addPaymentMethodPB.setVisibility(View.INVISIBLE);
          Toast.makeText(getApplicationContext(), "Card Details Invalid!", Toast.LENGTH_LONG).show();
        }
        stripeProcess(cardToAdd);
      }
    });
  }

  private void checkForValidFieldEntries() {
    if((!cardholderName.getText().toString().isEmpty())
        && (cardNumber.length() >= 15)
        && (expiryDate.length() >= 5)
        && (cvv.length() == 3)) {
      enablePayButton();
    } else {
      disablePayButton();
    }
  }

  private void enablePayButton() {
    addPaymentMethodContinueBTN.setEnabled(true);
    addPaymentMethodContinueBTN.setAlpha(1);
  }

  private void disablePayButton() {
    addPaymentMethodContinueBTN.setEnabled(false);
    addPaymentMethodContinueBTN.setAlpha(0.5f);
  }

  private void stripeProcess(Card cardToAdd){
    Stripe stripe = new Stripe(this, "pk_test_kwfy65ynBeJFLDiklvYHV2tI00fUxcehhP");
    stripe.createToken(
        cardToAdd,
        new TokenCallback() {
          public void onSuccess(Token token) {
            setUpTokenToServerCall(new StripeCharge(token.getId(), userId));
          }
          public void onError(Exception error) {
            Toast.makeText(AddPaymentMethod.this,
                "Error tokenizing card details",
                Toast.LENGTH_SHORT
            ).show();
            Log.e("Stripe Error on Token: ", error.getLocalizedMessage());
          }
        }
    );
  }

  public String parseOCRCardExpiryDate(int expiryMonth, int year) {
    String formattedExpMonth;
    String formattedExpYear = "";
    String formattedExpiryDate;
    if (Integer.toString(expiryMonth).length() == 1) {
      formattedExpMonth = "0" + expiryMonth;
    } else {
      formattedExpMonth = Integer.toString(expiryMonth);
    }
    if(Integer.toString(year).length() == 4) {
      formattedExpYear = Integer.toString(year).substring(2);
    } else {
      formattedExpYear = Integer.toString(year);
    }
    formattedExpiryDate = formattedExpMonth + "/" + formattedExpYear;
    return formattedExpiryDate;
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
    Call<Void> call = cardManagerApiInterface.saveCard(stripeCharge);
    call.enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.code() == 200) {
          addPaymentMethodPB.setVisibility(View.INVISIBLE);
          finish();
        } else {
          addPaymentMethodPB.setVisibility(View.INVISIBLE);
          Toast.makeText(getApplicationContext(), "We couldn't save your card for some strange reason!", Toast.LENGTH_LONG).show();
        }
      }
      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
        addPaymentMethodPB.setVisibility(View.INVISIBLE);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MY_SCAN_REQUEST_CODE) {
      String cardNumberResultStr, cardExpiryResultStr;
      if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
        CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

        // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
        cardNumberResultStr = scanResult.getFormattedCardNumber();


        cardNumber.setText(cardNumberResultStr);

        if (scanResult.isExpiryValid()) {
          cardExpiryResultStr = parseOCRCardExpiryDate(scanResult.expiryMonth, scanResult.expiryYear);
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

  private void parseCardExpiryDate() {
    String[] expiryDateSegments = expiryDate.getText().toString().split("/"); //App will crash if no / exists
    expiryMonth = expiryDateSegments[0];
    expiryYear = "20" + expiryDateSegments[1];
  }

  public void setUpActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.addPaymentMethodToolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      LayoutInflater inflator = LayoutInflater.from(this);
      View v = inflator.inflate(R.layout.generic_titleview, null);

      ((TextView) v.findViewById(R.id.title)).setText(R.string.toolbar_add_payment_method_title);
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
