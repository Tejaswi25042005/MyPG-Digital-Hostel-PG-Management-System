package com.srikanta.mypg;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.srikanta.mypg.helpers.SubscriptionPurchaseHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SubscriptionPlansActivity extends AppCompatActivity
        implements PaymentResultListener {

    private String hostelId;

    private TextView tvCurrentPlan, tvExpiry;
    private TextView tvMonthlyPrice, tvQuarterPrice, tvHalfPrice, tvYearPrice;

    private long monthly = 0, quarterly = 0, half = 0, yearly = 0;

    // payment temp
    private String selectedPlan;
    private int selectedDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription_plans);

        hostelId = getIntent().getStringExtra("hostelId");

        if (hostelId == null) {
            Toast.makeText(this, "Invalid hostel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Checkout.preload(getApplicationContext());

        initViews();
        loadCurrentPlan();
        loadPrices();
    }

    private void initViews() {
        tvCurrentPlan = findViewById(R.id.tvCurrentPlan);
        tvExpiry = findViewById(R.id.tvExpiry);

        tvMonthlyPrice = findViewById(R.id.tvMonthlyPrice);
        tvQuarterPrice = findViewById(R.id.tvQuarterPrice);
        tvHalfPrice = findViewById(R.id.tvHalfPrice);
        tvYearPrice = findViewById(R.id.tvYearPrice);
    }

    // ================= LOAD CURRENT =================
    private void loadCurrentPlan() {

        FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("subscription")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        tvCurrentPlan.setText("Not Active");
                        tvExpiry.setText("Valid till: --");
                        return;
                    }

                    String plan = snapshot.child("plan").getValue(String.class);
                    String end = snapshot.child("endDate").getValue(String.class);

                    tvCurrentPlan.setText(plan == null ? "Not Active" : plan);
                    tvExpiry.setText("Valid till: " + (end == null ? "--" : end));
                });
    }

    // ================= LOAD PRICE =================
    private void loadPrices() {

        FirebaseDatabase.getInstance()
                .getReference("SubscriptionPrices")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) return;

                    monthly = snapshot.child("monthly").getValue(Long.class) == null ? 0 :
                            snapshot.child("monthly").getValue(Long.class);

                    quarterly = snapshot.child("quarterly").getValue(Long.class) == null ? 0 :
                            snapshot.child("quarterly").getValue(Long.class);

                    half = snapshot.child("halfYearly").getValue(Long.class) == null ? 0 :
                            snapshot.child("halfYearly").getValue(Long.class);

                    yearly = snapshot.child("yearly").getValue(Long.class) == null ? 0 :
                            snapshot.child("yearly").getValue(Long.class);

                    tvMonthlyPrice.setText("₹" + monthly);
                    tvQuarterPrice.setText("₹" + quarterly);
                    tvHalfPrice.setText("₹" + half);
                    tvYearPrice.setText("₹" + yearly);

                    clickListeners();
                });
    }

    // ================= BUY =================
    private void clickListeners() {

        findViewById(R.id.btnBuyMonthly)
                .setOnClickListener(v -> openRazorpay("MONTHLY", monthly, 30));

        findViewById(R.id.btnBuyQuarter)
                .setOnClickListener(v -> openRazorpay("QUARTERLY", quarterly, 90));

        findViewById(R.id.btnBuyHalf)
                .setOnClickListener(v -> openRazorpay("HALF_YEARLY", half, 180));

        findViewById(R.id.btnBuyYear)
                .setOnClickListener(v -> openRazorpay("YEARLY", yearly, 365));
    }

    // ================= OPEN PAYMENT =================
    private void openRazorpay(String plan, long amount, int days) {

        if (amount <= 0) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedPlan = plan;
        selectedDays = days;

        Checkout checkout = new Checkout();
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID); // 🔴 replace

        try {
            JSONObject options = new JSONObject();

            options.put("name", "MyPG");
            options.put("description", plan + " Subscription");
            options.put("currency", "INR");

            // Razorpay takes amount in paise
            options.put("amount", amount * 100);

            checkout.open(this, options);

        } catch (Exception e) {
            Toast.makeText(this, "Payment error", Toast.LENGTH_SHORT).show();
        }
    }

    // ================= SUCCESS =================
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("subscription");

        Calendar today = Calendar.getInstance();

        String start = new SimpleDateFormat(
                "ddMMyyyy", Locale.getDefault()).format(today.getTime());

        today.add(Calendar.DAY_OF_YEAR, selectedDays);

        String end = new SimpleDateFormat(
                "ddMMyyyy", Locale.getDefault()).format(today.getTime());

        ref.child("plan").setValue(selectedPlan);
        ref.child("startDate").setValue(start);
        ref.child("endDate").setValue(end);
        ref.child("status").setValue("active");
        ref.child("paymentId").setValue(razorpayPaymentID);

        Toast.makeText(this, "Subscription Activated", Toast.LENGTH_LONG).show();
        finish();
    }

    // ================= FAILURE =================
    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
    }
}
