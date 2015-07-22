package com.aspsine.multithreaddownload.core;

import android.os.Handler;
import android.util.Log;

import com.aspsine.multithreaddownload.CallBack;

import java.util.concurrent.Executor;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatusDeliveryImpl implements DownloadStatusDelivery {
    private Executor mDownloadStatusPoster;

    public DownloadStatusDeliveryImpl(final Handler handler) {
        mDownloadStatusPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void postConnected(int length, boolean isRangeSupport, DownloadStatus status) {
        status.setStatus(DownloadStatus.STATUS_CONNECTED);
        status.setLength(length);
        status.setIsRangeSupport(isRangeSupport);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postProgressUpdate(int finished, int total, DownloadStatus status) {
        status.setStatus(DownloadStatus.STATUS_PROGRESS);
        status.setLength(total);
        status.setFinished(finished);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postComplete(DownloadStatus status) {
        Log.i("DownloadStatus", "STATUS_COMPLETE");
        status.setStatus(DownloadStatus.STATUS_COMPLETE);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postPause(DownloadStatus status) {
        status.setStatus(DownloadStatus.STATUS_PAUSE);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postCancel(DownloadStatus status) {
        status.setStatus(DownloadStatus.STATUS_CANCEL);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postFailure(DownloadException e, DownloadStatus status) {
        status.setStatus(DownloadStatus.STATUS_FAILURE);
        status.setException(e);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    private static class DownloadStatusDeliveryRunnable implements Runnable {
        private final DownloadStatus mDownloadStatus;
        private final CallBack mCallBack;

        public DownloadStatusDeliveryRunnable(DownloadStatus downloadStatus) {
            this.mDownloadStatus = downloadStatus;
            this.mCallBack = mDownloadStatus.getCallBack();
        }

        @Override
        public void run() {
            switch (mDownloadStatus.getStatus()) {
                case DownloadStatus.STATUS_CONNECTED:
                    mCallBack.onConnected(mDownloadStatus.getLength(), mDownloadStatus.isRangeSupport());
                    break;
                case DownloadStatus.STATUS_PROGRESS:
                    final int finished = mDownloadStatus.getFinished();
                    final int length = mDownloadStatus.getLength();
                    final int percent = finished * 100 / length;
                    mCallBack.onProgress(finished, length, percent);
                    break;
                case DownloadStatus.STATUS_COMPLETE:
                    Log.i("DownloadStatus", "STATUS_COMPLETE");
                    mCallBack.onComplete();
                    break;
                case DownloadStatus.STATUS_FAILURE:
                    mCallBack.onFailure(mDownloadStatus.getException());
                    break;
            }
        }
    }


}
