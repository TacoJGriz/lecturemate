package com.example.lecturemate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> implements View.OnClickListener {

    private final ArrayList<String> mNames;
    private final ArrayList<String> mDates;
    private final ArrayList<String> mDurs;

    private final ArrayList<String> mURIs;
    private final Context context;

    public FileAdapter(Context context, ArrayList<String> words, ArrayList<String> times, ArrayList<String> durs, ArrayList<String> uris) {
        mNames = words;
        mDates = times;
        this.context = context;
        mDurs = durs;
        mURIs = uris;
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecture_card, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //set the text
        holder.mNameView.setText(mNames.get(position));
        holder.mOtherText.setText(mDurs.get(position) + " | " + mDates.get(position));

    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }


    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mOpenButton.getId()) {
            Toast.makeText(context, holder.mNameView.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mNameView;
        public TextView mOtherText;
        public Button mOpenButton;


        public ViewHolder(View v) {
            super(v);
            mNameView = v.findViewById(R.id.FileNameView);
            mOtherText = v.findViewById(R.id.otherCardText);
            mOpenButton = v.findViewById(R.id.openFIleButton);

            mOpenButton.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            //method to open the file clicked on
            Intent intent = new Intent(context, SearchLecture.class);
            int i = this.getAdapterPosition();
            intent.putExtra("position", i);
            intent.putExtra("uri", mURIs.get(i));
            context.startActivity(intent);
        }
    }
}
