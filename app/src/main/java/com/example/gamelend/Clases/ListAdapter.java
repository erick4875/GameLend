package com.example.gamelend.Clases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelend.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Usuario> mData;
    private LayoutInflater mInflater;
    private Context context;

    public ListAdapter(List<Usuario> itemList, Context context){
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = itemList;
    }

    @Override
    public  int getItemCount(){
        return mData.size();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.usuario_cardview, null);
        return new ListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final  int position){
        holder.bindData(mData.get(position));
    }

    public void setItems(List<Usuario> items) {
        mData = items;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        TextView textViewNombre, textViewCiudad;
//        ImageView imageViewUsuario;
//        ImageButton imageButtonJuegos;
        ViewHolder(View itemView){
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewCiudad = itemView.findViewById(R.id.textViewCiudad);
//            imageViewUsuario = itemView.findViewById(R.id.imageViewUsuario);
//            imageButtonJuegos = itemView.findViewById(R.id.imageButtonJuegos);
        }

        void bindData(final Usuario item){
            textViewNombre.setText(item.getNombre());
            textViewCiudad.setText(item.getCiudad());
//            imageViewUsuario.setImageResource(item.getImagen());
        }
    }
}
