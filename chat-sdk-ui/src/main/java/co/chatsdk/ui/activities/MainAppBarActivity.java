package co.chatsdk.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

import butterknife.BindView;
import co.chatsdk.core.Tab;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.adapters.PagerAdapterTabs;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.SearchSupported;

public class MainAppBarActivity extends MainActivity {

    protected PagerAdapterTabs adapter;

    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.tabLayout) protected TabLayout tabLayout;
    @BindView(R2.id.viewPager) protected ViewPager viewPager;
    @BindView(R2.id.content) protected RelativeLayout content;
    @BindView(R2.id.searchView) protected MaterialSearchView searchView;
    @BindView(R2.id.root) protected FrameLayout root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    protected boolean searchEnabled() {
        return currentTab().fragment instanceof SearchSupported;
    }

    @Override
    protected void search(String text) {
        Fragment fragment = currentTab().fragment;
        if (fragment instanceof SearchSupported) {
            ((SearchSupported) fragment).filter(text);
        }
    }

    @Override
    protected MaterialSearchView searchView() {
        return searchView;
    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_view_pager;
    }

    protected void initViews() {
        super.initViews();

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        // Only creates the adapter if it wasn't initiated already
        if (adapter == null) {
            adapter = new PagerAdapterTabs(getSupportFragmentManager());
        }

        final List<Tab> tabs = adapter.getTabs();
        for (Tab tab : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab.title));
        }

//        ((BaseFragment) tabs.get(0).fragment).setTabVisibility(true);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setOffscreenPageLimit(3);

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tabSelected(tab);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(Icons.get(Icons.choose().user, R.color.app_bar_icon_color));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void tabSelected(TabLayout.Tab tab) {

        int index = tab.getPosition();

        viewPager.setCurrentItem(index);

        final List<Tab> tabs = adapter.getTabs();

//        Fragment currentFragment = adapter.getTabs().get(index).fragment;
//        if (getSupportActionBar() != null) {
//            if (currentFragment instanceof HasAppbar) {
//                getSupportActionBar().hide();
//            } else {
//                getSupportActionBar().show();
//            }
//        }

        updateLocalNotificationsForTab();

        // We mark the tab as visible. This lets us be more efficient with updates
        // because we only
        for (int i = 0; i < tabs.size(); i++) {
            Fragment fragment = tabs.get(i).fragment;
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) tabs.get(i).fragment).setTabVisibility(i == tab.getPosition());
            }
        }
    }

    public Tab currentTab() {
        return adapter.getTabs().get(viewPager.getCurrentItem());
    }

    public void updateLocalNotificationsForTab() {
        Tab tab = adapter.getTabs().get(tabLayout.getSelectedTabPosition());
        ChatSDK.ui().setLocalNotificationHandler(thread -> showLocalNotificationsForTab(tab.fragment, thread));
    }

    public void clearData() {
        for (Tab t : adapter.getTabs()) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).clearData();
            }
        }
    }

    public void reloadData() {
        for (Tab t : adapter.getTabs()) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).safeReloadData();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                ChatSDK.ui().startProfileActivity(this, ChatSDK.currentUserID());
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}