package com.wisatakita.app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wisatakita.app.data.TeamMember
import com.wisatakita.app.databinding.ItemTeamMemberBinding

class TeamAdapter(private val members: List<TeamMember>) :
    RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: TeamMember) {
            val roleColor = member.role.roleColor()
            binding.tvTeamName.text = member.name
            binding.tvTeamNim.text = member.nim
            binding.tvTeamRole.text = member.role
            binding.tvTeamQuote.text = member.quote
            binding.tvTeamRole.background = rounded(roleColor, 24f)
            binding.ivTeamRoleIcon.background = rounded(roleColor, 18f)

            Glide.with(binding.root.context)
                .load(member.photoRes)
                .centerCrop()
                .placeholder(GlidePlaceholders.batik(binding.root.context))
                .into(binding.ivTeamPhoto)

            binding.cardTeamMember.bounceClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(members[position])

    override fun getItemCount() = members.size

    private fun String.roleColor(): Int = when {
        contains("UI", ignoreCase = true) -> Color.parseColor("#E3A33A")
        contains("Frontend", ignoreCase = true) -> Color.parseColor("#20B8C7")
        contains("Feature", ignoreCase = true) -> Color.parseColor("#6AAB4A")
        contains("Content", ignoreCase = true) -> Color.parseColor("#F0C47A")
        contains("Architect", ignoreCase = true) -> Color.parseColor("#4DCAD6")
        else -> Color.parseColor("#E3A33A")
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
}

class TeamSpringItemAnimator : DefaultItemAnimator() {
    private val spring = OvershootInterpolator(1.8f)

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.scaleX = 0.92f
        holder.itemView.scaleY = 0.92f
        ViewCompat.animate(holder.itemView)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(420L)
            .setInterpolator(spring)
            .start()
        return super.animateAdd(holder)
    }
}
