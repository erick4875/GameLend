package com.example.gamelend.viewmodel;

import android.app.Application; // Necesario para AndroidViewModel si el repositorio necesita contexto
import androidx.lifecycle.AndroidViewModel; // Usar AndroidViewModel si necesitas ApplicationContext
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer; // Para manejar la observación de forma más segura

import com.example.gamelend.dto.UserResponseDTO;
import com.example.gamelend.repository.UserRepository;

import java.util.List;
import java.util.ArrayList; // Para inicializar con una lista vacía si es necesario

public class UserListViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<UserResponseDTO>> usersLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // Para manejar errores
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(); // Para estado de carga

    // Observador para el LiveData del repositorio
    private Observer<List<UserResponseDTO>> usersObserver;

    // Constructor para inicializar UserRepository
    public UserListViewModel(Application application) {
        super(application);
        // El UserRepository ahora necesita un Context para obtener ApiService
        this.userRepository = new UserRepository(application.getApplicationContext());
    }

    // Getter para que la UI observe la lista de usuarios
    public LiveData<List<UserResponseDTO>> getUsersLiveData() {
        return usersLiveData;
    }

    // Getter para que la UI observe los errores
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // Getter para que la UI observe el estado de carga
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    // metodo para cargar/refrescar la lista de usuarios
    public void fetchUsers() {
        isLoadingLiveData.setValue(true); // Indicar que la carga ha comenzado
        // Eliminar observador anterior si existe para evitar múltiples observadores
        // en el mismo LiveData del repositorio si fetchUsers se llama varias veces.
        if (usersObserver != null && userRepository.getAllUsers().hasObservers()) {
            userRepository.getAllUsers().removeObserver(usersObserver);
        }

        usersObserver = new Observer<List<UserResponseDTO>>() {
            @Override
            public void onChanged(List<UserResponseDTO> users) {
                isLoadingLiveData.setValue(false); // La carga ha terminado
                if (users != null) {
                    usersLiveData.setValue(users);
                } else {
                    // Manejar el caso en que no se obtienen usuarios o hay un error
                    usersLiveData.setValue(new ArrayList<>()); // O mantener valor anterior, o lista vacía
                    errorLiveData.setValue("No se pudieron cargar los usuarios."); // Mensaje de error
                }
                userRepository.getAllUsers().removeObserver(this); // Dejar de observar después de la primera actualización
            }
        };
        //  AuthInterceptor maneja el token
        userRepository.getAllUsers().observeForever(usersObserver);
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar el observador de 'userRepository.getAllUsers()' si sigue activo
        if (usersObserver != null && userRepository.getAllUsers().hasObservers()) {
            userRepository.getAllUsers().removeObserver(usersObserver);
            usersObserver = null;
        }
    }
}

