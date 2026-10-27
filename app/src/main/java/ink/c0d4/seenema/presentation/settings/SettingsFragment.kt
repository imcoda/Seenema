package ink.c0d4.seenema.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ink.c0d4.seenema.BuildConfig
import ink.c0d4.seenema.R
import ink.c0d4.seenema.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsItems()
        binding.itemTroubleshoot.root.visibility = View.GONE
        binding.itemAppSuggestion.root.visibility = View.GONE
        binding.itemDonation.root.visibility = View.GONE
        binding.itemAboutMe.root.visibility = View.GONE
    }

    private fun setupSettingsItems() {
        // Troubleshoot
        binding.itemTroubleshoot.itemSettingTitle.text = getString(R.string.learn_how_to_use)
        binding.itemTroubleshoot.itemSettingIcon.setImageResource(R.drawable.ic_troubleshoot)
        binding.itemTroubleshoot.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_howToUseFragment)
        }

        // Language
        binding.itemLanguage.itemSettingTitle.text = getString(R.string.change_language)
        binding.itemLanguage.itemSettingIcon.setImageResource(R.drawable.ic_language)
        binding.itemLanguage.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageFragment)
        }

        // App Suggestion
        binding.itemAppSuggestion.itemSettingTitle.text = getString(R.string.app_suggestion)
        binding.itemAppSuggestion.itemSettingIcon.setImageResource(R.drawable.ic_play_arrow)
        binding.itemAppSuggestion.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_moreAppsFragment)
        }

        // About Me
        binding.itemAboutMe.itemSettingTitle.text = getString(R.string.about_me)
        binding.itemAboutMe.itemSettingIcon.setImageResource(R.drawable.user_outline)
        binding.itemAboutMe.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutMeFragment)
        }

        // Donation
        binding.itemDonation.itemSettingTitle.text = getString(R.string.donation)
        binding.itemDonation.itemSettingIcon.setImageResource(R.drawable.ic_donation)
        binding.itemDonation.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_donationFragment)
        }

        // Source Code
        binding.itemSourceCode.itemSettingTitle.text = getString(R.string.source_code)
        binding.itemSourceCode.itemSettingIcon.setImageResource(R.drawable.ic_source_code)
        binding.itemSourceCode.root.setOnClickListener {
            openUrl("https://github.com/imcoda/Seenema")
        }

        // Backup
        binding.itemBackup.itemSettingTitle.text = getString(R.string.backup_restore)
        binding.itemBackup.itemSettingIcon.setImageResource(R.drawable.ic_calendar_check) // Using a placeholder icon
        binding.itemBackup.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_backupFragment)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}