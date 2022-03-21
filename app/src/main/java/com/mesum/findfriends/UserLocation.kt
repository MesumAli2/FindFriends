package com.mesum.findfriends

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*


class UserLocation {
     var user: User? = null
     var geo_point: GeoPoint? = null

     @ServerTimestamp
     var timestamp: Date? = null

     constructor(user: User?, geo_point: GeoPoint?, timestamp: Date?) {
          this.user = user
          this.geo_point = geo_point
          this.timestamp = timestamp
     }

     constructor() {}

     override fun toString(): String {
          return "UserLocation{" +
                  "user=" + user +
                  ", geo_point=" + geo_point +
                  ", timestamp=" + timestamp +
                  '}'
     }
}