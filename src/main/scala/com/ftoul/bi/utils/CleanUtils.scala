/**
  * File: @Title
  *
  * @author on 2018/8/30
  * */
package com.ftoul.bi.utils

import java.net.URI
import java.util.Properties
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}

import com.ftoul.bi.IClean
import com.ftoul.bi.clean.impl.TrimClean
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.w3c.dom.{Document, Element, NodeList}

/**
  * 清洗程序注册工具类
  */
object CleanUtils {
  // 清洗程序和清洗作业保存对象
  var cleans:Map[String, IClean] = Map[String, IClean]();
  var prop:Properties =null
  if (prop == null || cleans.size ==0) {
    init()
  }
  def init():Unit = {
    prop = new Properties()
    prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("clean.properties"))
    val cleanFile:String = getPropValue("clean_conf_file")
    var document:Document = getDocument(cleanFile)
    val rootElement : Element =  document.getDocumentElement
    val cleanList : NodeList = rootElement.getElementsByTagName("clean")
    for (i <- 1 to cleanList.getLength) {
      val node :Element = cleanList.item(i - 1).asInstanceOf[Element]
      import com.ftoul.bi.IClean
      if (node.hasAttribute("id") && node.hasAttribute("class")) {
        val cleanId = node.getAttribute("id")
        val clazzStr = node.getAttribute("class")
        try {
          val clazz = Class.forName(clazzStr)
          val cleanObj = clazz.newInstance().asInstanceOf[IClean]
          cleans += (cleanId -> cleanObj)
        } catch {
          case e: InstantiationException =>
            e.printStackTrace()
          case e: IllegalAccessException =>
            e.printStackTrace()
          case e: ClassNotFoundException =>
            e.printStackTrace()
        }
      }
   }
  }

  def getPropValue(key:String):String = {
    prop.getProperty(key)
  }

  /**
    * 根据清洗作业ID获取清洗对象
    * @param cleanId
    * @return
    */
  def getClean(cleanId:String):IClean = {
    val clean = cleans.get(cleanId)
    if (clean == null) return new TrimClean
    clean.get
  }

  /**
    * 获取xml的<code>org.w3c.dom.Document</code>
    * @param xmlFile
    * @return
    */
  def getDocument(xmlFile:String): Document = {
    var basePath: String = getPropValue("clean_base_path")
    if (!basePath.endsWith("/")) basePath += "/"
    val cleanPath = basePath + xmlFile
    val conf = new Configuration()
    val fs :FileSystem = FileSystem.get(URI.create(cleanPath),conf)
    val path :Path = new Path((cleanPath))
    val confInputStream = fs.open(path)
    val dbf :DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db : DocumentBuilder = dbf.newDocumentBuilder()
    val document :Document = db.parse(confInputStream)
    document
  }
}
