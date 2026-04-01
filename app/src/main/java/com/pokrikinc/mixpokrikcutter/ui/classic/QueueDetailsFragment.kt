package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Order
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QueueDetailsFragment : Fragment() {
    private val orderStates = mutableListOf<QueueOrderState>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressView: View
    private lateinit var summaryView: TextView
    private lateinit var currentView: TextView
    private lateinit var prevButton: Button
    private lateinit var printButton: Button
    private lateinit var skipButton: Button

    private var queueId: Int = 0
    private var queueName: String = ""
    private var currentIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_queue_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        queueId = requireArguments().getInt(ARG_QUEUE_ID)
        recyclerView = view.findViewById(R.id.recycler_view)
        emptyView = view.findViewById(R.id.empty_view)
        progressView = view.findViewById(R.id.progress_view)
        summaryView = view.findViewById(R.id.queue_summary)
        currentView = view.findViewById(R.id.queue_current)
        prevButton = view.findViewById(R.id.button_prev)
        printButton = view.findViewById(R.id.button_print)
        skipButton = view.findViewById(R.id.button_skip)

        val adapter = OrderListAdapter()
        adapter.onItemDoubleTap = { position, item ->
            if (position in orderStates.indices) {
                currentIndex = position
                render(adapter)
                Toast.makeText(
                    requireContext(),
                    "Выбран заказ ${item.id}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        prevButton.setOnClickListener {
            rewindProgress()
            render(adapter)
        }
        skipButton.setOnClickListener {
            skipCurrentPart()
            render(adapter)
        }
        printButton.setOnClickListener {
            printCurrentPart(adapter)
        }

        progressView.visibility = View.VISIBLE
        val appContext = context?.applicationContext ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val queue = RetrofitProvider.getPlotterApi().getQueue(queueId)
                if (!isAdded) return@launch

                queueName = queue.name
                (requireActivity() as MainActivity).setTitleAndBack(queueName, true)

                orderStates.clear()
                orderStates.addAll(queue.orders.orEmpty().map { order ->
                    val parts = order.parts.orEmpty()
                    if (order.isPrinted && parts.none { it.isPrinted }) {
                        parts.forEach { it.isPrinted = true }
                    }
                    QueueOrderState(
                        order = order,
                        printedParts = parts.count { it.isPrinted }
                    )
                })
                currentIndex = if (orderStates.isNotEmpty()) 0 else 0
                render(adapter)
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

    private fun render(adapter: OrderListAdapter) {
        val hasOrders = orderStates.isNotEmpty()
        emptyView.visibility = if (hasOrders) View.GONE else View.VISIBLE
        orderStates.forEach { it.syncPrintedParts() }

        val completedOrders = orderStates.count { it.isCompleted }
        summaryView.text = getString(
            R.string.queue_summary_format,
            queueName,
            completedOrders,
            orderStates.size
        )

        val currentOrder = orderStates.getOrNull(currentIndex)
        currentView.text = if (currentOrder == null) {
            getString(R.string.queue_complete)
        } else {
            getString(
                R.string.queue_current_format,
                currentIndex + 1,
                orderStates.size,
                currentOrder.order.name,
                currentOrder.printedParts,
                currentOrder.totalParts
            )
        }

        adapter.submitList(orderStates.mapIndexed { index, state ->
            OrderListItem(
                id = state.order.id,
                title = getString(R.string.queue_order_title_format, state.order.id),
                subtitle = getString(
                    R.string.queue_order_subtitle_format,
                    state.order.name,
                    state.totalParts
                ),
                status = when {
                    index == currentIndex -> getString(
                        R.string.queue_status_current_format,
                        state.printedParts,
                        state.totalParts
                    )
                    state.isCompleted -> getString(R.string.queue_status_done)
                    else -> getString(
                        R.string.queue_status_pending_format,
                        state.printedParts,
                        state.totalParts
                    )
                },
                isCompleted = state.isCompleted,
                isCurrent = index == currentIndex
            )
        })

        prevButton.isEnabled = hasOrders
        skipButton.isEnabled = hasOrders
        printButton.isEnabled = currentOrder?.hasPendingParts == true
        if (adapter.itemCount > 0) {
            recyclerView.scrollToPosition(currentIndex.coerceIn(0, adapter.itemCount - 1))
        }
    }

    private fun rewindProgress() {
        if (orderStates.isEmpty()) {
            currentIndex = 0
            return
        }

        val currentOrder = orderStates.getOrNull(currentIndex) ?: return
        val parts = currentOrder.order.parts.orEmpty()
        val lastPrintedIndex = parts.indexOfLast { it.isPrinted }

        if (lastPrintedIndex >= 0) {
            parts[lastPrintedIndex].isPrinted = false
            currentOrder.printedParts = (currentOrder.printedParts - 1).coerceAtLeast(0)
            return
        }

        moveToPreviousOrder()
    }

    private fun skipCurrentPart() {
        if (orderStates.isEmpty()) {
            currentIndex = 0
            return
        }

        val currentOrder = orderStates.getOrNull(currentIndex) ?: return
        val nextPart = currentOrder.order.parts.orEmpty().firstOrNull { !it.isPrinted }

        if (currentOrder.isCompleted || nextPart == null) {
            moveToNextOrder(forceAdvance = true)
            return
        }

        nextPart.isPrinted = true
        currentOrder.printedParts += 1
    }

    private fun printCurrentPart(adapter: OrderListAdapter) {
        val appContext = context?.applicationContext ?: return
        val currentOrder = orderStates.getOrNull(currentIndex)
        if (currentOrder == null || !currentOrder.hasPendingParts) {
            Toast.makeText(appContext, R.string.queue_nothing_to_print, Toast.LENGTH_SHORT).show()
            return
        }

        val nextPart = currentOrder.order.parts.orEmpty()
            .firstOrNull { !it.isPrinted }
            ?: currentOrder.order.parts.orEmpty().getOrNull(currentOrder.printedParts)
        if (nextPart == null) {
            moveToNextOrder(forceAdvance = true)
            render(adapter)
            return
        }

        progressView.visibility = View.VISIBLE
        prevButton.isEnabled = false
        printButton.isEnabled = false
        skipButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = when {
                    !nextPart.cutData.isNullOrBlank() ->
                        PlotterPrintHelper.printRawPltContent(nextPart.cutData)

                    nextPart.attfile.isNotBlank() ->
                        PlotterPrintHelper.printPartByAttFile(appContext, nextPart.attfile)

                    else -> null
                }
                if (!isAdded) return@launch

                if (result?.isSuccess == true) {
                    nextPart.isPrinted = true
                    currentOrder.printedParts += 1
                    if (currentOrder.isCompleted) {
                        notifyStickerPrinted(currentOrder.order.id)
                        moveToNextOrder(forceAdvance = true)
                    }
                    Toast.makeText(appContext, R.string.print_sent, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(appContext, R.string.print_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        appContext,
                        e.message ?: getString(R.string.print_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                if (isAdded) {
                    progressView.visibility = View.GONE
                    render(adapter)
                }
            }
        }
    }

    private suspend fun notifyStickerPrinted(orderId: Int) {
        val printerName = PreferenceManager.getPrinterName().trim()
        if (printerName.isEmpty()) return

        try {
            withContext(Dispatchers.IO) {
                RetrofitProvider.getPlotterApi().printSticker(queueId, orderId, printerName)
            }
        } catch (_: Exception) {
        }
    }

    private fun moveToNextOrder(forceAdvance: Boolean) {
        if (orderStates.isEmpty()) {
            currentIndex = 0
            return
        }

        if (forceAdvance && currentIndex < orderStates.lastIndex) {
            currentIndex += 1
        }
    }

    private fun moveToPreviousOrder() {
        if (currentIndex > 0) {
            currentIndex -= 1
        }
    }

    private data class QueueOrderState(
        val order: Order,
        var printedParts: Int = 0
    ) {
        fun syncPrintedParts() {
            printedParts = order.parts.orEmpty().count { it.isPrinted }
        }

        val totalParts: Int
            get() = order.parts.orEmpty().size

        val hasPendingParts: Boolean
            get() = printedParts < totalParts

        val isCompleted: Boolean
            get() = totalParts == 0 || printedParts >= totalParts
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
