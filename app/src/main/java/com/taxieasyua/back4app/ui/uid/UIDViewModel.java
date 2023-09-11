package com.taxieasyua.back4app.ui.uid;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UIDViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public UIDViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Мои поездки");
    }

    public LiveData<String> getText() {
        return mText;
    }
}