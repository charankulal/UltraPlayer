package com.example.videoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videoList;
    private Context context;
    BottomSheetDialog bottomSheetDialog;



    public VideoFilesAdapter(ArrayList<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.video_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videoList.get(position).getDisplayName());
        String size=videoList.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));
        double milliseconds= Double.parseDouble(videoList.get(position).getDuration());
        holder.videoDuration.setText(timeConversion((long) milliseconds));

        Glide.with(context).load(new File(videoList.get(position).getPath())).into(holder.thumbnail);
        holder.menu_more.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")

            @Override
            public void onClick(View v) {
                bottomSheetDialog=new BottomSheetDialog(context,R.style.BottomSheetTheme);
                View bsView=LayoutInflater.from(context).inflate(R.layout.video_bs_layout,v.findViewById(R.id.bottom_sheet));

                bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.itemView.performClick();
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Rename to");
                        EditText editText = new EditText(context);
                        String path = videoList.get(position).getPath();
                        final File file = new File(path);
                        String videoName = file.getName();
                        videoName = videoName.substring(0, videoName.lastIndexOf("."));
                        editText.setText(videoName);
                        alertDialog.setView(editText);
                        editText.requestFocus();

                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(editText.getText().toString())) {
                                    Toast.makeText(context, "Can't rename empty file", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String onlyPath = Objects.requireNonNull(file.getParentFile()).getAbsolutePath();
                                String ext = file.getAbsolutePath();
                                ext = ext.substring(ext.lastIndexOf("."));
                                String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                               File newFile = new File(newPath);
                                    boolean rename = file.renameTo(newFile);
                                    if (rename) {
                                        ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                        resolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA + "=?", new String[]
                                                        {file.getAbsolutePath()});
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);
                                        notifyItemChanged(position);

                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Video Renamed", Toast.LENGTH_SHORT).show();

                                        SystemClock.sleep(200);
                                        ((Activity) context).recreate();
                                    } else {
                                        Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.create().show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri=Uri.parse(videoList.get(position).getPath());
                        Intent shareintent= new Intent(Intent.ACTION_SEND);
                        shareintent.setType("video/*");
                        shareintent.putExtra(Intent.EXTRA_STREAM,uri);
                        context.startActivity(Intent.createChooser(shareintent,"Share Video Via"));
                        bottomSheetDialog.dismiss();
                    }
                });

                bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Delete");
                        alertDialog.setMessage("Do you want to delete this video?");
                        alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        Long.parseLong(videoList.get(position).getId()));

                                File file=new File(videoList.get(position).getPath());
                                boolean deleted = file.delete();
                                if(deleted)
                                {
                                    context.getContentResolver().delete(contentUri,null,null);
                                    videoList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,videoList.size());
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, "Can't be deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                        bottomSheetDialog.dismiss();;
                    }
                });

                bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
                        alertDialog.setTitle("Properties");


                        String one="File: "+videoList.get(position).getDisplayName();
                        String path=videoList.get(position).getPath();
                        int indexOfPath=path.lastIndexOf("/");
                        String two="Path: "+path.substring(0,indexOfPath);
                        String three="Size: "+android.text.format.Formatter.formatFileSize(context, Long.parseLong(size));
                        String four="Length: "+timeConversion((long) milliseconds);
                        String formatName=videoList.get(position).getDisplayName();
                        int index= formatName.lastIndexOf(".");
                        String nameFormat=formatName.substring(index+1);
                        String five= "Format: "+nameFormat;

                        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(videoList.get(position).getPath());
                        String height=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        String width=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                        String six ="Resolution: "+width+"x"+height;
                        alertDialog.setMessage(one+"\n\n"+two+"\n\n"+three+"\n\n"+four+"\n\n"+five+"\n\n"+six);
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bsView);
                bottomSheetDialog.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("video_title",videoList.get(position).getDisplayName());
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArrayList",videoList);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail,menu_more;
        TextView videoName,videoSize,videoDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail=itemView.findViewById(R.id.thumbnail);
            menu_more=itemView.findViewById(R.id.video_menu_more);
            videoName=itemView.findViewById(R.id.video_name);
            videoSize=itemView.findViewById(R.id.video_size);
            videoDuration=itemView.findViewById(R.id.video_duration);

        }
    }
    @SuppressLint("DefaultLocale")
    public String timeConversion(long value){
        String videoTime;
        int duration= (int) value;
        int hrs=(duration/3600000);
        int mns=(duration/60000)%60000-hrs*60;
        int secs=duration%60000/1000;
        if(hrs>0)
        {
            videoTime=String.format("%02d:%02d:%02d",hrs,mns,secs);
        }
        else {
            videoTime=String.format("%02d:%02d",mns,secs);
        }
        return videoTime;
    }
    void updateVideoFiles(ArrayList<MediaFiles> files)
    {
        videoList=new ArrayList<>();
        videoList.addAll(files);
        notifyDataSetChanged();
    }

}
