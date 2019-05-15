package grouppay.dylankilbride.com.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.text_watchers.CurrencyTextWatcher;
import grouppay.dylankilbride.com.text_watchers.DecimalDigitsInputFilter;

public class DepositMoneyToGroup extends AppCompatActivity {

  String groupAccountId, groupAccountName, userId, activityHeader;
  EditText amountToPay;
  Button continueBtn;
  TextView depositMoneyToGroupHeader;
  private String numberOfParticipants, totalAmountOwed;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_deposit_money_to_group);

    groupAccountId = getIntent().getStringExtra("groupAccountIdStr");
    groupAccountName = getIntent().getStringExtra("groupAccountName");
    userId = getIntent().getStringExtra("userIdStr");
    numberOfParticipants = getIntent().getStringExtra("numberOfParticipants");
    totalAmountOwed = getIntent().getStringExtra("totalOwed");

    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    continueBtn = (Button) findViewById(R.id.depositMoneyToGroupBTN);
    depositMoneyToGroupHeader = (TextView) findViewById(R.id.depositMoneyToGroupTitle);
    activityHeader = getResources().getString(R.string.deposit_amount_to_group_header) + " " + groupAccountName;
    depositMoneyToGroupHeader.setText(activityHeader);
    amountToPay = (EditText) findViewById(R.id.depositMoneyToGroupAmountET);

    continueBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent enterPaymentDetails = new Intent(DepositMoneyToGroup.this, EnterPaymentMethodDetails.class);
        enterPaymentDetails.putExtra("amountToDepositStr", amountToPay.getText().toString());
        enterPaymentDetails.putExtra("userIdStr", userId);
        enterPaymentDetails.putExtra("groupAccountIdStr", groupAccountId);
        startActivity(enterPaymentDetails);
        finish();
      }
    });

    amountToPay.addTextChangedListener(new CurrencyTextWatcher(amountToPay));
    amountToPay.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
    setAmountMaxLength(18);
  }

  private String calculateAmountToPay(String noOfParticipants, String owed) {
    double amountOwed = Double.parseDouble(owed);
    int noOfMembers = Integer.parseInt(noOfParticipants);
    DecimalFormat amountFormat = new DecimalFormat("#.##");
    amountFormat.setRoundingMode(RoundingMode.CEILING);
    return amountFormat.format((amountOwed/(double)noOfMembers) / 100);
  }

  public void setAmountMaxLength(int length) {
    InputFilter[] filterArray = new InputFilter[1];
    filterArray[0] = new InputFilter.LengthFilter(length);
    amountToPay.setFilters(filterArray);
  }

  public void setUpActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.depositMoneyToGroupToolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      LayoutInflater inflator = LayoutInflater.from(this);
      View v = inflator.inflate(R.layout.generic_titleview, null);

      ((TextView) v.findViewById(R.id.title)).setText(R.string.toolbar_deposit_money_to_group_title);
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

  @Override
  protected void onResume() {
    super.onResume();
    amountToPay.setText(calculateAmountToPay(numberOfParticipants, totalAmountOwed));
  }
}
