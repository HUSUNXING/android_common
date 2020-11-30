/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artiwares.common.http.callAdapter;


import com.artiwares.common.http.exception.ResponseThrowable;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import retrofit2.HttpException;
import retrofit2.Response;

final class BodyObservable<T> extends Observable<T> {
    private final Observable<Response<T>> upstream;
    public static final String SUCCESS = "000000";

    BodyObservable(Observable<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        upstream.subscribe(new BodyObserver<>(observer));
    }

    private static class BodyObserver<R> implements Observer<Response<R>> {
        private final Observer<? super R> observer;
        private boolean terminated;

        BodyObserver(Observer<? super R> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            observer.onSubscribe(disposable);
        }

        @Override
        public void onNext(Response<R> response) {
            if (response.isSuccessful()) {
                R body = response.body();
                if (body != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(body.toString());
                        String resultCode = jsonObject.getString("resultCode");
                        String resultMsg = jsonObject.getString("resultMsg");
                        if (resultCode.equals(SUCCESS)) {
                            observer.onNext(response.body());
                            observer.onComplete();
                        } else {
                            ResponseThrowable responseThrowable = new ResponseThrowable(resultCode, resultMsg);
                            sendError(responseThrowable);
                        }
                    } catch (JSONException e) {
                        Throwable t = new HttpException(response);
                        sendError(t);
                    }
                } else {
                    Throwable t = new HttpException(response);
                    sendError(t);
                }
            } else {
                terminated = true;
                Throwable t = new HttpException(response);
                sendError(t);
            }
        }

        private void sendError(Throwable t) {
            try {
                observer.onError(t);
                observer.onComplete();
            } catch (Throwable inner) {
                Exceptions.throwIfFatal(inner);
                RxJavaPlugins.onError(new CompositeException(t, inner));
            }
        }

        @Override
        public void onComplete() {
            if (!terminated) {
                observer.onComplete();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!terminated) {
                observer.onError(throwable);
            } else {
                // This should never happen! onNext handles and forwards errors automatically.
                Throwable broken =
                        new AssertionError(
                                "This should never happen! Report as a bug with the full stacktrace.");
                //noinspection UnnecessaryInitCause Two-arg AssertionError constructor is 1.7+ only.
                broken.initCause(throwable);
                RxJavaPlugins.onError(broken);
            }
        }
    }
}
