package com.example.socialmediaapp.adapter;

import static com.example.socialmediaapp.MainActivity.VIEW_POST;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.socialmediaapp.fragments.Add;
import com.example.socialmediaapp.fragments.Home;
import com.example.socialmediaapp.fragments.Notification;
import com.example.socialmediaapp.fragments.PostView;
import com.example.socialmediaapp.fragments.Profile;
import com.example.socialmediaapp.fragments.Search;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    int noOfTabs;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int noOfTabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.noOfTabs = noOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){

            default:
            case 0:
                if (VIEW_POST) {
                    return new PostView();
                }
                return new Home();

            case 1:
                return new Search();

            case 2:
                return new Add();

            case 3:
                return new Notification();

            case 4:
                return new Profile();
        }
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}
