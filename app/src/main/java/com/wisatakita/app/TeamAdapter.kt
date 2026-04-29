package com.wisatakita.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wisatakita.app.data.TeamMember
import com.wisatakita.app.databinding.ItemTeamMemberBinding

class TeamAdapter(private val members: List<TeamMember>) :
    RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: TeamMember) {
            binding.tvTeamName.text = member.name
            binding.tvTeamNim.text = member.nim
            binding.tvTeamRole.text = member.role
            binding.tvTeamQuote.text = "\"${member.quote}\""
            Glide.with(binding.root.context)
                .load(member.photoRes)
                .centerCrop()
                .placeholder(R.color.colorPrimaryLight)
                .into(binding.ivTeamPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(members[position])

    override fun getItemCount() = members.size
}
