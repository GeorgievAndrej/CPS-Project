package com.cps.teacherapp.data.sync;

// ЗОШТО интерфејс за callback?
// Sync се одвива во позадинска нишка.
// Кога ќе заврши, треба да го извести UI-от.
// Ова е класичен Observer pattern.
public interface SyncCallback {
    void onProgress(int current, int total);  // за прогрес бар
    void onComplete(int totalSynced);         // кога сè е готово
    void onError(String errorMessage);        // при грешка
}