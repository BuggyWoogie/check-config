package com.ovh.mls.checkConf

import com.typesafe.config.Config
import scala.jdk.CollectionConverters._

object SuperConfig {
    implicit def superConfig(config : Config) = new SuperConfig(config)

    def generateSubPathList(path : String): Seq[String] = {
        val splitPath = path.split("\\.")
        splitPath.tail.scanLeft(splitPath.head){ case (root, cur) => root+"."+cur }
    }
}

class SuperConfig(val config : Config) {

    def generatePathSet() : Set[String] = {
        config.entrySet().asScala.map(_.getKey).toSet
    }

    def generateSubPathSet(): Set[String] = {
        generatePathSet.toSeq.flatMap(SuperConfig.generateSubPathList).toSet
    }

}
