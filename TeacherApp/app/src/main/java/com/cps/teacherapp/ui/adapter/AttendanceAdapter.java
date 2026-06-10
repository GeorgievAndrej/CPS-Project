package com.cps.teacherapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cps.teacherapp.R;
import com.cps.teacherapp.data.model.AttendanceRecord;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends
        RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceRecord> records = new ArrayList<>();
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    // DiffUtil би бил подобар, но за почетник setData е доволно
    public void setData(List<AttendanceRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord r = records.get(position);
        holder.tvStudentName.setText(r.studentName);
        holder.tvStudentId.setText(r.studentId + " • " + r.courseName);
        holder.tvTime.setText(sdf.format(new Date(r.tappedAt)));
        // ✅ зелено = синхронизиран, ⏳ = чека
        holder.tvSyncIndicator.setText(r.isSynced ? "✅" : "⏳");
    }

    @Override
    public int getItemCount() { return records.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvTime, tvSyncIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName    = itemView.findViewById(R.id.tvStudentName);
            tvStudentId      = itemView.findViewById(R.id.tvStudentId);
            tvTime           = itemView.findViewById(R.id.tvTime);
            tvSyncIndicator  = itemView.findViewById(R.id.tvSyncIndicator);
        }
    }
}