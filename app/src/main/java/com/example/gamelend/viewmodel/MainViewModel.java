package com.example.gamelend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.repository.UserRepository;

public class MainViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<TokenResponseDTO> tokenResponseLiveData = new MutableLiveData<>();

    public MainViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Getter para observar el LiveData
    public LiveData<TokenResponseDTO> getTokenResponse() {
        return tokenResponseLiveData;
    }

    // Metodo para iniciar sesión
    public void login(String usuario, String contrasena) {
        userRepository.login(usuario, contrasena).observeForever(response -> {
            if (response != null) {
                tokenResponseLiveData.postValue(response);
            } else {
                tokenResponseLiveData.postValue(null); // Manejar error
            }
        });
    }
}
