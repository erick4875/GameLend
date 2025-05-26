package com.example.gamelend.models;

import android.content.Context;
import android.util.Log;
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

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.UserViewHolder> {

    private static final String TAG_ADAPTER = "ListAdapter";

    private List<UserResponseDTO> userList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onEdit(UserResponseDTO user);
        void onDelete(UserResponseDTO user);
        void onGamesClick(UserResponseDTO user);
    }

    public ListAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.onItemClickListener = listener;
        this.userList = new ArrayList<>();
    }

    public void submitList(List<UserResponseDTO> newUserList) {
        this.userList.clear();
        if (newUserList != null) {
            this.userList.addAll(newUserList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_cardview, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponseDTO currentUser = userList.get(position);
        holder.bind(currentUser, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextViewVH; // Renombrado para evitar confusión con IDs XML
        TextView userLocationTextViewVH;
        ImageView userProfileImageViewVH;
        ImageButton viewUserGamesButtonVH;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // --- IDs CORREGIDOS PARA COINCIDIR CON TU ÚLTIMO user_cardview.xml ---
            userNameTextViewVH = itemView.findViewById(R.id.userNameTextView);
            userLocationTextViewVH = itemView.findViewById(R.id.userLocationTextView);
            userProfileImageViewVH = itemView.findViewById(R.id.userProfileImageView);
            viewUserGamesButtonVH = itemView.findViewById(R.id.viewUserGamesButton);
            // -----------------------------------------------------------------

            if (userNameTextViewVH == null) Log.e(TAG_ADAPTER, "UserViewHolder: userNameTextViewVH es NULL - verifica ID userNameTextView en XML");
            if (userLocationTextViewVH == null) Log.e(TAG_ADAPTER, "UserViewHolder: userLocationTextViewVH es NULL - verifica ID userLocationTextView en XML");
            if (userProfileImageViewVH == null) Log.e(TAG_ADAPTER, "UserViewHolder: userProfileImageViewVH es NULL - verifica ID userProfileImageView en XML");
            if (viewUserGamesButtonVH == null) Log.e(TAG_ADAPTER, "UserViewHolder: viewUserGamesButtonVH es NULL - verifica ID viewUserGamesButton en XML");
        }

        public void bind(final UserResponseDTO user, final OnItemClickListener listener) {
            if (userNameTextViewVH != null && user.getPublicName() != null) {
                userNameTextViewVH.setText(user.getPublicName());
            } else if (userNameTextViewVH != null) {
                userNameTextViewVH.setText("Nombre no disponible");
            }

            if (userLocationTextViewVH != null) {
                String location = "";
                if (user.getCity() != null && !user.getCity().isEmpty()) {
                    location += user.getCity();
                }
                if (user.getProvince() != null && !user.getProvince().isEmpty()) {
                    if (!location.isEmpty()) {
                        location += ", ";
                    }
                    location += user.getProvince();
                }
                userLocationTextViewVH.setText(location.isEmpty() ? "Ubicación no disponible" : location);
            }

            if (userProfileImageViewVH != null) {
                userProfileImageViewVH.setImageResource(R.drawable.perfil_usuario); // Placeholder
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGamesClick(user);
                }
            });

            // El listener para viewUserGamesButtonVH es opcional si itemView ya maneja onGamesClick.
            // Si quieres que SOLO el botón active onGamesClick, mueve el listener.onGamesClick(user) aquí
            // y quítalo del itemView.setOnClickListener.
            if (viewUserGamesButtonVH != null) {
                viewUserGamesButtonVH.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGamesClick(user);
                    }
                });
            }
        }
    }
}