package com.example.socialmediaapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.NotificationAdapter;
import com.example.socialmediaapp.model.NotificationModel;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Fragment {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<NotificationModel> list;
    FirebaseUser user;

    public Notification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        loadNotification();
        // Set sự kiện click cho Adapter
//        adapter.setOnNotificationClickListener(notification -> {
//            // Xử lý khi sự kiện click được kích hoạt
//            // Ví dụ: Chuyển đến bài post tương ứng
//            Intent intent = new Intent(getActivity(), PostViewActivity.class);
//            intent.putExtra("uid", notification.getUid()); // Truyền UID của người đăng bài post
//            intent.putExtra("id", notification.getPostId()); // Truyền ID của bài post
//            startActivity(intent);
//        });
    }

    void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), list);

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    void loadNotification() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");

        reference.whereEqualTo("uid", user.getUid())
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value.isEmpty()) {
                        return;
                    }

                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        NotificationModel model = snapshot.toObject(NotificationModel.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}