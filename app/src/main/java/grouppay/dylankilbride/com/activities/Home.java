package grouppay.dylankilbride.com.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import grouppay.dylankilbride.com.adapters.ActiveAccountsRVAdapter;
import grouppay.dylankilbride.com.adapters.ItemClickListener;
import grouppay.dylankilbride.com.grouppay.R;
import grouppay.dylankilbride.com.models.Cards;
import grouppay.dylankilbride.com.models.GroupAccount;
import grouppay.dylankilbride.com.models.User;
import grouppay.dylankilbride.com.retrofit_interfaces.GroupAccountAPI;
import grouppay.dylankilbride.com.retrofit_interfaces.ProfileAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static grouppay.dylankilbride.com.constants.Constants.LOCALHOST_SERVER_BASEURL;

public class Home extends AppCompatActivity implements ItemClickListener {

  public ActiveAccountsRVAdapter adapter;
  List<GroupAccount> groupAccounts = new ArrayList<>();
  private RecyclerView accountsRecyclerView;
  private RecyclerView.LayoutManager accountsRecyclerViewLayoutManager;
  private TextView noAccountsTextView, navName, navEmail;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private NavigationView navigationView;
  private SwipeRefreshLayout pullToRefresh;
  private String userId, userName, userEmail, profileImgUrl;
  private int numberOfActiveGroups;
  private GroupAccountAPI apiInterface;
  private ImageView navDrawerProfileImage, noAccountsImgView;
  private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 152;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    userId = getIntent().getStringExtra("userId");

    setUpFAB();
    setUpAccountPreviewRecyclerView();
    //noAccountsTextView = (TextView) findViewById(R.id.noAccountPreviewsTextView);

    pullToRefresh = findViewById(R.id.homePullToRefresh);
    pullToRefresh.setColorSchemeResources(R.color.colorAccent);
    pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        setUpAssociatedAccountsCall(userId);
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
           pullToRefresh.setRefreshing(false);
          }
        },4000);
      }
    });

    checkStoragePermissions();

    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_close);

    drawerLayout.addDrawerListener(actionBarDrawerToggle);
    actionBarDrawerToggle.syncState();

    setUpActionBar();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void setUpNavDrawer() {
    navigationView = (NavigationView) findViewById(R.id.navView);
    View headerView = navigationView.getHeaderView(0);
    navName = (TextView) headerView.findViewById(R.id.navName);
    navEmail = (TextView) headerView.findViewById(R.id.navEmail);
    navName.setText(userName);
    navEmail.setText(userEmail);
    navDrawerProfileImage = (ImageView)headerView.findViewById(R.id.navDrawerProfileImage);
    navDrawerProfileImage.setOnClickListener(view -> {
      Intent profileIntent = new Intent(Home.this, Profile.class);
      profileIntent.putExtra("userId", userId);
      startActivityForResult(profileIntent, 100);
    });
    if(profileImgUrl != null) {
      Glide.with(navDrawerProfileImage.getContext())
          .load(profileImgUrl)
          .into(navDrawerProfileImage);
    } else {
      navDrawerProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.no_profile_photo));
    }
    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
          case R.id.nav_profile:
            Intent profileIntent = new Intent(Home.this, Profile.class);
            profileIntent.putExtra("userId", userId);
            startActivityForResult(profileIntent, 100);
            break;
          case R.id.nav_cards:
            Intent intentPaymentMethods = new Intent(Home.this, PaymentMethods.class);
            intentPaymentMethods.putExtra("userIdStr", userId);
            startActivityForResult(intentPaymentMethods, 120);
            break;
          case R.id.nav_transactions:
            Intent intentTransactions = new Intent(Home.this, AllTransactions.class);
            intentTransactions.putExtra("userIdStr", userId);
            intentTransactions.putExtra("numberOfGroups", String.valueOf(groupAccounts.size()));
            startActivity(intentTransactions);
            break;
          case R.id.nav_logout:
            FirebaseAuth.getInstance().signOut();
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
          groupAccounts.clear();
          setUpAccountPreviewRecyclerView();
          checkForEmptyAccountsList();
        } else {
          if(response.body().size() > 0 && !response.body().equals("null")){
            groupAccounts.clear();
            for(int i=0; i<response.body().size(); i++) {
              GroupAccount groupAccount = new GroupAccount(response.body().get(i).getGroupAccountId(),
                  response.body().get(i).getAccountName(),
                  response.body().get(i).getAccountDescription(),
                  response.body().get(i).getNumberOfMembers(),
                  response.body().get(i).getTotalAmountOwed(),
                  response.body().get(i).getTotalAmountPaid(),
                  response.body().get(i).getGroupImage());
              groupAccounts.add(groupAccount);
            }
          }
          if (pullToRefresh.isRefreshing()) {
            pullToRefresh.setRefreshing(false);
          }
        }
        //emptyRVTextViewSetUp(checkIfListIsEmpty(groupAccounts));
        adapter.notifyDataSetChanged();
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
  public void onItemClick(User contact) {

  }

  public void getProifleDetails() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(LOCALHOST_SERVER_BASEURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    final ProfileAPI profileAPI = retrofit.create(ProfileAPI.class); //Creates model for JSON
    Call<User> call = profileAPI.getUserDetails(userId);
    call.enqueue(new Callback<User>() { //Don't use execute as it will execute on main thread
      @Override
      public void onResponse(Call<User> call, Response<User> response) {
        if (!response.isSuccessful()) {
          //TODO Add error display message for user
        } else {
          userEmail = response.body().getEmailAddress();
          userName = response.body().getFirstName();
          profileImgUrl = response.body().getProfileUrl();
          setUpNavDrawer();
        }
      }

      @Override
      public void onFailure(Call<User> call, Throwable t) {
        //TODO Do something here
      }
    });
  }

  @Override
  public void onItemClick(GroupAccount groupAccount) {
    Intent viewDetailedInfo = new Intent(Home.this, GroupAccountDetailed.class);
    viewDetailedInfo.putExtra("groupAccountId", Long.toString(groupAccount.getGroupAccountId()));
    viewDetailedInfo.putExtra("userIdStr", userId);
    viewDetailedInfo.putExtra("groupName",groupAccount.getAccountName());
    startActivity(viewDetailedInfo);
  }

  @Override
  public void onItemClick(Cards card) {

  }

  @Override
  protected void onResume() {
    super.onResume();
    setUpAssociatedAccountsCall(userId);
    getProifleDetails();
    groupAccounts.clear();
  }

  public void checkForEmptyAccountsList() {
    if (groupAccounts.isEmpty()) {
      accountsRecyclerView.setVisibility(View.GONE);
      noAccountsImgView.setVisibility(View.VISIBLE);
      noAccountsTextView.setVisibility(View.VISIBLE);
    } else {
      accountsRecyclerView.setVisibility(View.VISIBLE);
      noAccountsTextView.setVisibility(View.GONE);
      noAccountsImgView.setVisibility(View.GONE);
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100) {
      if(resultCode == RESULT_OK) {
        profileImgUrl = data.getStringExtra("profileUrl");
        setUpNavDrawer();
      }
    }
  }

  private void checkStoragePermissions() {
    if (ContextCompat.checkSelfPermission(Home.this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

      if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
          Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      } else {
        ActivityCompat.requestPermissions(Home.this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            STORAGE_PERMISSIONS_REQUEST_CODE);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
      }
    } else {
      // Permission has already been granted
    }
  }
}