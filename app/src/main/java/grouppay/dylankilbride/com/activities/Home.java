package grouppay.dylankilbride.com.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import grouppay.dylankilbride.com.adapters.ActiveAccountsRVAdapter;
import grouppay.dylankilbride.com.adapters.ItemClickListener;
import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.models.Contact;
import grouppay.dylankilbride.com.models.GroupAccount;
import grouppay.dylankilbride.com.retrofit_interfaces.GroupAccountAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static grouppay.dylankilbride.com.constants.Constants.LOCALHOST_SERVER_BASEURL;

public class Home extends AppCompatActivity implements ItemClickListener {

  ActiveAccountsRVAdapter adapter;
  List<GroupAccount> groupAccounts = new ArrayList<>();
  private RecyclerView accountsRecyclerView;
  private RecyclerView.LayoutManager accountsRecyclerViewLayoutManager;
  private TextView noAccountsTextView, navName, navEmail;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private NavigationView navigationView;
  private String userId, userName, userEmail;
  private GroupAccountAPI apiInterface;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    userId = getIntent().getStringExtra("userId");
    userName = getIntent().getStringExtra("name");
    userEmail = getIntent().getStringExtra("email");
    setUpAssociatedAccountsCall(userId);

    noAccountsTextView= (TextView) findViewById(R.id.noAccountPreviewsTextView);

    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_close);

    drawerLayout.addDrawerListener(actionBarDrawerToggle);
    actionBarDrawerToggle.syncState();
//    groupAccounts.add(new GroupAccount(1, R.drawable.human_photo, "Pas De Casa", "Quick Hol", 3, new BigDecimal("47.23"), new BigDecimal("2500"), null));
//    groupAccounts.add(new GroupAccount(2, R.drawable.human_photo, "Dinner Today", "Quick Hol", 14, new BigDecimal("4"), new BigDecimal("25"), null));
//    groupAccounts.add(new GroupAccount(3, R.drawable.human_photo, "Monday", "Quick Hol", 5, new BigDecimal("56.70"), new BigDecimal("314"), null));
//    groupAccounts.add(new GroupAccount(4, R.drawable.human_photo, "Car", "Quick Hol", 2, new BigDecimal("0"), new BigDecimal("100"), null)
    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    navigationView = (NavigationView) findViewById(R.id.navView);
    View headerView = navigationView.getHeaderView(0);
    navName = (TextView) headerView.findViewById(R.id.navName);
    navEmail = (TextView) headerView.findViewById(R.id.navEmail);
    navName.setText(userName);
    navEmail.setText(userEmail);
  }

  private void setUpNavDrawer() {
    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
          case R.id.nav_profile:
            Intent profileIntent = new Intent(Home.this, Profile.class);
            profileIntent.putExtra("userId", userId);
            startActivity(profileIntent);
            break;
          case R.id.nav_cards:
            Intent intentPaymentMethods = new Intent(Home.this, PaymentMethods.class);
            startActivity(intentPaymentMethods);
            break;
          case R.id.nav_logout:
            Intent intentLogin = new Intent(Home.this, Login.class);
            startActivity(intentLogin);
            break;
          default:
            return true;
        }
        return true;
      }
    });
  }

  private void setUpFAB() {
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddAccount);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Home.this, CreateGroupAccountStage1.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
      }
    });
  }

  public void setUpAccountPreviewRecyclerView() {
    // set up the RecyclerView
    accountsRecyclerView = (RecyclerView) findViewById(R.id.rvAccountsPreview);
    accountsRecyclerViewLayoutManager = new LinearLayoutManager(this);
    accountsRecyclerView.setLayoutManager(accountsRecyclerViewLayoutManager);
    adapter = new ActiveAccountsRVAdapter(groupAccounts, R.layout.activity_home_preview_list_item, this);
    accountsRecyclerView.setAdapter(adapter);
    adapter.setOnClick(Home.this);
  }

  public void setUpActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.homeToolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      LayoutInflater inflator = LayoutInflater.from(this);
      View v = inflator.inflate(R.layout.generic_titleview, null);

      ((TextView) v.findViewById(R.id.title)).setText(R.string.toolbar_home_title);
      ((TextView) v.findViewById(R.id.title)).setTextSize(20);

      this.getSupportActionBar().setCustomView(v);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(actionBarDrawerToggle.onOptionsItemSelected(item))
      return true;
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    actionBarDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    actionBarDrawerToggle.onConfigurationChanged(newConfig);
  }

  public boolean checkIfListIsEmpty(List<GroupAccount> list) {
    if (list.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  public void emptyRVTextViewSetUp(boolean listEmpty) {
    if (listEmpty == true) {
      accountsRecyclerView.setVisibility(View.GONE);
      noAccountsTextView.setVisibility(View.VISIBLE);
    } else {
      accountsRecyclerView.setVisibility(View.VISIBLE);
      noAccountsTextView.setVisibility(View.GONE);
    }
  }

  public void setUpAssociatedAccountsCall(String userId) {
    Retrofit getAssociatedAccounts = new Retrofit.Builder()
        .baseUrl(LOCALHOST_SERVER_BASEURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    apiInterface = getAssociatedAccounts.create(GroupAccountAPI.class);
    getUserAssociatedAccounts(userId);
  }

  public void getUserAssociatedAccounts(String userId){
    Call<List<GroupAccount>> call = apiInterface.getUserAssociatedAccounts(userId);
    call.enqueue(new Callback<List<GroupAccount>>() {
      @Override
      public void onResponse(Call<List<GroupAccount>> call, Response<List<GroupAccount>> response) {
        if(!response.isSuccessful()) {
          setUpFAB();
          setUpNavDrawer();
          setUpAccountPreviewRecyclerView();
          emptyRVTextViewSetUp(checkIfListIsEmpty(groupAccounts));
        } else {
          if(response.body().size() > 0){
            for(int i=0; i<response.body().size(); i++) {
              GroupAccount groupAccount = new GroupAccount(response.body().get(i).getGroupAccountId(),
                  response.body().get(i).getAccountName(),
                  response.body().get(i).getAccountDescription(),
                  response.body().get(i).getNumberOfMembers(),
                  response.body().get(i).getTotalAmountPaid(),
                  response.body().get(i).getTotalAmountOwed(),
                  R.drawable.human_photo);
              groupAccounts.add(groupAccount);
            }
          }
        }
        setUpFAB();
        setUpNavDrawer();
        setUpAccountPreviewRecyclerView();
        emptyRVTextViewSetUp(checkIfListIsEmpty(groupAccounts));
      }

      @Override
      public void onFailure(Call<List<GroupAccount>> call, Throwable t) {

      }
    });
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }

  @Override
  public void onItemClick(Contact contact) {

  }

  @Override
  public void onItemClick(GroupAccount groupAccount) {
    Intent viewDetailedInfo = new Intent(Home.this, GroupAccountDetailed.class);
    viewDetailedInfo.putExtra("groupAccountId", groupAccount.getGroupAccountId());
    startActivity(viewDetailedInfo);
  }
}