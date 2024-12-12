package com.example.lecturemate;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> implements View.OnClickListener {

    private final ArrayList<TranscriptItem> mWords;
    private final Context context;

    public WordAdapter(Context context, ArrayList<TranscriptItem> words) {
        mWords = words;
        this.context = context;
    }

    @NonNull
    @Override
    public WordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.word, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTextView.setText(mWords.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return mWords.size();
    }


    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mTextView.getId()) {
            if(mWords.get(holder.getAdapterPosition()).getClass() == Keypoint.class) {
                Keypoint keypoint = (Keypoint) mWords.get(holder.getAdapterPosition());
                Toast.makeText(context, keypoint.getDescription(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, holder.mTextView.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.word);

            mTextView.setOnClickListener(this);
            v.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            SearchLecture l = (SearchLecture) context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                l.setTime(mWords.get(getAdapterPosition()).getSeconds());
            }
        }
    }
}
