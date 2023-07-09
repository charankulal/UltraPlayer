package com.example.videoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayBackIconsAdapter extends RecyclerView.Adapter<PlayBackIconsAdapter.ViewHolder> {

    private ArrayList<IconModel> iconModels;
    private Context context;

    public PlayBackIconsAdapter(ArrayList<IconModel> iconModels, Context context) {
        this.iconModels = iconModels;
        this.context = context;
    }

    @NonNull
    @Override
    public PlayBackIconsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.icons_layout,parent,false);
        return  new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull PlayBackIconsAdapter.ViewHolder holder, int position) {
        holder.icon.setImageResource(iconModels.get(position).getImageView());
        holder.iconName.setText(iconModels.get(position).getIconTitle());

    }

    @Override
    public int getItemCount() {
        return iconModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconName;
        ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.playback_icon);
            iconName=itemView.findViewById(R.id.icon_title);
        }
    }
}
