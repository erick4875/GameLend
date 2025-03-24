package com.example.gamelend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamelend.dto.LoginResponse;
import com.example.gamelend.repository.UserRepository;

public class MainViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<LoginResponse> loginResponseLiveData = new MutableLiveData<>();

    public MainViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponseLiveData;
    }

    public void login(String usuario, String contrasena) {
        userRepository.login(usuario, contrasena).observeForever(response -> {
            loginResponseLiveData.setValue(response);
        });
    }
}
