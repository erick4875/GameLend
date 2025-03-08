package com.example.gamelend.ui.usuarios;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;
import com.example.gamelend.model.dto.UsuarioResponseDTO;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {

    private List<UsuarioResponseDTO> usuarios;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UsuarioResponseDTO usuario);
    }

    public UsuarioAdapter(Context context, List<UsuarioResponseDTO> usuarios, OnItemClickListener listener) {
        this.context = context;
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioResponseDTO usuario = usuarios.get(position);

        holder.tvNombrePublico.setText(usuario.getNombrePublico());
        holder.tvEmail.setText(usuario.getEmail());

        String ubicacion = "";
        if (usuario.getLocalidad() != null && !usuario.getLocalidad().isEmpty()) {
            ubicacion = usuario.getLocalidad();

            if (usuario.getProvincia() != null && !usuario.getProvincia().isEmpty()) {
                ubicacion += ", " + usuario.getProvincia();
            }
        } else if (usuario.getProvincia() != null && !usuario.getProvincia().isEmpty()) {
            ubicacion = usuario.getProvincia();
        } else {
            ubicacion = "Ubicación no disponible";
        }

        holder.tvUbicacion.setText(ubicacion);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(usuario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios == null ? 0 : usuarios.size();
    }

    public void updateData(List<UsuarioResponseDTO> newUsuarios) {
        this.usuarios = newUsuarios;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePublico, tvEmail, tvUbicacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePublico = itemView.findViewById(R.id.tvNombrePublico);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
        }
    }
}