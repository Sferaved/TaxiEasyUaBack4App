package com.taxieasyua.back4app.ui.fondy.status;

public interface StatusCallback {
    void onStatusReceived(String orderStatus);
    void onError(String errorMessage);
}
