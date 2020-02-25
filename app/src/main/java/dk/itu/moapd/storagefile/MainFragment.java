package dk.itu.moapd.storagefile;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment {

    private static final String KEY_WRITE_READ = "KEY_WRITE_READ";
    private static final String filename = "ActorNames.txt";

    private RecyclerView mRecyclerView;

    private ActorNameAdapter mAdapter;
    private List<String> mActorNames;

    private int mPosition;
    private View mPrevSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        final EditText editText = view.findViewById(R.id.edit_text);

        Button writeButton = view.findViewById(R.id.write_button);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String actorName = editText.getText().toString();
                writeDataToFile(actorName);
                editText.setText("");
                updateUI();
            }
        });

        Button readButton = view.findViewById(R.id.read_button);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI();
            }
        });

        mPosition = -1;
        Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDataFromFile(mPosition);
            }
        });

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    private void updateUI() {
        mActorNames = readDataFromFile();
        mAdapter = new ActorNameAdapter(mActorNames);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void writeDataToFile(String data) {

        if (data.isEmpty()) {
            Toast.makeText(getContext(),
                    "The data can not be empty.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileOutputStream fileOutputStream
                    = Objects.requireNonNull(getContext())
                        .openFileOutput(filename, Context.MODE_APPEND);

            byte[] dataInBytes = data.getBytes();
            String lineSeparator = System.getProperty("line.separator");

            fileOutputStream.write(dataInBytes);
            fileOutputStream.write(
                    Objects.requireNonNull(lineSeparator).getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(getContext(),
                    "Data successfully recorded.",
                    Toast.LENGTH_LONG).show();
        } catch(IOException ex) {
            Log.e(KEY_WRITE_READ, ex.getMessage(), ex);
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean isExternalStorageReadableOnly() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private List<String> readDataFromFile() {
        List<String> data = new ArrayList<>();

        try {
            FileInputStream fileInputStream
                    = Objects.requireNonNull(getContext())
                        .openFileInput(filename);
            InputStreamReader inputStreamReader
                    = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader
                    = new BufferedReader(inputStreamReader);

            String lineData = bufferedReader.readLine();
            while (lineData != null) {
                data.add(lineData);
                lineData = bufferedReader.readLine();
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            Toast.makeText(getContext(),
                    "Load data complete.",
                    Toast.LENGTH_LONG).show();
        } catch(IOException ex) {
            Log.e(KEY_WRITE_READ, ex.getMessage(), ex);
        }

        return data;
    }

    private void deleteDataFromFile(int position) {

        if (position < 0 || position > mActorNames.size()) {
            Toast.makeText(getContext(),
                    "Select a valid bike name.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileOutputStream fileOutputStream
                    = Objects.requireNonNull(getContext())
                        .openFileOutput(filename, Context.MODE_PRIVATE);

            String lineSeparator = System.getProperty("line.separator");

            for (int i = 0; i < mActorNames.size(); i++) {
                if (i == position)
                    continue;

                fileOutputStream.write(mActorNames.get(i).getBytes());
                fileOutputStream.write(
                        Objects.requireNonNull(lineSeparator).getBytes());
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            mActorNames.remove(position);
            mAdapter = new ActorNameAdapter(mActorNames);
            mRecyclerView.setAdapter(mAdapter);

            Toast.makeText(getContext(),
                    "Data successfully deleted.",
                    Toast.LENGTH_LONG).show();
        } catch(IOException ex) {
            Log.e(KEY_WRITE_READ, ex.getMessage(), ex);
        }
    }

    private class ActorNameAdapter
            extends RecyclerView.Adapter<ActorNameHolder> {

        private final List<String> mActorNames;

        ActorNameAdapter(List<String> actorNames) {
            mActorNames = actorNames;
        }

        @NonNull
        @Override
        public ActorNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater =
                    LayoutInflater.from(parent.getContext());
            return new ActorNameHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ActorNameHolder holder, int position) {
            String bikeName = mActorNames.get(position);
            holder.bind(bikeName);
        }

        @Override
        public int getItemCount() {
            return mActorNames.size();
        }

    }

    private class ActorNameHolder
            extends RecyclerView.ViewHolder {

        private final TextView mActorName;

        ActorNameHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_actor_name, parent, false));
            mActorName = itemView.findViewById(R.id.actor_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPosition = getAdapterPosition();

                    if (mPrevSelected != null)
                        mPrevSelected.setBackgroundColor(view.getDrawingCacheBackgroundColor());
                    view.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    mPrevSelected = view;
                }
            });
        }

        void bind(String bikeName) {
            mActorName.setText(bikeName);
        }

    }

}
