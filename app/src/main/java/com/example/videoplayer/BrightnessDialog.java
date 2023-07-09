package com.example.videoplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class BrightnessDialog extends AppCompatDialogFragment {

    private TextView brightness_num;
    private ImageView cross;
    private SeekBar seekBar;


    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view =inflater.inflate(R.layout.brightness_dialog_item,null);
        builder.setView(view);

        cross=view.findViewById(R.id.bright_close);
        brightness_num=view.findViewById(R.id.brightness_num);
        seekBar=view.findViewById(R.id.brightness_seekbar);
        int brightness= Settings.System.getInt(getContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,0);
        brightness_num.setText(brightness+"");
        seekBar.setProgress(brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Context context=getContext().getApplicationContext();
                boolean canWrite=Settings.System.canWrite(context);
                if(canWrite){
                    int sBright=progress*255/255;
                    brightness_num.setText(sBright+"");
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,sBright);

                }else{
                    Toast.makeText(context, "Enable write Settings for brightness control", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:"+context.getPackageName()));
                    startActivityForResult(intent,0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder.create();
    }
}











