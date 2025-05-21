package com.example.gamelend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.repository.UserRepository;

import java.util.List;

public class ListaUsuariosViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<UserResponseDTO>> usuariosLiveData = new MutableLiveData<>();

    public ListaUsuariosViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<List<UserResponseDTO>> getUsuarios() {
        return usuariosLiveData;
    }

    public void cargarUsuarios(String token) {
        userRepository.obtenerUsuarios(token).observeForever(usuarios -> {
            if(usuarios != null) {
                usuariosLiveData.setValue(usuarios);
            } else {
                // Manejar el caso en que no se obtienen usuarios
                usuariosLiveData.setValue(null);
            }
        });
    }
}

