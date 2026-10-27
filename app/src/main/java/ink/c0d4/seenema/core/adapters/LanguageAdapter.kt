package ink.c0d4.seenema.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ink.c0d4.seenema.databinding.ItemLanguageSelectBinding
import ink.c0d4.seenema.presentation.settings.LanguageFragment

class LanguageAdapter(
    private val languages: List<LanguageFragment.LanguageItem>,
    private val selectedLanguageCode: String,
    private val onLanguageSelected: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemLanguageSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageFragment.LanguageItem) {
            binding.tvLanguageName.text = item.name
            
            // Check if this language is selected
            val isSelected = item.code == selectedLanguageCode
            binding.ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener {
                onLanguageSelected(item.code)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(languages[position])
    }

    override fun getItemCount(): Int = languages.size
}