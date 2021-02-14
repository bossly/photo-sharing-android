package me.a01eg.photosharing.model

import java.util.*

/**
 * Model to display photo
 *
 * Created on 22/11/2017.
 * Copyright by 01eg.me
 */

//: Model - contains only information we need
data class Story(
        var uid: String? = null,
        var user: String?,
        var image: String?,
        var timestamp: Date?
)
