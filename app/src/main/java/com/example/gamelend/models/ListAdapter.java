package com.example.gamelend.models; // Asegúrate que sea 'models' o el paquete correcto

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;
import com.example.gamelend.dto.UserResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<UserResponseDTO> userList; // Cambiado a userList (inglés)
    private Context context;
    private OnItemClickListener onItemClickListener; // Cambiado a onItemClickListener (inglés)

    // Constructor
    public ListAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.onItemClickListener = listener;
        this.userList = new ArrayList<>(); // Inicializar aquí
    }

    //metodo para actualizar la lista
    public void submitList(List<UserResponseDTO> newUserList) {
        this.userList.clear();
        if (newUserList != null) {
            this.userList.addAll(newUserList);
        }
        notifyDataSetChanged(); // Notifica al adaptador que los datos cambiaron DiffUtil
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponseDTO user = userList.get(position); // Cambiado a userList y user
        holder.bind(user, onItemClickListener); // Pasa el onItemClickListener de la instancia
    }

    @Override
    public int getItemCount() {
        return userList.size(); // Cambiado a userList
    }

    // Interfaz para los clicks en los elementos
    public interface OnItemClickListener {
        void onEdit(UserResponseDTO user);
        void onDelete(UserResponseDTO user);
        void onGamesClick(UserResponseDTO user);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Nombres de vistas en inglés (asumiendo que los IDs en usuario_cardview.xml también se actualizarán)
        TextView nameTextView; // Antes tvNombre
        TextView cityTextView; // Antes tvCiudad
        ImageView userImageView; // Antes ivFoto
        ImageButton gamesImageButton; // Antes ibJuegos

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculamos las vistas con los IDs del layout (actualiza los IDs si los cambias en el XML)
            nameTextView = itemView.findViewById(R.id.textViewNombre); // Mantenlo o cámbialo a R.id.nameTextView
            cityTextView = itemView.findViewById(R.id.textViewLocalidad); // Mantenlo o cámbialo a R.id.cityTextView
            userImageView = itemView.findViewById(R.id.imageViewUsuario); // Mantenlo o cámbialo a R.id.userImageView
            gamesImageButton = itemView.findViewById(R.id.imageButtonJuegos); // Mantenlo o cámbialo a R.id.gamesImageButton
        }

        public void bind(final UserResponseDTO user, final OnItemClickListener listener) {
            // Asignamos los datos del usuario a las vistas
            nameTextView.setText(user.getPublicName()); // Asumiendo que UserResponseDTO tiene getPublicName()
            cityTextView.setText(user.getCity()); // Asumiendo que UserResponseDTO tiene getCity()
            userImageView.setImageResource(R.drawable.perfil_usuario); // Placeholder

            // Asignamos el comportamiento de los botones de acción
            gamesImageButton.setOnClickListener(v -> {
                if (listener != null) { // Buena práctica verificar que el listener no sea nulo
                    listener.onGamesClick(user); // Cambiado a onGamesClick
                }
            });

            // Implementa listeners para onEdit y onDelete si tienes botones para ello en tu item_layout
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    // Podrías tener un click general en el item, o botones específicos para edit/delete
                    // Ejemplo: listener.onEdit(user);
                }
            });
        }
    }
}