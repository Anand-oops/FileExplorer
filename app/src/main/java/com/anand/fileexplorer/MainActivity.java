package com.anand.fileexplorer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    ArrayList<FileItem> fileItemArrayList = new ArrayList<>();
    RecyclerView fileRecyclerList;
    FileViewAdapter fileAdapter;
    private boolean isRoot = true;
    TextView textView;
    Button actionButton;
    private File root;
    private String mRootPath = Environment.getExternalStorageDirectory().getPath();
    String outputPath;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileRecyclerList = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.permissionText);
        actionButton = findViewById(R.id.action_button);
        fileRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        if (!(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            textView.setVisibility(View.VISIBLE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            textView.setVisibility(View.GONE);
            viewDirectories(mRootPath);
        }

        LinearLayout linearLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==1){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                textView.setVisibility(View.GONE);
                viewDirectories(mRootPath);
            }else
                Toast.makeText(this,"Now you shall give permissions manually",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkPermission(String permission)
    {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public void viewDirectories(final String pRootPath) {
        Log.i(TAG, "viewDirectories: Root Path " + pRootPath);
        outputPath = pRootPath;
        root = new File(pRootPath);
        fileItemArrayList.clear();
        isRoot = true;
        if (!pRootPath.equals(mRootPath)) {
            isRoot = false;
            String currentPath = root.getPath();
            fileItemArrayList.add(new FileItem(currentPath + "../", true, root.getParent()));
        }
        File[] files = root.listFiles();
        assert files != null;
        for (File file : files) {
            fileItemArrayList.add(new FileItem(file.getName(), file.isDirectory(), file.getAbsolutePath()));
        }
        fileAdapter = new FileViewAdapter(MainActivity.this, fileItemArrayList);
        fileRecyclerList.setAdapter(fileAdapter);
        fileAdapter.setOnItemClickListener(new FileViewAdapter.clickAction() {
            @Override
            public void OnItemClick(int position) {
                FileItem currentItem = fileItemArrayList.get(position);
                if (currentItem.isFolder())
                    viewDirectories(currentItem.getFilePath());
                else {
                    File currentFile = new File(currentItem.getFilePath());
                    Log.i(TAG, "OnItemClick: file " + currentFile.getAbsolutePath());
                    Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", currentFile);
                    String mime = getType(uri.toString());
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mime);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        fileAdapter.setOnLongClickListener(new FileViewAdapter.longClickAction() {
            @Override
            public void OnLongClick(int position) {
                final FileItem currentItem = fileItemArrayList.get(position);
                if (!currentItem.isFolder()) {
                    final File currentFile = new File(currentItem.getFilePath());
                    final CharSequence[] array = {"Copy", "Move", "Delete"};
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Select your action...");
                    dialog.setIcon(R.drawable.ic_file);
                    final CharSequence[] action = new CharSequence[1];
                    dialog.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            action[0] = array[i];
                        }
                    });
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String inputFile = currentFile.getPath();
                            final String inputFileName = currentFile.getName();
                            Log.i(TAG, "onClick: action " + action[0]);
                            switch (action[0].toString()) {
                                case "Copy":
                                    actionButton.setText("Paste here");
                                    viewDirectories(mRootPath);
                                    actionButton.setVisibility(View.VISIBLE);
                                    actionButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            copyFile(inputFile, inputFileName, outputPath);
                                        }
                                    });
                                    break;
                                case "Move":
                                    actionButton.setText("Move here");
                                    viewDirectories(mRootPath);
                                    actionButton.setVisibility(View.VISIBLE);
                                    actionButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            moveFile(inputFile, inputFileName, outputPath);
                                        }
                                    });
                                    break;
                                case "Delete":
                                    currentFile.delete();
                                    viewDirectories(pRootPath);
                                    break;
                            }
                        }
                    });
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();

                }
            }
        });
    }

    private void moveFile(String inputFile, String inputFileName, String pRootPath) {
        Log.i(TAG, "onClick: Input File " + inputFile);
        Log.i(TAG, "onClick: Input File Name " + inputFileName);
        Log.i(TAG, "onClick: Output Path " + pRootPath);

        InputStream fileIn;
        OutputStream fileOut;
        try {
            fileIn = new FileInputStream(inputFile);
            fileOut = new FileOutputStream(pRootPath + "/" + inputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, read);
            }
            fileIn.close();

            fileOut.flush();
            fileOut.close();

            new File(inputFile).delete();
        } catch (Exception e) {
            Log.e("tag", Objects.requireNonNull(e.getMessage()));
        }
        actionButton.setVisibility(View.GONE);
        viewDirectories(pRootPath);
    }

    private void copyFile(String inputFile, String inputFileName, String pRootPath) {
        Log.i(TAG, "onClick: Input File " + inputFile);
        Log.i(TAG, "onClick: Input File Name " + inputFileName);
        Log.i(TAG, "onClick: Output Path " + pRootPath);
        InputStream fileIn;
        OutputStream fileOut;
        try {
            fileIn = new FileInputStream(inputFile);
            fileOut = new FileOutputStream(pRootPath + "/" + inputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, read);
            }
            fileIn.close();

            fileOut.flush();
            fileOut.close();

        } catch (Exception e) {
            Log.e("tag", Objects.requireNonNull(e.getMessage()));
        }
        actionButton.setVisibility(View.GONE);
        viewDirectories(pRootPath);
    }

    private String getType(String s) {
        String url = MimeTypeMap.getFileExtensionFromUrl(s);
        String mimeType = null;
        if (url != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(url);
        }
        Log.i(TAG, "getType: mime" + mimeType);
        return mimeType;
    }

    @Override
    public void onBackPressed() {
        if (isRoot) {
            System.exit(0);
            super.onBackPressed();
        } else
            viewDirectories(root.getParent());

    }
}