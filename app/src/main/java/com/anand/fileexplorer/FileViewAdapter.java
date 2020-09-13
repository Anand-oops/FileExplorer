package com.anand.fileexplorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.FileViewHolder> {
    private Context context;
    private ArrayList<FileItem> fileItemArrayList;
    private clickAction mListener;
    private longClickAction mLongListener;

    public FileViewAdapter(Context context, ArrayList<FileItem> itemArrayList) {
        this.context = context;
        this.fileItemArrayList = itemArrayList;
    }

    public interface longClickAction {
        void OnLongClick(int position);
    }

    public void setOnLongClickListener( longClickAction longListener){
        mLongListener = longListener;
    }
    public interface clickAction {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(clickAction listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public FileViewAdapter.FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_view, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewAdapter.FileViewHolder holder, int position) {

        holder.mImageView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));

        holder.adapterCard.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));

        final FileItem currentItem = fileItemArrayList.get(position);
        holder.mTextView.setText(currentItem.getName());
        if (currentItem.isFolder()) {
            holder.mImageView.setImageResource(R.drawable.ic_folder);
        } else holder.mImageView.setImageResource(getImage(currentItem.getFilePath()));
    }

    private int getImage(String filePath) {
        if (filePath.contains(".jpg"))
            return R.drawable.ic_jpg;
        else if (filePath.contains(".jpeg"))
            return R.drawable.ic_jpg;
        else if (filePath.contains(".doc"))
            return R.drawable.ic_doc;
        else if (filePath.contains(".mp3"))
            return R.drawable.ic_mp3;
        else if (filePath.contains(".mp4"))
            return R.drawable.ic_mp4;
        else if (filePath.contains(".pdf"))
            return R.drawable.ic_pdf;
        else if (filePath.contains(".png"))
            return R.drawable.ic_png;
        else if (filePath.contains(".ppt"))
            return R.drawable.ic_ppt;
        else if (filePath.contains(".xls"))
            return R.drawable.ic_xls;
        else return R.drawable.ic_file;
    }

    @Override
    public int getItemCount() {
        return fileItemArrayList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        private CardView adapterCard;
        private TextView mTextView;
        private ImageView mImageView;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text);
            mImageView = itemView.findViewById(R.id.image);
            adapterCard = itemView.findViewById(R.id.card_layout);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            mListener.OnItemClick(position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mLongListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            mLongListener.OnLongClick(position);
                    }
                    return true;
                }
            });
        }
    }
}
