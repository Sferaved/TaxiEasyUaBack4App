package com.taxieasyua.back4app.ui.open_map.visicom.key;

public interface ApiCallback {
    void onVisicomKeyReceived(String key);
    void onApiError(int errorCode);
    void onApiFailure(Throwable t);
}

