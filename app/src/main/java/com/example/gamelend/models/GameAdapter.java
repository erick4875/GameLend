package com.example.gamelend.models; // Asegúrate que el paquete sea 'models'

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
// import androidx.cardview.widget.CardView; // Descomenta si usas CardView y tienes el ID correcto en item_game.xml
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// import com.bumptech.glide.Glide; // Descomenta si vas a cargar imágenes desde una URL
import com.example.gamelend.R;

import java.util.ArrayList;
import java.util.List;

// El GameAdapter es responsable de tomar una lista de objetos 'Game'
// y convertirlos en vistas individuales que se mostrarán en un RecyclerView.
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context; // Contexto de la aplicación o Activity, necesario para inflar layouts y otras operaciones.
    private List<Game> gameList; // Lista de objetos 'Game' (tu modelo local) que el adaptador mostrará.
    private OnGameItemClickListener onItemClickListener; // Interfaz para manejar clics

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
     * @param initialGameList Una lista inicial de juegos para mostrar (puede ser vacía).
     * @param listener El listener para los eventos de clic en los ítems.
     */
    public GameAdapter(Context context, List<Game> initialGameList, OnGameItemClickListener listener) {
        this.context = context;
        // Es buena práctica crear una nueva copia de la lista para evitar modificar la original externamente.
        this.gameList = new ArrayList<>(initialGameList);
        this.onItemClickListener = listener; // Asigna el listener.
    }

    /**
     * Método para actualizar la lista de juegos que muestra el adaptador.
     * @param newGameList La nueva lista de juegos a mostrar.
     */
    public void submitList(List<Game> newGameList) {
        this.gameList.clear(); // Limpia la lista actual.
        if (newGameList != null) {
            this.gameList.addAll(newGameList); // Añade todos los juegos de la nueva lista.
        }
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado y debe redibujarse.
        // Para listas grandes o actualizaciones frecuentes, considera usar DiffUtil
        // para un mejor rendimiento y animaciones más suaves.
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
        // LayoutInflater se usa para crear (inflar) una vista desde un archivo de layout XML.
        // R.layout.item_game es tu archivo XML que define cómo se ve cada juego en la lista.
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view); // Crea y devuelve el ViewHolder con la vista inflada.
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

        // Asigna los datos del juego a las vistas del ViewHolder.
        // Asumimos que tu clase 'models.Game' tiene los métodos getName() e getImageResourceId().
        holder.gameNameTextView.setText(currentGame.getName());

        // Cargar la imagen del juego.
        // Si 'models.Game' tuviera una URL de imagen (String imageUrl) en lugar de un ID de recurso:
        /*
        if (currentGame.getImageUrl() != null && !currentGame.getImageUrl().isEmpty()) {
            Glide.with(context)
               .load(currentGame.getImageUrl()) // Carga la imagen desde la URL.
               .placeholder(R.drawable.mando) // Muestra esta imagen mientras carga la real.
               .error(R.drawable.mando_error)   // Muestra esta imagen si hay un error al cargar.
               .into(holder.gameImageView);   // El ImageView donde se mostrará la imagen.
        } else {
            holder.gameImageView.setImageResource(R.drawable.mando); // Imagen por defecto si no hay URL.
        }
        */

        // Como tu 'models.Game' actual tiene 'imageResourceId' (un int):
        if (currentGame.getImageResourceId() != 0) { // Comprueba si el ID del recurso es válido (no 0).
            holder.gameImageView.setImageResource(currentGame.getImageResourceId());
        } else {
            holder.gameImageView.setImageResource(R.drawable.mando); // Muestra una imagen por defecto si no hay una específica.
        }

        // Configurar un listener de clic para todo el ítem.
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
        // Declara las vistas que contiene cada ítem de la lista.
        TextView gameNameTextView;  // Para mostrar el nombre del juego.
        ImageView gameImageView; // Para mostrar la imagen del juego.
        // CardView gameCardView; // Descomenta si usas un CardView como raíz del ítem y necesitas referenciarlo.

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameNameTextView = itemView.findViewById(R.id.tvGameName);
            gameImageView = itemView.findViewById(R.id.imageViewGame);
        }
    }
}