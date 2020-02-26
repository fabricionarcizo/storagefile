package dk.itu.moapd.storagefile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

class MainFragment : Fragment() {

    companion object {
        private const val KEY_WRITE_READ = "KEY_WRITE_READ"
        private const val filename = "ActorNames.txt"
    }

    private var mAdapter: ActorNameAdapter? = null
    private var mActorNames = ArrayList<String>()

    private var mPosition = -1
    private var mPrevSelected: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        write_button.setOnClickListener {
            val actorName = edit_text.text.toString()
            writeDataToFile(actorName)
            edit_text.setText("")
            updateUI()
        }

        read_button.setOnClickListener {
            updateUI()
        }

        delete_button.setOnClickListener {
            deleteDataFromFile(mPosition)
        }

        recycler_view.layoutManager = LinearLayoutManager(activity)
        updateUI()
    }

    private fun updateUI() {
        mActorNames = readDataFromFile()
        mAdapter = ActorNameAdapter(mActorNames)
        recycler_view.adapter = mAdapter
    }

    private fun writeDataToFile(data: String) {
        if (data.isEmpty()) {
            Toast.makeText(context,
                "The data can not be empty.",
                Toast.LENGTH_LONG).show()
            return
        }

        try {
            val fileOutputStream =
                context?.openFileOutput(filename, Context.MODE_APPEND)

            val dataInBytes = data.toByteArray()
            val lineSeparator = System.getProperty("line.separator")

            fileOutputStream?.write(dataInBytes)
            fileOutputStream?.write(
                lineSeparator?.toByteArray()!!)
            fileOutputStream?.flush()
            fileOutputStream?.close()

            Toast.makeText(context,
                "Data successfully recorded.",
                Toast.LENGTH_LONG).show()
        } catch (ex: IOException) {
            Log.e(KEY_WRITE_READ, ex.message, ex)
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun isExternalStorageReadableOnly(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    private fun readDataFromFile(): ArrayList<String> {
        val data = ArrayList<String>()

        try {
            val fileInputStream =
                context?.openFileInput(filename) as FileInputStream
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var lineData = bufferedReader.readLine()
            while (lineData != null) {
                data.add(lineData)
                lineData = bufferedReader.readLine()
            }

            bufferedReader.close()
            inputStreamReader.close()
            fileInputStream.close()

            Toast.makeText(context,
                "Load data complete.",
                Toast.LENGTH_LONG).show()

        } catch (ex: IOException) {
            Log.e(KEY_WRITE_READ, ex.message, ex)
        }

        return data
    }

    private fun deleteDataFromFile(position: Int) {
        if (position < 0 || position > mActorNames.size) {
            Toast.makeText(context,
                "Select a valid actor name.",
                Toast.LENGTH_LONG).show()
            return
        }

        try {
            val fileOutputStream =
                context?.openFileOutput(filename, Context.MODE_PRIVATE)
            val lineSeparator = System.getProperty("line.separator")

            for (i in mActorNames.indices) {
                if (i == position)
                    continue

                fileOutputStream?.write(mActorNames[i].toByteArray())
                fileOutputStream?.write(
                    lineSeparator?.toByteArray()!!)
            }

            fileOutputStream?.flush()
            fileOutputStream?.close()

            mActorNames.removeAt(position)
            mAdapter = ActorNameAdapter(mActorNames)
            recycler_view.adapter = mAdapter

            mPosition = -1

            Toast.makeText(context,
                "Data successfully deleted.",
                Toast.LENGTH_LONG).show()

        } catch (ex: IOException) {
            Log.e(KEY_WRITE_READ, ex.message, ex)
        }
    }

    private inner class ActorNameHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val mActorName: TextView = view.findViewById(R.id.actor_name)

    }

    private inner class ActorNameAdapter(val mActorNames: ArrayList<String>) :
        RecyclerView.Adapter<ActorNameHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorNameHolder {
            val layout = layoutInflater.inflate(R.layout.list_actor_name, parent, false)
            return ActorNameHolder(layout)
        }

        override fun getItemCount() = mActorNames.size

        override fun onBindViewHolder(holder: ActorNameHolder, position: Int) {
            val actorName = mActorNames[position]
            holder.apply {
                mActorName.text = actorName
            }
            holder.itemView.setOnClickListener {
                mPosition = position

                if (mPrevSelected != null)
                    mPrevSelected?.setBackgroundColor(it.solidColor)

                it.setBackgroundColor(Color.parseColor("#EEEEEE"))
                mPrevSelected = it
            }

        }

    }

}
