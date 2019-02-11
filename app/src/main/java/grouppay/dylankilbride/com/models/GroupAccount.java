package grouppay.dylankilbride.com.models;

import android.media.Image;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class GroupAccount {

  private long accountId;
  private long adminId;
  //TODO Remove testResource in place for users profile image - retrieve from s3 bucket
  private int testResourceId;
  private String accountName;
  private String accountDescription;
  private int numberOfMembers;
  private BigDecimal totalAmountOwed;
  private BigDecimal totalAmountPaid;
  private List<Payments> paymentLog;
  private BigDecimal paymentProgress = new BigDecimal(0);

  public GroupAccount(long accountId, int testResourceId, String accountName, String accountDescription, int numberOfMembers, BigDecimal totalAmountPaid, BigDecimal totalAmountOwed, List<Payments> paymentLog) {
    this.accountId = accountId;
    this.testResourceId = testResourceId;
    this.accountName = accountName;
    this.accountDescription = accountDescription;
    this.numberOfMembers = numberOfMembers;
    this.totalAmountPaid = totalAmountPaid;
    this.totalAmountOwed = totalAmountOwed;
    this.paymentLog = paymentLog;
  }

  /**For creating a basic account**/
  public GroupAccount(long adminId, String accountName, String accountDescription, BigDecimal totalAmountOwed){
    this.adminId = adminId;
    this.accountName = accountName;
    this.accountDescription = accountDescription;
    this.totalAmountOwed = totalAmountOwed;
  }

  public List<Payments> getPaymentLog() {
    return paymentLog;
  }

  public void setPaymentLog(List<Payments> paymentLog) {
    this.paymentLog = paymentLog;
  }

  public int getTestResourceId() {
    return testResourceId;
  }

  public void setTestResourceId(int testResourceId) {
    this.testResourceId = testResourceId;
  }

  public GroupAccount() {}

  public long getAccountId() {
    return accountId;
  }

  public String getAccountName() {
    return accountName;
  }

  public String getAccountDescription() {
    return accountDescription;
  }

  public int getNumberOfMembers() {
    return numberOfMembers;
  }

  public BigDecimal getTotalAmountOwed() {
    return totalAmountOwed;
  }

  public BigDecimal getTotalAmountPaid() {
    return totalAmountPaid;
  }

  public String getAccountFinanceString(){
    //DecimalFormat decimalFormat = new DecimalFormat("#.##");
    String amountPaid = String.valueOf(totalAmountPaid);
    String amountOwed = String.valueOf(totalAmountOwed);
    return "€" + amountPaid + " of €" + amountOwed;
  }

  public String getNumberOfMembersString(){
    String noOfMembers = String.valueOf(numberOfMembers);
    return noOfMembers + " Members";
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public void setAccountDescription(String accountDescription) {
    this.accountDescription = accountDescription;
  }

  public void setNumberOfMembers(int numberOfMembers) {
    this.numberOfMembers = numberOfMembers;
  }

  public void setTotalAmountOwed(BigDecimal totalAmountOwed) {
    this.totalAmountOwed = totalAmountOwed;
  }

  public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
    this.totalAmountPaid = totalAmountPaid;
  }

  public String getPaymentProgress(BigDecimal paymentValue) {
    paymentProgress = paymentProgress.add(paymentValue);
    return String.valueOf(paymentProgress);
  }
}
