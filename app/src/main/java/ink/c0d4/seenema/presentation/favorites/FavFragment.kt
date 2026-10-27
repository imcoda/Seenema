package ink.c0d4.seenema.presentation.favorites

import android.app.ActivityOptions
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.adapters.GalleryTimelineAdapter
import ink.c0d4.seenema.core.adapters.FavAdapters
import ink.c0d4.seenema.core.utils.*
import ink.c0d4.seenema.databinding.FragmentFavBinding
import ink.c0d4.seenema.presentation.MainActivity
import ink.c0d4.seenema.presentation.gallery.GalleryViewerActivity
import ink.c0d4.seenema.presentation.viewmodels.FavMovieViewModel
import ink.c0d4.seenema.domain.model.MovieResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FavFragment : Fragment() {
    private val model: FavMovieViewModel by viewModels()
    private val sharePosterPicker = MediaSharePosterPicker(this)
    private var _binding: FragmentFavBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GalleryTimelineAdapter
    private lateinit var gridAdapter: FavAdapters
    private var compact = false
    private var pendingMediaId: Int? = null
    private val pickStills = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = pendingMediaId ?: return@registerForActivityResult
        pendingMediaId = null
        if (uri == null) return@registerForActivityResult
        // Use the Fragment lifecycle: returning from the picker can precede view creation.
        lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch
                val file = withContext(Dispatchers.IO) { DiaryImages.copy(ctx, uri, "gallery_") }
                try {
                    if (!model.insertFirstGalleryImage(id, file.absolutePath)) file.delete()
                } catch (e: Exception) {
                    file.delete()
                    throw e
                }
            } catch (e: Exception) {
                context?.let { showToast(it, e.message ?: getString(R.string.image_copy_failed)) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compact = requireContext().getSharedPreferences("gallery_display", Context.MODE_PRIVATE)
            .getBoolean("compact", false)
        if (savedInstanceState?.containsKey("pending_still_media") == true)
            pendingMediaId = savedInstanceState.getInt("pending_still_media")
    }
    override fun onSaveInstanceState(outState: Bundle) {
        pendingMediaId?.let { outState.putInt("pending_still_media", it) }
        super.onSaveInstanceState(outState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = GalleryTimelineAdapter(
            onPosterClick = ::openMovie,
            onLongClick = ::showMovieActions,
            onStillClick = { source, image ->
                source.transitionName = GalleryViewerActivity.SHARED_STILL
                val intent = Intent(requireContext(), GalleryViewerActivity::class.java).apply {
                    putExtra(GalleryViewerActivity.MEDIA_ID, image.mediaId)
                    putExtra(GalleryViewerActivity.IMAGE_ID, image.id)
                    putExtra(GalleryViewerActivity.IMAGE_PATH, image.imagePath)
                }
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                    requireActivity(), source, GalleryViewerActivity.SHARED_STILL).toBundle())
            },
            onAddStill = { id -> pendingMediaId = id; pickStills.launch("image/*") }
        )
        gridAdapter = FavAdapters(::openMovie, ::showMovieActions)
        applyDisplayMode()
        binding.galleryViewToggle.setOnClickListener {
            val position = (binding.fragmentFavRv.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: -1
            val anchorId = if (compact) gridAdapter.currentList.getOrNull(position)?.id
                else adapter.currentList.drop(position.coerceAtLeast(0)).firstNotNullOfOrNull { it.movie?.id }
            compact = !compact
            requireContext().getSharedPreferences("gallery_display", Context.MODE_PRIVATE)
                .edit().putBoolean("compact", compact).apply()
            applyDisplayMode(anchorId)
        }
        model.getAllMovieData().observe(viewLifecycleOwner) { movies ->
            adapter.submitMovies(movies)
            gridAdapter.submitList(GalleryTimeline.sections(movies).flatMap { it.movies })
            val empty = movies.isEmpty()
            binding.fragmentFavRv.visibility = if (empty) View.GONE else View.VISIBLE
            binding.fragmentFavPlaceholder.visibility = if (empty) View.VISIBLE else View.GONE
            binding.peekingLogo.visibility = if (empty) View.VISIBLE else View.GONE
            binding.peekingShelf.visibility = if (empty) View.VISIBLE else View.GONE
        }
        model.getAllGalleryImages().observe(viewLifecycleOwner) { adapter.submitImages(it) }
    }

    private fun applyDisplayMode(anchorId: Int? = null) {
        val list = binding.fragmentFavRv
        list.layoutManager = if (compact) GridLayoutManager(requireContext(), 3) else LinearLayoutManager(requireContext())
        list.adapter = if (compact) gridAdapter else adapter
        binding.galleryViewToggle.apply {
            setImageResource(if (compact) R.drawable.ic_gallery_rows else R.drawable.ic_gallery_grid)
            contentDescription = getString(if (compact) R.string.gallery_show_cards else R.string.gallery_show_grid)
            isSelected = compact
            androidx.appcompat.widget.TooltipCompat.setTooltipText(this, contentDescription)
        }
        val target = if (compact) gridAdapter.currentList.indexOfFirst { it.id == anchorId }
            else adapter.currentList.indexOfFirst { it.movie?.id == anchorId && it.movie != null }.let {
                if (it > 0 && adapter.isHeading(it - 1)) it - 1 else it
            }
        if (anchorId != null && target >= 0)
            (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(target, 0)
    }

    private fun openMovie(movie: MovieResult) {
        if (ClickHandler.isClickAllowed() && findNavController().currentDestination?.id == R.id.libraryFragment) {
            findNavController().navigate(R.id.movieDetailsFragment, Bundle().apply {
                putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(movie))
            })
        }
    }
    private fun showMovieActions(target: View, movie: MovieResult) {
        (requireActivity() as? MainActivity)?.showQuickActionMenu(target,
            listOf(
                QuickActionOverlay.ActionItem(R.drawable.baseline_share_24, getString(R.string.share_movie)) {
                    shareMediaCard(this, movie, sharePosterPicker)
                },
                QuickActionOverlay.ActionItem(R.drawable.baseline_delete_24, getString(R.string.btn_remove_from_favorites)) {
                    model.deleteFavMovieData(movie)
                }
            ))
    }
    override fun onDestroyView() {
        binding.fragmentFavRv.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
