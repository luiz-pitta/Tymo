package io.development.tymo.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**
 * @author rwondratschek
 */
public class ActivityJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ActivitySyncJob.TAG:
                return new ActivitySyncJob();
            case NotificationSyncJob.TAG:
                return new NotificationSyncJob();
            default:
                return null;
        }
    }

    public static final class AddReceiver extends AddJobCreatorReceiver {
        @Override
        protected void addJobCreator(@NonNull Context context, @NonNull JobManager manager) {
            // manager.addJobCreator(new DemoJobCreator());
        }
    }
}
