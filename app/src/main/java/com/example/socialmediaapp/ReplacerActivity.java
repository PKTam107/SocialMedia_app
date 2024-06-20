package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialmediaapp.fragments.Comment;

public class ReplacerActivity extends AppCompatActivity {
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replacer);

        frameLayout = findViewById(R.id.frameLayout);

        Intent intent = getIntent();
        if (intent != null) {
            boolean isComment = intent.getBooleanExtra("isComment", false);
            if (isComment) {
                String id = intent.getStringExtra("id");
                String uid = intent.getStringExtra("uid");
                setCommentFragment(id, uid);
            } else {
                // Nếu không phải là bình luận, chuyển sang màn hình đăng nhập
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }
    }

    private void setCommentFragment(String id, String uid) {
        Comment fragment = new Comment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("uid", uid);
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }

}
