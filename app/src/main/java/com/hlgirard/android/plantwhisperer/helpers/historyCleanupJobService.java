package com.hlgirard.android.plantwhisperer.helpers;

import android.app.job.JobParameters;
import android.app.job.JobService;

import data.MoistureHistoryRepository;

public class historyCleanupJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        MoistureHistoryRepository mHistoryRepo = new MoistureHistoryRepository(getApplication());

        mHistoryRepo.deleteAllOlderThan(1000*60*60*24*7); //delete all data older than 7 days from history table

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
