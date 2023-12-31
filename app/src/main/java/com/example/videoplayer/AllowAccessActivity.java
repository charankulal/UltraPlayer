package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;

import java.lang.annotation.Target;

public class AllowAccessActivity extends AppCompatActivity {
    Button allow_btn;
    public static final int STORAGE_PERMISSION=1;
    public static final int STORAGE_PERMISSION_FOR_13=123;
    public static final int REQUEST_PERMISSION_SETTING=12;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);
        allow_btn=findViewById(R.id.allow_access);


        allow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                        finish();
                    } else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                    }
                }

                else{
                    if(Environment.isExternalStorageManager()){
                        startActivity(new Intent(AllowAccessActivity.this,MainActivity.class));
                        finish();
                    }else{
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                            finish();
                        } else {
                            ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, STORAGE_PERMISSION);
                        }
                        try{
                            Intent intent=new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            Uri uri=Uri.fromParts("package",getPackageName(),null);
                            intent.setData(uri);
                            startActivityForResult(intent,STORAGE_PERMISSION_FOR_13);
                        }catch(Exception e){
                            e.printStackTrace();
                            Intent intent=new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent,STORAGE_PERMISSION_FOR_13);
                        }

            }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSION){
            for (int i=0;i<permissions.length;i++)
            {
                String per=permissions[i];
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    boolean showRationale=shouldShowRequestPermissionRationale(per);
                    if(!showRationale)
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        builder.setTitle("App Permission").setMessage("For using the app allow all the permissions"+"\n\n"+"Follow the below steps"+"\n\n"+
                                "Open setting from below button \n Click on permissions\n Allow access for storage").setPositiveButton("Open Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent= new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                                        intent.setData(uri);
                                        startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();
                    }else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,new String[]{Manifest.permission.READ_MEDIA_VIDEO},STORAGE_PERMISSION);
                    }
                }else {
                    startActivity(new Intent(AllowAccessActivity.this,MainActivity.class));
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==STORAGE_PERMISSION_FOR_13)
        {
            startActivity(new Intent(AllowAccessActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO)==PackageManager.PERMISSION_GRANTED)
        {
            startActivity(new Intent(AllowAccessActivity.this,MainActivity.class));
            finish();
        }
    }
}