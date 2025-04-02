package com.example.gamelend.Clases;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<Game> gameList;

    public GameAdapter(Context context, List<Game> gameList) {
        this.context = context;
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.tvNombreJuego.setText(game.getNombre());
        holder.imageViewJuego.setImageResource(game.getImagenResource());
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreJuego;
        ImageView imageViewJuego;
        CardView cardView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreJuego = itemView.findViewById(R.id.tvNombreJuego);
            imageViewJuego = itemView.findViewById(R.id.imageViewJuego);
            cardView = (CardView) itemView;
        }
    }
}
