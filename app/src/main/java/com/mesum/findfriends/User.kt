package com.mesum.findfriends

import android.app.Application

data class User (var name : String,
var email: String,
var uid :String)
class UserClient : Application() {
    var user: User? = null
}