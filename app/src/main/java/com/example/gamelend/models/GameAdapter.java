package com.example.gamelend.models; // Asegúrate que el paquete sea 'models'

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
// import androidx.cardview.widget.CardView; // Descomenta si usas CardView y tienes el ID
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Para cargar imágenes desde URL (si tu models.Game tuviera imageUrl)
import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<Game> gameList; // Sigue usando tu clase 'models.Game'
    // private OnGameItemClickListener onItemClickListener; // Opcional: para clics en los items

    // Constructor: ahora solo necesita el contexto (y el listener opcional)
    // La lista se pasará a través de submitList
    public GameAdapter(Context context, List<Game> initialGameList /*, OnGameItemClickListener listener */) {
        this.context = context;
        this.gameList = new ArrayList<>(initialGameList); // Copia la lista inicial o inicializa vacía
        // this.onItemClickListener = listener;
    }

    // Método para actualizar la lista de juegos en el adaptador
    public void submitList(List<Game> newGameList) {
        this.gameList.clear();
        if (newGameList != null) {
            this.gameList.addAll(newGameList);
        }
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
        // Considera usar DiffUtil para animaciones y mejor rendimiento en actualizaciones
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout XML para cada ítem de la lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        // Obtiene el juego actual de la lista
        Game currentGame = gameList.get(position);
        // Asigna los datos del juego a las vistas del ViewHolder
        holder.gameNameTextView.setText(currentGame.getName()); // Usa el getter de tu clase Game

        // Si tu 'models.Game' tuviera una URL de imagen en lugar de un resource ID:
        // Glide.with(context)
        //    .load(currentGame.getImageUrl()) // Asume que Game tiene getImageUrl()
        //    .placeholder(R.drawable.mando) // Imagen de placeholder mientras carga
        //    .error(R.drawable.mando_error) // Imagen si hay error al cargar
        //    .into(holder.gameImageView);
        // Como tu 'models.Game' actual tiene 'imagenResource' (int):
        if (currentGame.getImageResourceId() != 0) { // Comprueba si el resource ID es válido
            holder.gameImageView.setImageResource(currentGame.getImageResourceId());
        } else {
            holder.gameImageView.setImageResource(R.drawable.mando); // Imagen por defecto
        }

        // Configurar listener de clic para el ítem (opcional)
        /*
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onGameItemClick(currentGame);
            }
        });
        */
    }

    @Override
    public int getItemCount() {
        return gameList != null ? gameList.size() : 0; // Devuelve el tamaño de la lista
    }

    // ViewHolder: Mantiene las referencias a las vistas de cada ítem
    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView gameNameTextView;  // Antes tvNombreJuego
        ImageView gameImageView; // Antes imageViewJuego
        // CardView gameCardView; // Descomenta si usas CardView y tienes el ID

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            // Enlaza las variables con los IDs del layout item_game.xml
            gameNameTextView = itemView.findViewById(R.id.tvNombreJuego); // Asegúrate que este ID exista en item_game.xml
            gameImageView = itemView.findViewById(R.id.imageViewJuego); // Asegúrate que este ID exista
            // gameCardView = itemView.findViewById(R.id.gameCardView); // Si usas CardView
        }
    }

    // Interfaz opcional para manejar clics en los ítems
    /*
    public interface OnGameItemClickListener {
        void onGameItemClick(Game game);
    }
    */
}