package org.multibluetooth.multibluetooth.History;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by YS on 2016-10-04.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {
    private static final String TAG = "HistoryListAdapter";
    Context context;
    List<SafeScore> items;
    List<Boolean> selectedList;
    boolean selectAction = false;

    public HistoryListAdapter(Context context, List<SafeScore> items) {
        this.context = context;
        this.items = items;
        this.selectedList = new ArrayList<>();
        int i=0;
        while(i++<items.size()) {
            selectedList.add(false);
        }
    }

    public void onDeleteItem() {

    }

    public List<Boolean> getSelectedItems() {
        return selectedList;
    }

    public boolean isSelect() {
        return selectAction;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SafeScore item = items.get(position);

        holder.driveId.setText(""+item.getDriveId());
        holder.avgScore.setText(""+item.getAvgScore());

        Calendar calendar = Calendar.getInstance();
        Log.d(TAG, item.getDriveStart());
        Log.d(TAG, Long.valueOf(item.getDriveStart()).toString());
        if (item.getDriveStart() != null && !item.getDriveStart().equals("")) {
            calendar.setTimeInMillis(Long.valueOf(item.getDriveStart()));

            String date = String.valueOf(calendar.get(Calendar.YEAR)).substring(2,4) + "-";
            if (calendar.get(Calendar.MONTH) < 10)  date += "0" + calendar.get(Calendar.MONTH) + "-";
            else                                    date += calendar.get(Calendar.MONTH) + "-";

            if (calendar.get(Calendar.DAY_OF_MONTH) < 10)   date += "0" + calendar.get(Calendar.DAY_OF_MONTH) + " ";
            else                                            date += calendar.get(Calendar.DAY_OF_MONTH) + " ";

            if (calendar.get(Calendar.HOUR_OF_DAY) < 10)   date += "0" + calendar.get(Calendar.HOUR_OF_DAY) + ":";
            else                                            date += calendar.get(Calendar.HOUR_OF_DAY) + ":";

            if (calendar.get(Calendar.MINUTE) < 10) date += "0" + calendar.get(Calendar.MINUTE);
            else                                    date += calendar.get(Calendar.MINUTE);

            holder.dateStart.setText(date);
        }

        if (item.getDriveStop() != null && !item.getDriveStop().equals("")) {
            calendar.setTimeInMillis(Long.valueOf(item.getDriveStop()));

            String date = String.valueOf(calendar.get(Calendar.YEAR)).substring(2,4) + "-";
            if (calendar.get(Calendar.MONTH) < 10)  date += "0" + calendar.get(Calendar.MONTH) + "-";
            else                                    date += calendar.get(Calendar.MONTH) + "-";

            if (calendar.get(Calendar.DAY_OF_MONTH) < 10)   date += "0" + calendar.get(Calendar.DAY_OF_MONTH) + " ";
            else                                            date += calendar.get(Calendar.DAY_OF_MONTH) + " ";

            if (calendar.get(Calendar.HOUR_OF_DAY) < 10)   date += "0" + calendar.get(Calendar.HOUR_OF_DAY) + ":";
            else                                            date += calendar.get(Calendar.HOUR_OF_DAY) + ":";

            if (calendar.get(Calendar.MINUTE) < 10) date += "0" + calendar.get(Calendar.MINUTE);
            else                                    date += calendar.get(Calendar.MINUTE);
            holder.dateStop.setText(date);
        }

        holder.driveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectAction) {
                    Log.d(TAG, "short");
                    // 선택 액션시 해당 holder의 상태를 역으로 바꿈
                    selectedList.set(holder.getAdapterPosition(), !selectedList.get(holder.getAdapterPosition()));
                    if (selectedList.get(holder.getAdapterPosition())) holder.selected.setVisibility(View.VISIBLE);
                    else holder.selected.setVisibility(View.GONE);
                } else {
                    // 해당 ID의 운행정보 가져옴
                    Intent intent = new Intent(context, SafeScoreActivity.class);
                    intent.putExtra("drive_item", item);
                    context.startActivity(intent);
                }
            }
        });

        holder.driveItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "long");
                selectAction = true;
                selectedList.set(holder.getAdapterPosition(), true);
                holder.selected.setVisibility(View.VISIBLE);
                ((HistoryActivity) context).setOnShowMenu();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView driveId, avgScore, dateStart, dateStop;
        RelativeLayout driveItem;
        RelativeLayout selected;

        public ViewHolder(View itemView) {
            super(itemView);
            driveItem = (RelativeLayout) itemView.findViewById(R.id.drive_item);
            driveId = (TextView) itemView.findViewById(R.id.drive_id);
            avgScore = (TextView) itemView.findViewById(R.id.avg_score);
            dateStart = (TextView) itemView.findViewById(R.id.date_start);
            dateStop = (TextView) itemView.findViewById(R.id.date_stop);

            selected = (RelativeLayout) itemView.findViewById(R.id.selected);
        }
    }
}
