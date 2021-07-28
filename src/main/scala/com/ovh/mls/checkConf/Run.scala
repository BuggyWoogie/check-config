package com.ovh.mls.checkConf

import java.io.FileNotFoundException

import com.typesafe.config.ConfigFactory
import SuperConfig.superConfig
import PathStatus._

object Run extends App{

    def getSourceFileList(sourceDir : java.io.File) : Seq[java.io.File] = {
        if(! sourceDir.exists())
            throw new FileNotFoundException(sourceDir.getAbsolutePath)
        if(! sourceDir.isDirectory)
            throw new IllegalArgumentException(s"$sourceDir should be a directory")
        sourceDir
            .listFiles
            .map(file => if(file.isDirectory) getSourceFileList(file) else Seq(file))
            .flatten
            .filter(file => file.getName.endsWith(".scala") || file.getName.endsWith(".java"))
    }

    def findConfigPathsInFile(file : java.io.File) : Set[String] = {
        val pattern = "\\\"[^\"]+\\\"".r
        scala.io.Source.fromFile(file)
            .getLines()
            .flatMap(pattern.findAllMatchIn)
            .map(_.toString().replaceFirst("\\\"$", "").replaceFirst("^\\\"", ""))
            .toSet
    }

    def extractVariablesFromConfigFile(configFile : String) : Set[String] = {
        val pattern = "\\$\\{[A-Za-z0-9_.-]+\\}".r
        scala.io.Source.fromFile(new java.io.File(configFile))
            .getLines()
            .flatMap(pattern.findAllMatchIn _)
            .map(_.toString.replace("$", "").replace("{", "").replace("}", ""))
            .toSet
    }

    val confPath = if(args.length > 0) args(0) else "application.conf.template"
    val sourcePath =  if(args.length > 1) args(1) else "src"

    println(s"Reading conf file $confPath")
    val config = ConfigFactory.parseFile(new java.io.File(confPath)).resolve()
    val pathSet : Set[String] = config.generatePathSet()
    val configVariables = extractVariablesFromConfigFile(confPath)

    val pathMap = scala.collection.mutable.Map[String, PathStatus]()

    val stringsInSource : Set[String] = getSourceFileList(new java.io.File(sourcePath))
        .flatMap(findConfigPathsInFile).toSet

    stringsInSource.foreach(println)

    // Basic: if the path is found in the source code, it is marked as used, not used otherwise
    pathSet.foreach(path => if((stringsInSource contains path) || (configVariables contains path)) {
        pathMap(path) = USED
    } else pathMap(path) = UNUSED)

    // If an unused path has a sub path that appears in the source code, it is marked as possibly used.
    pathSet
        .filter(path => pathMap(path) == UNUSED )
        .foreach(unusedPath => {
            println("trying "+unusedPath)
            val subpaths = SuperConfig.generateSubPathList(unusedPath)
            println(subpaths)
            if (subpaths.exists(stringsInSource contains _)) {
                println("ok")
                pathMap(unusedPath) = POSSIBLY_USED
            } else
                println("fuck")
        })

    // printing results
    val pathsByStatus = pathMap.groupBy(_._2).toSeq.map {
        case (status, paths) => (status, paths.keys.toSeq.sorted)
    }.toMap

    if(pathsByStatus.getOrElse(USED, Seq()).size > 0) {
        println("Used config paths:")
        pathsByStatus(USED).foreach(x => println(s"- $x"))
    }
    if(pathsByStatus.getOrElse(POSSIBLY_USED, Seq()).size > 0) {
        println("Possibly used config paths:")
        pathsByStatus(POSSIBLY_USED).foreach(x => println(s"- $x"))
    }
    if(pathsByStatus.getOrElse(UNUSED, Seq()).size > 0) {
        println("Unused config paths:")
        pathsByStatus(UNUSED).foreach(x => println(s"- $x"))
    }

    //stringsInSource.foreach(println)

}
