package com.example.vibesshared.ui.ui.data



fun User.toUserProfile(): UserProfile {
    return UserProfile(
        userId = this.userId,
        userName = this.userName,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        dob = this.dob,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun UserProfile.toUser(): User {
    return User(
        userId = this.userId,
        userName = this.userName.toString(),
        firstName = this.firstName.toString(),
        lastName = this.lastName.toString(),
        email = this.email,
        dob = this.dob,
        profilePictureUrl = this.profilePictureUrl
    )
}