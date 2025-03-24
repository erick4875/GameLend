package com.example.gamelend.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamelend.dto.UsuarioResponseDTO;
import com.example.gamelend.repository.UserRepository;

import java.util.List;

public class ListaUsuariosViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<UsuarioResponseDTO>> usuariosLiveData = new MutableLiveData<>();

    public ListaUsuariosViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        cargarUsuarios();
    }

    public LiveData<List<UsuarioResponseDTO>> getUsuarios() {
        return usuariosLiveData;
    }

    public void cargarUsuarios() {
        userRepository.obtenerUsuarios().observeForever(usuarios -> {
            usuariosLiveData.setValue(usuarios);
        });
    }
}

