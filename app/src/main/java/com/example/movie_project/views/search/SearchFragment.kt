package com.example.movie_project.views.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movie_project.views.profile.ProfileActivity
import com.example.movie_project.models.MovieModel
import com.example.movie_project.views.DetailActivity
import com.example.movie_project.views.theme.MovieMagicTheme

/**
 * Hosts the Compose-based Search UI.
 *
 * The Fragment is intentionally thin: it only wires navigation (to Detail /
 * Profile) and supplies the [SearchViewModel] (built via [SearchViewModel.Factory]
 * so the repository dependency is injected). All UI lives in [SearchRoute].
 */
class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MovieMagicTheme {
                    SearchRoute(
                        viewModel = searchViewModel,
                        onMovieClicked = ::openMovieDetail,
                        onProfileClicked = ::openProfile,
                    )
                }
            }
        }
    }

    private fun openMovieDetail(movie: MovieModel) {
        val intent = Intent(activity, DetailActivity::class.java)
        intent.putExtra("movie", movie)
        startActivity(intent)
    }

    private fun openProfile() {
        startActivity(Intent(activity, ProfileActivity::class.java))
    }
}