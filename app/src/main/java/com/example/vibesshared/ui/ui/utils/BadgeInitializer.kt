package com.example.vibesshared.ui.ui.utils

import com.example.vibesshared.ui.ui.data.Badge
import com.example.vibesshared.ui.ui.repository.BadgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BadgeInitializer {
    fun initializeBadges(badgeRepository: BadgeRepository) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            // Check and initialize First Login badge
            val existingFirstLogin = badgeRepository.getBadge("first_login")
            if (existingFirstLogin == null) {
                val firstLoginBadge = Badge(
                    badgeId = "first_login",
                    name = "First Login",
                    imageUrl = "https://drive.google.com/uc?export=download&id=1s1JNOJs0i0GV85AWRNvCnMGcYxh28DtD",
                    description = "Awarded for logging in for the first time!"
                )
                badgeRepository.addBadge(firstLoginBadge)
            }

            // Check and initialize First Post badge
            val existingFirstPost = badgeRepository.getBadge("first_post")
            if (existingFirstPost == null) {
                val firstPostBadge = Badge(
                    badgeId = "first_post",
                    name = "First Post",
                    imageUrl = "https://drive.google.com/file/d/1mBmg0wrMAO81lqPliUvailSLcvdfDLiL/view", // Replace with your image URL
                    description = "Awarded for making your first post!"
                )
                badgeRepository.addBadge(firstPostBadge)
            }

            // Check and initialize 5 Posts badge
            val existingFivePosts = badgeRepository.getBadge("five_posts")
            if (existingFivePosts == null) {
                val fivePostsBadge = Badge(
                    badgeId = "five_posts",
                    name = "5 Posts",
                    imageUrl = "https://via.placeholder.com/48?text=5+Posts", // Replace with your image URL
                    description = "Awarded for making 5 posts!"
                )
                badgeRepository.addBadge(fivePostsBadge)
            }
        }
    }
}