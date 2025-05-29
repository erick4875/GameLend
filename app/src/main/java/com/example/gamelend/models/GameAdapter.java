package com.example.gamelend.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final Context context;
    private final List<Game> gameList;
    private final OnGameItemClickListener onItemClickListener;

    /**
     * Interfaz para manejar clics en los ítems del RecyclerView.
     * La Activity que usa este adaptador implementará esta interfaz.
     */
    public interface OnGameItemClickListener {
        void onGameItemClick(Game game);
    }

    /**
     * Constructor del GameAdapter.
     * @param context El contexto de la aplicación o Activity.
     * @param initialGameList Una lista inicial de juegos para mostrar (puede ser vacía).
     * @param listener El listener para los eventos de clic en los ítems.
     */
    public GameAdapter(Context context, List<Game> initialGameList, OnGameItemClickListener listener) {
        this.context = context;
        this.gameList = new ArrayList<>(initialGameList);
        this.onItemClickListener = listener;
    }

    /**
     * Método para actualizar la lista de juegos que muestra el adaptador.
     * @param newGameList La nueva lista de juegos a mostrar.
     */
    public void submitList(List<Game> newGameList) {
        this.gameList.clear();
        if (newGameList != null) {
            this.gameList.addAll(newGameList);
        }
        notifyDataSetChanged();
    }

    /**
     * Se llama cuando el RecyclerView necesita crear un nuevo ViewHolder.
     * Un ViewHolder contiene las vistas para un solo ítem de la lista.
     * @param parent El ViewGroup al que se añadirá la nueva vista (el RecyclerView).
     * @param viewType El tipo de vista (útil si tienes múltiples tipos de ítems, no es el caso aquí).
     * @return Un nuevo GameViewHolder que contiene la vista para un ítem.
     */
    @NonNull // Indica que el método nunca devolverá null.
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    /**
     * Se llama cuando el RecyclerView necesita mostrar los datos de un ítem en una posición específica.
     * Aquí es donde se asignan los datos del objeto 'Game' a las vistas dentro del ViewHolder.
     * @param holder El ViewHolder que debe ser actualizado para representar el contenido del ítem.
     * @param position La posición del ítem dentro del conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        // Obtiene el objeto 'Game' actual de la lista según su posición.
        Game currentGame = gameList.get(position);

        holder.gameNameTextView.setText(currentGame.getName());

        if (currentGame.getImageResourceId() != 0) {
            holder.gameImageView.setImageResource(currentGame.getImageResourceId());
        } else {
            holder.gameImageView.setImageResource(R.drawable.mando);
        }

        // Configurar un listener de clic
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // Llama al método de la interfaz, pasando el juego que fue clickeado.
                onItemClickListener.onGameItemClick(currentGame);
            }
        });
    }

    /**
     * Devuelve el número total de ítems en el conjunto de datos que maneja el adaptador.
     * @return El tamaño de la lista de juegos.
     */
    @Override
    public int getItemCount() {
        return gameList != null ? gameList.size() : 0; // Devuelve 0 si la lista es nula para evitar errores.
    }

    /**
     * Clase interna ViewHolder.
     * Representa la vista de un solo ítem en el RecyclerView y mantiene referencias
     * a las subvistas (TextView, ImageView, etc.) dentro de ese ítem para un acceso eficiente.
     */
    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView gameNameTextView;
        ImageView gameImageView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameNameTextView = itemView.findViewById(R.id.tvGameName);
            gameImageView = itemView.findViewById(R.id.imageViewGame);
        }
    }
}