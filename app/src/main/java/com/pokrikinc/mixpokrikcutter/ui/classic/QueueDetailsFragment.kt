package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import kotlinx.coroutines.launch

class QueueDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val queueId = requireArguments().getInt(ARG_QUEUE_ID)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)

        val adapter = OrderListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        progressView.visibility = View.VISIBLE
        val appContext = context?.applicationContext ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val queue = RetrofitProvider.getPlotterApi().getQueue(queueId)
                if (!isAdded) return@launch
                (requireActivity() as MainActivity).setTitleAndBack(queue.name, true)

                val items = queue.orders.orEmpty().map { order ->
                    OrderListItem(
                        id = order.id,
                        title = "Заказ ${order.id}",
                        subtitle = "${order.name} • деталей: ${order.parts?.size ?: 0}",
                        status = if (order.isPrinted) "Напечатан" else "Не напечатан"
                    )
                }
                adapter.submitList(items)
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        appContext,
                        e.message ?: getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    emptyView.visibility = View.VISIBLE
                }
            } finally {
                if (isAdded) {
                    progressView.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private const val ARG_QUEUE_ID = "queue_id"

        fun newInstance(queueId: Int): QueueDetailsFragment {
            return QueueDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_QUEUE_ID, queueId)
                }
            }
        }
    }
}
