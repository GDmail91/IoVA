package org.multibluetooth.multibluetooth.History;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

import java.util.ArrayList;

/**
 * Created by YS on 2016-10-04.
 */
public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    // VIEW
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private boolean showMenu = false;
    private MenuItem registrar;

    ArrayList<SafeScore> historyList = new ArrayList<>();
    HistoryListAdapter historyListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list_activity);

        // swipe layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO 리프레쉬
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // recycler view
        recyclerView = (RecyclerView) swipeRefreshLayout.findViewById(R.id.ranking_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager1);


        // 전체 운행기록 가져옴
        SafeScoreModel safeScoreModel = new SafeScoreModel(this, "DriveInfo.db", null);
        historyList = safeScoreModel.getAllData();
        for(SafeScore dinfo : historyList) {
            Log.d("TEST", dinfo.toString());
        }
        safeScoreModel.close();

        // binding
        historyListAdapter = new HistoryListAdapter(HistoryActivity.this, historyList);
        recyclerView.setAdapter(historyListAdapter);

    }

    public void setOnShowMenu() {
        showMenu = true;
        registrar.setVisible(true);
    }

    public void setOffShowMenu() {
        showMenu = false;
        registrar.setVisible(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        registrar = menu.findItem(R.id.seleted_delete);
        registrar.setVisible(showMenu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.seleted_delete:
                seletedDelete();
                setOffShowMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void seletedDelete() {
        // 선택된 아이템 삭제
        SafeScoreModel safeScoreModel = new SafeScoreModel(this, "DriveInfo.db", null);
        DriveInfoModel driveInfoModel = new DriveInfoModel(this, "DriveInfo.db", null);
        ArrayList<Integer> driveIds = new ArrayList<>();
        ArrayList<Boolean> selectedItems = new ArrayList<>(historyListAdapter.getSelectedItems());
        for (int i=selectedItems.size()-1; i >= 0; i--) {
            if (selectedItems.get(i)) {
                // 리스트의 아래부터 삭제, 삭제할 id 목록에 저장
                driveIds.add(historyList.remove(i).getDriveId());
            }
        }

        // TODO DB에서 삭제 작업
        Log.d(TAG, ""+driveIds.size());
        driveInfoModel.deleteByDriveIds(driveIds);
        Log.d(TAG, ""+driveIds.size());
        safeScoreModel.deleteByIds(driveIds);

        historyListAdapter = new HistoryListAdapter(HistoryActivity.this, historyList);
        recyclerView.setAdapter(historyListAdapter);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "isSelect: "+historyListAdapter.isSelect());
        if (historyListAdapter.isSelect()) {
            historyListAdapter = new HistoryListAdapter(HistoryActivity.this, historyList);
            recyclerView.setAdapter(historyListAdapter);
            setOffShowMenu();
        } else {
            super.onBackPressed();
        }
    }
}

