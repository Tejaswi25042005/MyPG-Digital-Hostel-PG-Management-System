package com.srikanta.mypg.notices;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.R;
import com.srikanta.mypg.adapters.NoticeAdapter;
import com.srikanta.mypg.models.NoticeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeActivity extends AppCompatActivity {

    // ================= DATA =================
    private String hostelId;
    private DatabaseReference hostelRef;

    private NoticeAdapter adapter;
    private List<NoticeModel> noticeList = new ArrayList<>();
    private RecyclerView rvNotices;
    private FloatingActionButton fabCreateNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notice);

        // 1️⃣ Read hostelId safely
        hostelId = getIntent().getStringExtra("hostelId");

        if (hostelId == null) {
            Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        hostelRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Hostels")
                .child(hostelId);

        initViews();
        setupRecycler();
        loadNotices();
        setupClicks();
    }

    // ================= INIT =================
    private void initViews() {
        rvNotices = findViewById(R.id.rvNotices);
        fabCreateNotice = findViewById(R.id.fabCreateNotice);
    }

    // ================= RECYCLER =================
    private void setupRecycler() {
        rvNotices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoticeAdapter(noticeList, this::showNoticeDialog);
        rvNotices.setAdapter(adapter);
    }



    // ================= LOAD NOTICES =================
    private void loadNotices() {

        hostelRef.child("notices")
                .orderByChild("createdAt")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {

                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {

                        noticeList.clear();

                        if (!snapshot.exists()) {
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (com.google.firebase.database.DataSnapshot snap : snapshot.getChildren()) {

                            NoticeModel notice =
                                    snap.getValue(NoticeModel.class);

                            if (notice == null) continue;

                            if (!notice.active) continue;

                            noticeList.add(notice);
                        }

                        // latest first
                        java.util.Collections.reverse(noticeList);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {}
                });
    }


    // ================= CLICKS =================
    private void setupClicks() {

        fabCreateNotice.setOnClickListener(v -> showCreateNoticeDialog());
    }

    private void showCreateNoticeDialog() {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_create_notice, null);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etMessage = view.findViewById(R.id.etMessage);
        RadioGroup rgType = view.findViewById(R.id.rgType);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("CREATE", null)
                .setNegativeButton("CANCEL", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {

            Button btnCreate =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            btnCreate.setOnClickListener(v -> {

                String title = etTitle.getText().toString().trim();
                String message = etMessage.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Title required");
                    return;
                }

                if (message.isEmpty()) {
                    etMessage.setError("Message required");
                    return;
                }

                int checkedId = rgType.getCheckedRadioButtonId();
                String type;

                if (checkedId == R.id.rbPayment) {
                    type = "PAYMENT";
                } else if (checkedId == R.id.rbMaintenance) {
                    type = "MAINTENANCE";
                } else if (checkedId == R.id.rbFood) {
                    type = "FOOD";
                } else {
                    type = "GENERAL";
                }

                createNotice(title, message, type);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createNotice(
            String title,
            String message,
            String type
    ) {

        DatabaseReference noticesRef = hostelRef.child("notices");
        String noticeId = noticesRef.push().getKey();

        if (noticeId == null) {
            Toast.makeText(this, "Failed to create notice", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        Map<String, Object> notice = new HashMap<>();
        notice.put("noticeId", noticeId);
        notice.put("title", title);
        notice.put("message", message);
        notice.put("type", type);
        notice.put("active", true);
        notice.put("createdAt", now);

        noticesRef.child(noticeId)
                .setValue(notice)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Notice created", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showNoticeDialog(NoticeModel notice) {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_notice_view, null);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvType = view.findViewById(R.id.tvDialogType);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);

        tvTitle.setText(notice.title);
        tvType.setText(notice.type);
        tvMessage.setText(notice.message);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("CLOSE", null)
                .setNegativeButton("REMOVE", (d, w) ->
                        showDeleteConfirmDialog(notice.noticeId)
                )
                .create();

        dialog.show();
    }

    private void showDeleteConfirmDialog(String noticeId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Notice")
                .setMessage("This notice will be permanently deleted and cannot be recovered. Continue?")
                .setPositiveButton("DELETE", (d, w) ->
                        deleteNotice(noticeId)
                )
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void deleteNotice(String noticeId) {

        hostelRef.child("notices")
                .child(noticeId)
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Notice deleted", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


}
