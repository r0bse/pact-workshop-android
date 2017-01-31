package au.com.dius.pactconsumer.util;


import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RxBinder {

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  public <T> void bind(Observable<T> observable,
                       Consumer<T> onNext,
                       Consumer<Throwable> onError,
                       Action onComplete) {
    compositeDisposable.add(
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableObserver<T>() {

              @Override
              public void onNext(T o) {
                try {
                  onNext.accept(o);
                } catch (Exception e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error calling onNext", e);
                }
              }

              @Override
              public void onError(Throwable thr) {
                try {
                  onError.accept(thr);
                } catch (Exception e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error calling onError", e);
                }
              }

              @Override
              public void onComplete() {
                try {
                  onComplete.run();
                } catch (Exception e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error calling onComplete", e);
                }
              }
            })
    );
  }

  public void clear() {
    compositeDisposable.clear();
  }
}
