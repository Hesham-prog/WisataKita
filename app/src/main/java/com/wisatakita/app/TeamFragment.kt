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
                name = "Nathan Abigail Rahman",
                nim = "2410511036",
                role = "UI/UX & Logo",
                quote = "There is time to kill today",
                photoRes = R.drawable.team_nathan
            ),
            TeamMember(
                name = "Atalla Ahsan Indrayana",
                nim = "2410511039",
                role = "Frontend Developer",
                quote = "Ngopi skuy",
                photoRes = R.drawable.team_atalla
            ),
            TeamMember(
                name = "Athallah Abrar Duano",
                nim = "2410511046",
                role = "Feature Developer",
                quote = "KICAUMANIAAAGHHH!!!!",
                photoRes = R.drawable.team_athallah
            ),
            TeamMember(
                name = "Sulthon D. Arrafi",
                nim = "2410511061",
                role = "Content & Data",
                quote = "Saya doang yang kaya di grup ini semuanya miskin, btw follow ig aku ya guys @sulthdaffa!!!",
                photoRes = R.drawable.team_sulthon
            ),
            TeamMember(
                name = "Hesham Alsami",
                nim = "2410511066",
                role = "App Architect",
                quote = "HAI, SAYA AKAN LAWAN!",
                photoRes = R.drawable.team_hesham
            )
        )

        binding.recyclerTeam.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTeam.adapter = TeamAdapter(team)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
