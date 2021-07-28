package com.ovh.mls.checkConf

object PathStatus extends Enumeration {
    type PathStatus = Value
    val USED, POSSIBLY_USED, UNUSED = Value
}
