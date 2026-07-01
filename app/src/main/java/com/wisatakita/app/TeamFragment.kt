package com.wisatakita.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.data.TeamMember
import com.wisatakita.app.databinding.FragmentTeamBinding

class TeamFragment : Fragment() {
    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val team = listOf(
            TeamMember(
                name = getString(R.string.team_name_1),
                nim = getString(R.string.team_nim_1),
                role = getString(R.string.team_role_1),
                quote = getString(R.string.team_quote_1),
                photoRes = R.drawable.team_nathan
            ),
            TeamMember(
                name = getString(R.string.team_name_2),
                nim = getString(R.string.team_nim_2),
                role = getString(R.string.team_role_2),
                quote = getString(R.string.team_quote_2),
                photoRes = R.drawable.team_atalla
            ),
            TeamMember(
                name = getString(R.string.team_name_3),
                nim = getString(R.string.team_nim_3),
                role = getString(R.string.team_role_3),
                quote = getString(R.string.team_quote_3),
                photoRes = R.drawable.team_athallah
            ),
            TeamMember(
                name = getString(R.string.team_name_4),
                nim = getString(R.string.team_nim_4),
                role = getString(R.string.team_role_4),
                quote = getString(R.string.team_quote_4),
                photoRes = R.drawable.team_sulthon
            ),
            TeamMember(
                name = getString(R.string.team_name_5),
                nim = getString(R.string.team_nim_5),
                role = getString(R.string.team_role_5),
                quote = getString(R.string.team_quote_5),
                photoRes = R.drawable.team_hesham
            )
        )

        binding.recyclerTeam.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTeam.itemAnimator = TeamSpringItemAnimator()
        binding.recyclerTeam.adapter = TeamAdapter(team)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
