package grouppay.dylankilbride.com.retrofit_interfaces;

import java.util.List;

import grouppay.dylankilbride.com.models.GroupAccount;
import grouppay.dylankilbride.com.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GroupAccountAPI {

  @PUT("groupAccounts/createBasicAccount")
  Call<GroupAccount> createBasicAccount(@Body GroupAccount groupAccount);

  @POST("groupAccounts/addUsersToAccount/{groupAccountId}")
  Call<List<User>> addUsersToAccount(@Path("groupAccountId") String groupId, @Body List<User> userList);
}
