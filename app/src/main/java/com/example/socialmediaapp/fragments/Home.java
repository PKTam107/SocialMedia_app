package com.example.socialmediaapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.HomeAdapter;
import com.example.socialmediaapp.adapter.StoriesAdapter;
import com.example.socialmediaapp.chat.ChatUserActivity;
import com.example.socialmediaapp.model.HomeModel;
import com.example.socialmediaapp.model.StoriesModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Home extends Fragment {

    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();
    HomeAdapter adapter;
    RecyclerView storiesRecyclerView;
    StoriesAdapter storiesAdapter;
    List<StoriesModel> storiesModelList;
    private RecyclerView recyclerView;
    private List<HomeModel> list;
    private FirebaseUser user;
    Activity activity;

    long currentTime, expiryTime;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();

        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed((position, id, uid, likeList, isChecked) -> {
            // Kiểm tra xem người dùng hiện tại đã like bài viết trước đó hay không
            boolean isPreviouslyLiked = likeList.contains(user.getUid());

            // Cập nhật danh sách like trên Firestore
            DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                    .document(uid)
                    .collection("Post Images")
                    .document(id);

            if (isChecked) {
                // Người dùng thích bài viết
                likeList.add(user.getUid()); // Thêm người dùng vào danh sách like
                if (!isPreviouslyLiked && !uid.equals(user.getUid())) {
                    // Nếu bài viết chưa được người dùng hiện tại like trước đó và người dùng không phải là chủ sở hữu của bài viết
                    createNotification(uid, id); // Tạo thông báo
                }
            } else {
                // Người dùng bỏ thích bài viết
                likeList.remove(user.getUid()); // Xóa người dùng khỏi danh sách like
            }

            // Cập nhật danh sách like trên Firestore
            Map<String, Object> map = new HashMap<>();
            map.put("likes", likeList);
            reference.update(map);
        });

        view.findViewById(R.id.sendBtn).setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), ChatUserActivity.class);
            startActivity(intent);

        });

    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList = new ArrayList<>();
        storiesModelList.add(new StoriesModel("", "", "", "", "", System.currentTimeMillis()));
        storiesAdapter = new StoriesAdapter(storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storiesAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }

    private void loadDataFromFirestore() {
        // Lấy tất cả người dùng
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userSnapshot = task.getResult();
                        List<String> followingList = (List<String>) userSnapshot.get("following");
                        assert followingList != null;
                        followingList.add(user.getUid());
                        if (followingList != null && !followingList.isEmpty()) {
                            // Gọi hàm để lấy bài post từ các người dùng mà người dùng hiện tại đang theo dõi
                            loadPostsFromUsers(followingList);
                            loadStories(followingList);
                            scheduleStoryDeletion();
                        } else {
                            Log.d("Info: ", "No users followed by the current user.");
                        }
                    } else {
                        Log.d("Error: ", task.getException().getMessage());
                    }
                });
    }
    private void loadPostsFromUsers(List<String> uidList) {
        // Lấy bài post từ tất cả người dùng
        FirebaseFirestore.getInstance().collectionGroup("Post Images")
                .whereIn("uid", uidList)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.d("Error: ", error.getMessage());
                        return;
                    }

                    if (value == null)
                        return;

                    list.clear();

                    for (QueryDocumentSnapshot snapshot : value) {
                        if (!snapshot.exists())
                            return;

                        HomeModel model = snapshot.toObject(HomeModel.class);
                        list.add(new HomeModel(
                                model.getName(),
                                model.getProfileImage(),
                                model.getImageUrl(),
                                model.getUid(),
                                model.getDescription(),
                                model.getId(),
                                model.getTimestamp(),
                                model.getLikes()));
                    }
                    adapter.notifyDataSetChanged();
                });
        loadStories(uidList);
    }

    void loadStories(List<String> followingList) {

        List<StoriesModel> tempStoriesList = new ArrayList<>(); // Tạo một danh sách tạm thời để chứa các stories mới

        Query query = FirebaseFirestore.getInstance().collection("Stories");
        query.whereIn("uid", followingList).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null || value.isEmpty()) {
                // Nếu không có stories nào, không cần làm gì cả
                return;
            }

            // Duyệt qua từng document trong snapshot
            for (QueryDocumentSnapshot snapshot : value) {
                // Chuyển đổi document thành đối tượng StoriesModel
                StoriesModel model = snapshot.toObject(StoriesModel.class);

                // Kiểm tra xem stories đã tồn tại trong danh sách tạm thời hay chưa
                boolean isNewStory = true;
                for (StoriesModel tempStory : tempStoriesList) {
                    if (tempStory.getId().equals(model.getId())) {
                        // Nếu stories đã tồn tại trong danh sách tạm thời, đánh dấu là không phải stories mới
                        isNewStory = false;
                        break;
                    }
                }

                // Nếu stories là mới, thêm vào danh sách tạm thời
                if (isNewStory) {
                    tempStoriesList.add(model);
                }
            }

            // Xóa hết các stories cũ trong danh sách chính và thêm các stories mới từ danh sách tạm thời
            storiesModelList.clear();
            storiesModelList.addAll(tempStoriesList);

            // Thông báo cho adapter biết rằng danh sách stories đã thay đổi
            storiesAdapter.notifyDataSetChanged();
        });

    }

    void scheduleStoryDeletion() {
        // Schedule a background task to run periodically

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                currentTime = System.currentTimeMillis();

                // Calculate the timestamp of 24 hours ago
                expiryTime = currentTime - (120 * 1000);

                // Query Firestore for stories uploaded more than 24 hours ago
                CollectionReference storiesRef = FirebaseFirestore.getInstance().collection("Stories");
                Query query = storiesRef.whereLessThan("timestamp", expiryTime);

                query.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Delete the story
                            storiesRef.document(document.getId()).delete();
                        }
                    } else {
                        // Handle errors
                    }
                });
            }
        }, 0, 120 * 1000); // Run every 24 hours
    }

    void createNotification(String uid, String postid) {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");

        String id = reference.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("time", FieldValue.serverTimestamp());
        map.put("notification", user.getDisplayName() + " liked your post.");
        map.put("id", id);
        map.put("uid", uid);
        map.put("postId", postid);

        reference.document(id).set(map);

    }
}