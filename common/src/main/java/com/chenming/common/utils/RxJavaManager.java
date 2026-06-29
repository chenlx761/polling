package com.chenming.common.utils;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created: fwp
 * Date: 2022/3/22
 * Description:
 */

public class RxJavaManager {
    // 指向自己实例的私有静态引用
    private static RxJavaManager mInstance;
    protected CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public interface OnNeedInNewThreadToDoListener<T> {
        T inNewThreadToDo() throws IOException;

        void toDoFinish(T info);

        void toDoFaild(Throwable throwable);
    }

    public interface OnNeedInNewThreadToDoReturnListListener<T> {
        List<T> inNewThreadToDo() throws IOException;

        void toDoFinish(List<T> info);

        void toDoFaild(Throwable throwable);
    }

    public interface onTimerToDoListener {
        void timerToDo(Long aLong);
    }


    public interface onTimerRepeatToDoListener {
        void timerToDo(Long aLong);

        void onFinish();
    }

    // 私有的构造方法
    private RxJavaManager() {
    }

    // 以自己实例为返回值的静态的公有方法，静态工厂方法
    public static RxJavaManager getInstance() {
        // 被动创建，在真正需要使用时才去创建
        if (mInstance == null) {
            mInstance = new RxJavaManager();
        }
        return mInstance;
    }

    /**
     * 延时任务
     *
     * @param delayed
     * @param listener
     * @return
     */
    public Disposable delayedToDoHasReturn(long delayed, onTimerToDoListener listener) {
        // 每隔1000毫秒执行一次逻辑代码
        return Observable.timer(delayed, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        listener.timerToDo(aLong);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

    }


    /**
     * 定时任务
     *
     * @param initialDelay 延時時間
     * @param period       間隔時間
     * @param listener
     * @return
     */
    public Disposable timerToDo(long initialDelay, long period, onTimerToDoListener listener) {
        return Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        listener.timerToDo(aLong);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }


    /**
     * 定时任务
     *
     * @param initialDelay 延時時間
     * @param period       間隔時間
     * @param listener
     * @return
     */
    public Disposable timerToDoNewThread(long initialDelay, long period, onTimerToDoListener listener) {
        return Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        listener.timerToDo(aLong);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }


    /**
     * 定时任务
     *
     * @param initialDelay 延時時間
     * @param period       間隔時間
     * @param repeat_time  重复次数
     * @param listener
     * @return
     */
    public Disposable timerToDo(long initialDelay, long period, int repeat_time, onTimerRepeatToDoListener listener) {
        return Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                .take(repeat_time)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        listener.timerToDo(aLong);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        listener.onFinish();
                    }
                });
    }

    /**
     * 延时任务
     *
     * @param delayed
     * @param listener
     * @return
     */
    public void delayedToDo(long delayed, onTimerToDoListener listener) {
        // 每隔1000毫秒执行一次逻辑代码
        Disposable subscribe = Observable.timer(delayed, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        mCompositeDisposable.clear();
                        listener.timerToDo(aLong);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        mCompositeDisposable.clear();
                    }
                });
        mCompositeDisposable.add(subscribe);

    }


    public <T> void doTo(OnNeedInNewThreadToDoListener<T> listener) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> e) throws Exception {
                        T t = listener.inNewThreadToDo();
                        try {
                            if (e != null)
                                e.onNext(t);
                        } catch (Exception e1) {

                        }
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(T info) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFinish(info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFaild(throwable);
                    }
                });//在主线程中执行
        mCompositeDisposable.add(subscribe);
    }

    public <T> Disposable doToSingle(OnNeedInNewThreadToDoListener<T> listener) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> e) throws Exception {
                        T t = listener.inNewThreadToDo();
                        e.onNext(t);
                    }
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(T info) throws Exception {
                        listener.toDoFinish(info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listener.toDoFaild(throwable);
                    }
                });//在主线程中执行
        return subscribe;
    }

    public <T> Disposable doToListSingle(OnNeedInNewThreadToDoReturnListListener<T> listener) {
        return Observable.create(new ObservableOnSubscribe<List<T>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<T>> e) throws Exception {
                        List<T> ts = listener.inNewThreadToDo();
                        e.onNext(ts);
                    }
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<T>>() {
                    @Override
                    public void accept(List<T> info) throws Exception {
                        listener.toDoFinish(info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listener.toDoFaild(throwable);
                    }
                });//在主线程中执行;
    }


    public <T> Disposable doToListHasReturn(OnNeedInNewThreadToDoReturnListListener<T> listener) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<List<T>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<T>> e) throws Exception {
                        List<T> ts = listener.inNewThreadToDo();
                        e.onNext(ts);
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<T>>() {
                    @Override
                    public void accept(List<T> info) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFinish(info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFaild(throwable);
                    }
                });//在主线程中执行
        return subscribe;
    }
    public <T> void doToList(OnNeedInNewThreadToDoReturnListListener<T> listener) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<List<T>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<T>> e) throws Exception {
                        List<T> ts = listener.inNewThreadToDo();
                        e.onNext(ts);
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<T>>() {
                    @Override
                    public void accept(List<T> info) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFinish(info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mCompositeDisposable.clear();
                        listener.toDoFaild(throwable);
                    }
                });//在主线程中执行
        mCompositeDisposable.add(subscribe);
    }
}
