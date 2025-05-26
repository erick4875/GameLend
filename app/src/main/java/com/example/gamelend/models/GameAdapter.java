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

// El GameAdapter es responsable de tomar una lista de objetos 'Game'
// y convertirlos en vistas individuales que se mostrarán en un RecyclerView.
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<Game> gameList;
    private OnGameItemClickListener onItemClickListener;

    /**
     * Interfaz para manejar clics en los ítems del RecyclerView.
     * La Activity que usa este adaptador implementará esta interfaz.
     */
    public interface OnGameItemClickListener {
        void onGameItemClick(Game game); // Se llama cuando se hace clic en un juego.
    }

    /**
     * Constructor del GameAdapter.
     * @param context El contexto de la aplicación o Activity.
     * @param initialGameList Una lista inicial de juegos para mostrar.
     * @param listener El listener para los eventos de clic en los ítems.
     */
    public GameAdapter(Context context, List<Game> initialGameList, OnGameItemClickListener listener) {
        this.context = context;
        this.gameList = new ArrayList<>(initialGameList);
        this.onItemClickListener = listener; // Asigna el listener.
    }

    /**
     * Metodo para actualizar la lista de juegos que muestra el adaptador.
     * @param newGameList La nueva lista de juegos a mostrar.
     */
    public void submitList(List<Game> newGameList) {
        this.gameList.clear();
        if (newGameList != null) {
            this.gameList.addAll(newGameList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game currentGame = gameList.get(position);
        holder.gameNameTextView.setText(currentGame.getName());

        if (currentGame.getImageResourceId() != 0) {
            holder.gameImageView.setImageResource(currentGame.getImageResourceId());
        } else {
            holder.gameImageView.setImageResource(R.drawable.mando); // Imagen por defecto
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // Llama al metodo de la interfaz, pasando el juego que fue clickeado.
                onItemClickListener.onGameItemClick(currentGame);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameList != null ? gameList.size() : 0;
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView gameNameTextView;
        ImageView gameImageView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameNameTextView = itemView.findViewById(R.id.tvNombreJuego); // Asegúrate que este ID exista en item_game.xml
            gameImageView = itemView.findViewById(R.id.imageViewJuego); // Asegúrate que este ID exista
        }
    }
}