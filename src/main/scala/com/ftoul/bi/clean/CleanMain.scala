package com.ftoul.bi.clean

import com.ftoul.bi.utils.RowParser
import org.apache.spark.{SparkConf, SparkContext}

object CleanMain {
  def main(args: Array[String]):Unit = {
    val argLen = args.length;

    if (argLen < 3) {
      println("Usage spark-submit --jars sparkclean.jar --class com.ftoul.bi.clean.CleanMain <cleanConfFile> <inputs...> <output>")
      System.exit(1)
    }

    val cleanConfFile = args(0)
    val inputPaths = new Array[String](argLen-2)
    System.arraycopy(args,1,inputPaths,0,argLen-2)
    var outputPath = args(argLen-1)

    val conf = new SparkConf().setAppName("CleanMain")
    val sc = new SparkContext(conf)
    val rowParser: RowParser = new RowParser(cleanConfFile)
    inputPaths.foreach(path => {
      sc.textFile(path + "/*").map(parserRow).saveAsTextFile(outputPath)
      def parserRow(row:String):String = {
        rowParser.parserRow(row)
      }
    })
  }
}
