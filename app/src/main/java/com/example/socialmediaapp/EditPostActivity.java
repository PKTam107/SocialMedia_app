package com.example.socialmediaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPostActivity extends AppCompatActivity {
    private EditText editDescription;
    private Button btnSave;
    private FirebaseFirestore firestore;

    private String postId;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        if (getIntent().hasExtra("userId")) {
            userId = getIntent().getStringExtra("userId");
        } else {
            // Nếu không có userId, kết thúc activity
            finish();
        }

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // Ánh xạ views
        editDescription = findViewById(R.id.edit_description);
        btnSave = findViewById(R.id.btn_save);

        // Lấy postId từ Intent
        if (getIntent().hasExtra("postId")) {
            postId = getIntent().getStringExtra("postId");
        } else {
            // Nếu không có postId, kết thúc activity
            finish();
        }

        // Lấy nội dung hiện tại của bài đăng và hiển thị trong EditText
        getCurrentDescription();

        // Thiết lập sự kiện click cho nút Lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy nội dung đã chỉnh sửa
                String newDescription = editDescription.getText().toString().trim();

                // Kiểm tra nội dung mới có rỗng hay không
                if (!TextUtils.isEmpty(newDescription)) {
                    // Cập nhật nội dung mới vào Firestore
                    updatePostDescription(newDescription);
                } else {
                    // Hiển thị thông báo khi nội dung rỗng
                    Toast.makeText(EditPostActivity.this, "Please enter description", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getCurrentDescription() {
        // Truy vấn Firestore để lấy nội dung của bài đăng hiện tại
        DocumentReference postRef = firestore.collection("Users")
                .document(userId) // userId của người đăng bài
                .collection("Post Images")
                .document(postId); // postId của bài đăng

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Nếu tài liệu tồn tại, lấy nội dung và hiển thị trong EditText
                String currentDescription = documentSnapshot.getString("description");
                editDescription.setText(currentDescription);
            }
        }).addOnFailureListener(e -> {
            // Xử lý khi không thể lấy nội dung của bài đăng
        });
    }

    // Phương thức này cập nhật nội dung mới của bài đăng vào Firestore
    private void updatePostDescription(String newDescription) {
        // Truy vấn Firestore để cập nhật nội dung mới của bài đăng
        DocumentReference postRef = firestore.collection("Users")
                .document(userId) // userId của người đăng bài
                .collection("Post Images")
                .document(postId); // postId của bài đăng

        postRef.update("description", newDescription)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật thành công
                    Toast.makeText(EditPostActivity.this, "Description updated successfully", Toast.LENGTH_SHORT).show();
                    // Kết thúc activity

                    editDescription.setText(newDescription);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi cập nhật thất bại
                    Toast.makeText(EditPostActivity.this, "Failed to update description", Toast.LENGTH_SHORT).show();
                });
    }
}