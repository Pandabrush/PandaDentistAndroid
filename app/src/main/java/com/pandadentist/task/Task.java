package com.pandadentist.task;

import java.util.concurrent.Future;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/7 下午3:17
 * 修改时间：2017/4/7 下午3:17
 * Description: 异步任务task
 */
@SuppressWarnings("unused")
public abstract class Task<Params> implements Runnable {

    private Future<?> future;
    protected Params[] params;

    @Override
    public void run() {
        this.doInBackground(params);
    }

    protected abstract void doInBackground(Params... params);

    public final Task<Params> execute(Params... params) {
        this.params = params;
        XExecutor executor = XExecutor.getInstance();
        future = executor.submit(this);
        return this;
    }

    public final void cancel() {
        if (!this.isCancelled()) {
            future.cancel(true);
        }
    }

    public final boolean isCancelled() {
        return future == null || future.isCancelled();
    }
}
