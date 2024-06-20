package com.example.socialmediaapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.EditPostActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.ReplacerActivity;
import com.example.socialmediaapp.model.HomeModel;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewAdapter extends RecyclerView.Adapter<PostViewAdapter.PostViewHolder> {

    private final List<HomeModel> list;
    Activity context;
    OnPressed onPressed;

    public PostViewAdapter(List<HomeModel> list, Activity context) {
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        holder.userNameTv.setText(list.get(position).getName());
        holder.timeTv.setText("" + list.get(position).getTimestamp());

        List<String> likeList = list.get(position).getLikes();

        int count = likeList.size();

        if (count == 0) {
            holder.likeCountTv.setText("0 Like");
        } else if (count == 1) {
            holder.likeCountTv.setText(count + " Like");
        } else {
            holder.likeCountTv.setText(count + " Likes");
        }

        //check if already like
        assert user != null;
        holder.likeCheckBox.setChecked(likeList.contains(user.getUid()));

        holder.descriptionTv.setText(list.get(position).getDescription());

        Random random = new Random();

        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        fetchImageUrl(list.get(position).getUid(), holder);

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        holder.clickListener(position,
                list.get(position).getId(),
                list.get(position).getName(),
                list.get(position).getUid(),
                list.get(position).getLikes(),
                list.get(position).getImageUrl()
        );

        if (user != null && list.get(position).getUid().equals(user.getUid())) {
            // Hiển thị nút editPost nếu bài viết là của người dùng hiện tại
            holder.edtPost.setVisibility(View.VISIBLE);
        } else {
            // Ẩn nút editPost nếu bài viết không thuộc về người dùng hiện tại
            holder.edtPost.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked);
    }

    void fetchImageUrl(String uid, PostViewHolder holder) {

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot snapshot = task.getResult();

                        Glide.with(context.getApplicationContext())
                                .load(snapshot.getString("profileImage"))
                                .placeholder(R.drawable.ic_person)
                                .timeout(6500)
                                .into(holder.profileImage);

                    } else {
                        assert task.getException() != null;
                        Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView profileImage;
        private final TextView userNameTv;
        private final TextView timeTv;
        private final TextView likeCountTv;
        private final TextView descriptionTv;
        private final ImageView imageView;
        private final CheckBox likeCheckBox;
        private final ImageButton commentBtn;
        private final ImageButton shareBtn;
        private final ImageButton edtPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            imageView = itemView.findViewById(R.id.imageView);
            userNameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            likeCheckBox = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);
            edtPost = itemView.findViewById(R.id.editPost);
        }

        public void clickListener(final int position, final String id, String name, final String uid, final List<String> likes, final String imageUrl) {

            commentBtn.setOnClickListener(v -> {

                Intent intent = new Intent(context, ReplacerActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("uid", uid);
                intent.putExtra("isComment", true);

                context.startActivity(intent);

            });

            likeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                onPressed.onLiked(position, id, uid, likes, isChecked);
                // Update the like count TextView or similar UI element here
                updateLikeCount(likes.size());
            });

            shareBtn.setOnClickListener(v -> {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, imageUrl);
                intent.setType("text/*");
                context.startActivity(Intent.createChooser(intent, "Share link using..."));

            });

            edtPost.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, edtPost);
                popupMenu.getMenuInflater().inflate(R.menu.menu_edtpost, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.action_delete) {
                            DeletePost(getAdapterPosition());
                            return true;
                        }
                        if (itemId == R.id.action_edit) {
                            EditPost(getAdapterPosition());
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();

            });

        }

        private void EditPost(int position) {
            String userId = list.get(position).getUid(); // Lấy UID của người đăng bài
            String postId = list.get(position).getId();
            Intent intent = new Intent(context, EditPostActivity.class);
            intent.putExtra("userId", userId); // userId là id của người đăng bài
            intent.putExtra("postId", postId); // postId là id của bài đăng cần chỉnh sửa
            context.startActivity(intent);
        }

        private void DeletePost(int position) {
            String userId = list.get(position).getUid(); // Lấy UID của người đăng bài
            String postId = list.get(position).getId(); // Lấy ID của bài đăng cần xóa

            // Xóa bài đăng từ Firestore
            FirebaseFirestore.getInstance()
                    .collection("Users") // Truy cập collection "Users"
                    .document(userId) // Truy cập document của người đăng bài
                    .collection("Post Images") // Truy cập collection "Post Images" của người đăng bài
                    .document(postId) // Truy cập document của bài đăng cần xóa
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Xóa thành công, cập nhật giao diện người dùng
                        list.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> {
                        // Xóa thất bại, hiển thị thông báo hoặc xử lý lỗi tương ứng
                        Log.e("DeletePost", "Error deleting post: " + e.getMessage());
                    });
        }
        private void updateLikeCount(int likeCount) {
            if (likeCount == 0)
            {
                likeCountTv.setText(String.valueOf(likeCount) + " Like");
            } else if (likeCount == 1)
            {
                likeCountTv.setText(String.valueOf(likeCount) + " Like");
            } else
            {
                likeCountTv.setText(String.valueOf(likeCount) + " Likes");
            }
        }
    }
}
