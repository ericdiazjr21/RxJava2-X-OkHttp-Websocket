package com.example.rxwebsocket;

import android.annotation.SuppressLint;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class RxWebSocket {

    private final OkHttpClient client;
    private final Request request;
    private WebSocket webSocket;

    public RxWebSocket(@NonNull final OkHttpClient client,
                       @NonNull final Request request) {
        this.client = client;
        this.request = request;
    }

    public RxWebSocket(@NonNull final String requestUrl) {
        client = getBasicOkHttpClient();
        request = getBasicRequest(requestUrl);
    }

    private Request getBasicRequest(@NonNull final String requestUrl) {
        return new Request.Builder().url(requestUrl).build();
    }

    private OkHttpClient getBasicOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @SuppressLint("CheckResult")
    private Subject<String> connectWebSocketSubject(Subject<String> subject) {

        final WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                subject.onNext(response.message());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                subject.onNext(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                subject.onNext(bytes.toString());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                subject.onNext(reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                subject.onComplete();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                subject.onError(t);
            }
        };

        subject
          .subscribeOn(Schedulers.io())
          .doOnSubscribe(disposable -> webSocket = client.newWebSocket(request, webSocketListener))
          .doAfterTerminate(() -> webSocket.close(1000, null));

        return subject;
    }

    public Observable<String> publishSubjectStream() {
        return connectWebSocketSubject(PublishSubject.create());
    }

    public Observable<String> replaySubjectStream() {
        return connectWebSocketSubject(ReplaySubject.create());
    }

    public Observable<String> behaviorSubjectStream() {
        return connectWebSocketSubject(BehaviorSubject.create());
    }

    public Observable<String> asyncSubjectStream() {
        return connectWebSocketSubject(ReplaySubject.create());
    }

    public Observable<String> customPublishSubjectStream(@NonNull final PublishSubject<String> subject) {
        return connectWebSocketSubject(subject);
    }

    public Observable<String> customReplaySubjectStream(@NonNull final ReplaySubject<String> subject) {
        return connectWebSocketSubject(subject);
    }

    public Observable<String> customBehaviorSubjectStream(@NonNull final BehaviorSubject<String> subject) {
        return connectWebSocketSubject(subject);
    }

    public Observable<String> customAsyncSubjectStream(@NonNull final AsyncSubject<String> subject) {
        return connectWebSocketSubject(subject);
    }

}
