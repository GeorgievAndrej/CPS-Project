package com.cps.teacherapp.data.sync;

public interface SyncCallback {
    void onProgress(int current, int total);
    void onComplete(int totalSynced);
    void onError(String errorMessage);
}