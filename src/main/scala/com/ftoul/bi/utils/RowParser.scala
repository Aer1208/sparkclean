/**
  *  @Title
  *
  * @Author xiaohaifang on 2018/8/30
  * @Copyright @
  * @Descript: 数据解析类
  */
package com.ftoul.bi.utils

import java.util.regex.{Matcher, Pattern}

import com.ftoul.bi.IClean
import com.ftoul.bi.clean.impl.TrimClean
import org.w3c.dom.{Document, Element}
class RowParser(var cleanConfFile: String) extends Serializable {

  val document:Document = CleanUtils.getDocument(cleanConfFile)
  val rootElement: Element = document.getDocumentElement

  /**
    * 获取源文件分隔符
    * @return
    */
  def getSepStr() : String = {
    if (rootElement.hasAttribute("sepStr")) {
      return rootElement.getAttribute("sepStr")
    }
    return "\001"
  }

  /**
    * 获取源文件字段记录数
    * @return
    */
  def getFieldCnt():Int = Integer.parseInt(rootElement.getAttribute("fieldCnt"))

  /**
    * 获取源文件的文件类型 FIX：定长数据源,COL：字符分隔符数据源
    * @return
    */
  def getFileType():String = rootElement.getAttribute("type")

  /**
    * 清洗后输出字段分隔符
    * @return
    */
  def getOutSepStr(): String = {
    if (rootElement.hasAttribute("outSepStr")) {
      return rootElement.getAttribute("outSepStr")
    }
    return  getSepStr()
  }

  /**
    * 解析由分隔符构成记录数的数据源
    * @param row
    */
  def parserColRow(row:String) = {
    val filedValues:Array[String] = row.split(getSepStr, getFieldCnt)
    var result :StringBuffer = new StringBuffer()

    val nodes = rootElement.getElementsByTagName("column")
    for (i <- 1 to nodes.getLength) {
      val node :Element = nodes.item(i - 1).asInstanceOf[Element]
      val fieldType :String = node.getAttribute("type")
      val fieldIndex : Int = Integer.parseInt(node.getAttribute("index"))
      val fieldValue = filedValues(fieldIndex - 1)
      if ("bit".equalsIgnoreCase(fieldType)) {
        result.append(if ("true".equals(fieldValue)) 1 else 0)
      } else {
        val clean:IClean = getClean(node)
        result.append(clean.clean(fieldValue))
      }
      if (i < nodes.getLength) {
        result.append(getOutSepStr())
      }
    }
    result.toString
  }

  /**
    * 解析定长数据源记录
    * @param row
    * @return
    */
  def parserFixRow(row:String): String = {
    val sb = new StringBuffer

    val nodes = rootElement.getElementsByTagName("column")
    for (ind <- 1 to nodes.getLength) {
      val n: Element = nodes.item(ind - 1).asInstanceOf[Element]
      val indexDesc = n.getAttribute("index")
      val indexs = indexDesc.split("-|,")
      if (indexs != null && indexs.length == 2) {
        val begin = indexs(0).toInt
        var end = indexs(1).toInt
        if (end <= row.length) end = row.length
        val sourceValue = row.substring(begin, end)
        val clean = getClean(n)
        sb.append(clean.clean(sourceValue))
      }
      if (ind < nodes.getLength) {
        sb.append(getOutSepStr())
      }
    }
    sb.toString
  }

  def parserRow(row:String):String = {
    if ("FIX".equalsIgnoreCase(getFileType())) {
      parserFixRow(row)
    }
    parserColRow(row)
  }

  /**
    * 获取清洗对象
    * @param node
    * @return
    */
  def getClean(node:Element):IClean = {
    var clean:IClean = null;
    if (node.hasAttribute("clean")) {
      clean = CleanUtils.getClean(node.getAttribute("clean"))
      if (node.hasAttribute("params") && clean != null) {
        var attr: String = node.getAttribute("params")
        if (attr != null && !"".equals(attr)) {
          var attrs:Array[String] = attr.split(";")
          attrs.foreach(attrPair => {
            val mathcer: Matcher = Pattern.compile("([^=]+)=([^=]+)").matcher(attrPair)
            if (mathcer.matches()) {
              val properties:String = mathcer.group(1)
              val value:String = mathcer.group(2)
              val clazz = clean.getClass()
              val field = clazz.getDeclaredField(properties)
              field.setAccessible(true)
              field.set(clean, value)
            }
          })
        }
      }
    }
    if (clean == null) clean = new TrimClean
    clean
  }

}
