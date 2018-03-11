package com.example.arne.translogistics_device.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.example.arne.translogistics_device.DAL.DateConverter;

import java.util.Date;

/**
 * Created by Arne on 11-03-2018.
 */


@Entity
@TypeConverters(DateConverter.class)
public class DataRecording {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "rec_intervals")
    private int recordingIntervals;
    @ColumnInfo(name = "max_shock_limit")
    private int maxShockLimit;
    @ColumnInfo(name = "start_time")
    private Date startTime;
    @ColumnInfo(name = "end_time")
    private Date endTime;
    @ColumnInfo(name = "package_id")
    private int packageId;

    public DataRecording(){}

    public DataRecording(int id, int recordingIntervals, int maxShockLimit, Date startTime, Date endTime, int packageId) {
        this.id = id;
        this.recordingIntervals = recordingIntervals;
        this.maxShockLimit = maxShockLimit;
        this.startTime = startTime;
        this.endTime = endTime;
        this.packageId = packageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecordingIntervals() {
        return recordingIntervals;
    }

    public void setRecordingIntervals(int recordingIntervals) {
        this.recordingIntervals = recordingIntervals;
    }

    public int getMaxShockLimit() {
        return maxShockLimit;
    }

    public void setMaxShockLimit(int maxShockLimit) {
        this.maxShockLimit = maxShockLimit;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }
}
