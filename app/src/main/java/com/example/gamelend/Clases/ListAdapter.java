package com.example.gamelend.Clases;

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
import com.example.gamelend.dto.UsuarioResponseDTO;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<UsuarioResponseDTO> usuarios;
    private Context context;
    private OnItemClickListener listener;

    public ListAdapter(List<UsuarioResponseDTO> usuarios, Context context, OnItemClickListener listener) {
        this.usuarios = usuarios;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.usuario_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioResponseDTO usuario = usuarios.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNombre;
        private TextView tvCiudad;
        private ImageView ivFoto;
        private ImageButton ibJuegos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculamos las vistas con los IDs del layout
            tvNombre = itemView.findViewById(R.id.textViewNombre);
            tvCiudad = itemView.findViewById(R.id.textViewLocalidad);
            ivFoto = itemView.findViewById(R.id.imageViewUsuario);
            ibJuegos = itemView.findViewById(R.id.imageButtonJuegos);
        }

        public void bind(final UsuarioResponseDTO usuario, final OnItemClickListener listener) {
            // Asignamos los datos del usuario a las vistas
            tvNombre.setText(usuario.getNombrePublico());
            tvCiudad.setText(usuario.getLocalidad());
            ivFoto.setImageResource(R.drawable.perfil_usuario);

            // Asignamos el comportamiento de los botones de acción
            ibJuegos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onJuegosClick(usuario);
                }
            });
        }
    }

    // Interfaz para los clicks en los elementos
    public interface OnItemClickListener {
        void onJuegosClick(UsuarioResponseDTO usuario);
    }
}
