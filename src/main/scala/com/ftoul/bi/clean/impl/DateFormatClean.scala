/**
  * File: @Title
  *
  * @Author on 2018/8/30
  * @Copyright @
  * @Descript:
  */
package com.ftoul.bi.clean.impl

import com.ftoul.bi.IClean
import java.text.{ParseException, SimpleDateFormat}

class DateFormatClean extends IClean{
  /**
    * 数据清洗主体函数
    *
    * @param value
    * @return
    */

  var fromFormat:String = "yyyy-MM-dd HH:mm:ss";
  var toFormat:String = "yyyyMMdd";

  override def clean(value: Any):Any = {
    val formSdf = new SimpleDateFormat(fromFormat)
    val toSdf = new SimpleDateFormat(toFormat)
    try
      return toSdf.format(formSdf.parse(value.toString))
    catch {
      case e:ParseException => println(e)
    }
  }
}
