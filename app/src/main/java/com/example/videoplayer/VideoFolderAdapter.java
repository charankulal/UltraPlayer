package com.example.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    private Context context;

    public VideoFolderAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFolderAdapter.ViewHolder holder, int position) {

        int indexPath=folderPath.get(position).lastIndexOf("/");
        String nameOfFolder=folderPath.get(position).substring(indexPath+1);
        holder.folderName.setText(nameOfFolder);
        holder.folder_path.setText(folderPath.get(position));

        holder.numoffiles.setText(numberOfFiles(folderPath.get(position))+" Videos");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,VideoFilesActivity.class);
                intent.putExtra("folderName",nameOfFolder);
                context.startActivity(intent);
            }
        });
    }

    int numberOfFiles(String folderName)
    {
        int count=0;
        for(MediaFiles mediaFiles1: mediaFiles)
        {
            if(mediaFiles1.getPath().substring(0,mediaFiles1.getPath().lastIndexOf("/")).equalsIgnoreCase(folderName))
            {
                count += 1;

            }
        }
        return count;
    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
    TextView folderName,folder_path,numoffiles;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName=itemView.findViewById(R.id.folderName);
            folder_path=itemView.findViewById(R.id.folderPath);
            numoffiles=itemView.findViewById(R.id.noOfFiles);
        }
    }

}
