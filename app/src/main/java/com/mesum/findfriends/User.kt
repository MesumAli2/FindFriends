package com.mesum.findfriends

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable


@SuppressLint("ParcelCreator")
class User : Parcelable {
    var email: String? = null
    var user_id: String? = null
    var username: String? = null
    var password: String? = null

    constructor(email: String?, user_id: String?, username: String?, pass: String?) {
        this.email = email
        this.user_id = user_id
        this.username = username
        this.password = pass
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        email = `in`.readString()
        user_id = `in`.readString()
        username = `in`.readString()
        password = `in`.readString()
    }

    override fun toString(): String {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + password + '\'' +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(email)
        dest.writeString(user_id)
        dest.writeString(username)
        dest.writeString(password)
    }

    companion object {
        val cREATOR: Parcelable.Creator<User?> = object : Parcelable.Creator<User?> {
            override fun createFromParcel(`in`: Parcel): User? {
                return User(`in`)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }


}