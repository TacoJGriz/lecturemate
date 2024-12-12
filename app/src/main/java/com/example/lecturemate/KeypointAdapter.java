package com.example.lecturemate;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * keypoint adapter for TakeVideo class
 */
public class KeypointAdapter extends RecyclerView.Adapter<KeypointAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final ArrayList<String> mNames;
    private final ArrayList<String> mTimes;
    private final Context context;
    private int i;

    public KeypointAdapter(Context context, ArrayList<String> names, ArrayList<String> times) {
        mNames = names;
        mTimes = times;
        this.context = context;
        i = 0;
    }

    public ArrayList<String> getNames() {
        return mNames;
    }

    public ArrayList<String> getTimes() {
        return mTimes;
    }

    public void addKeyPoint(String time) {
        i++;
        mNames.add("Keypoint " + i);
        mTimes.add(time);
        notifyItemInserted(getItemCount()); //Need to tell recycler that a keypoint was added
        notifyItemRangeChanged(getItemCount(), mNames.size());
    }

    @NonNull
    @Override
    public KeypointAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.key_point, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mEditText.setText(mNames.get(position));

    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }


    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mButton.getId()) {
            Toast.makeText(context, holder.mEditText.getText(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onLongClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mButton.getId()) {
            int pos = holder.getAdapterPosition();
            mNames.remove(pos);

            notifyItemRemoved(pos);
        }
        return false;
    }

    public void removeAt(int position) {
        mNames.remove(position);
        mTimes.remove(position);
        notifyItemRemoved(position); //you need to notify the recycler when you remove a keypoint
        notifyItemRangeChanged(position, mNames.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button mButton;
        public EditText mEditText;

        public ViewHolder(View v) {
            super(v);
            mButton = v.findViewById(R.id.removeKeypointButton);
            mEditText = v.findViewById(R.id.keyPointNameTake);

            //Listener to change text correctly on edit
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String newText = mEditText.getText().toString();
                    mNames.set(getAdapterPosition(), newText);
                }
            });

            mButton.setOnClickListener(this);
            v.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (v.equals(mButton)) {
                removeAt(getAdapterPosition());
            }
        }
    }
}
