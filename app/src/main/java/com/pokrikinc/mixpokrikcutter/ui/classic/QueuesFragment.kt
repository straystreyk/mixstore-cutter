package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Queue
import kotlinx.coroutines.launch

class QueuesFragment : Fragment() {
    private val allQueues = mutableListOf<Queue>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressView: View
    private lateinit var searchView: EditText
    private lateinit var adapter: QueueListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_queues, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setTitleAndBack(
            getString(R.string.title_queues),
            false
        )

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyView = view.findViewById(R.id.empty_view)
        progressView = view.findViewById(R.id.progress_view)
        searchView = view.findViewById(R.id.search_view)
        val refreshButton = view.findViewById<Button>(R.id.refresh_button)

        adapter = QueueListAdapter { item ->
            (requireActivity() as MainActivity).openQueueDetails(item.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        refreshButton.setOnClickListener { loadQueues() }
        searchView.addTextChangedListener {
            updateList(it?.toString().orEmpty())
        }

        loadQueues()
    }

    private fun loadQueues() {
        progressView.visibility = View.VISIBLE
        val appContext = context?.applicationContext ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allQueues.clear()
                allQueues.addAll(RetrofitProvider.getPlotterApi().listQueues())
                if (!isAdded) return@launch
                updateList(searchView.text?.toString().orEmpty())
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        appContext,
                        e.message ?: getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                if (isAdded) {
                    progressView.visibility = View.GONE
                }
            }
        }
    }

    private fun updateList(query: String) {
        val filtered = allQueues.filter { queue ->
            query.isBlank() ||
                queue.name.contains(query, ignoreCase = true) ||
                queue.id.toString().contains(query)
        }.map { queue ->
            QueueListItem(
                id = queue.id,
                title = queue.name,
                subtitle = "ID ${queue.id}"
            )
        }

        adapter.submitList(filtered)
        emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
