package com.taxieasyua.back4app.ui.about;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AboutViewModel extends ViewModel {


    private final MutableLiveData<String> mText_author = new MutableLiveData<>();
    private final MutableLiveData<String> mText_site = new MutableLiveData<>();
    private final MutableLiveData<String> mText_email = new MutableLiveData<>();
    private final MutableLiveData<String> mText_build = new MutableLiveData<>();

    public AboutViewModel() {
        mText_author.setValue("автор: Андрей Коржов");
        mText_site.setValue("Сайт: https://play.google.com/store/apps/dev?id=8830024160014473355");
        mText_email.setValue("Email: taxi.easy.ua@gmail.com");
        mText_build.setValue("2023");
    }


    public LiveData<String> getTextAuthor() {
        return mText_author;
    }
    public LiveData<String> getTextSite() {
        return mText_site;
    }
    public LiveData<String> getTextEmail() {
        return mText_email;
    }
    public LiveData<String> getTextBuild() {
        return mText_build;
    }

}