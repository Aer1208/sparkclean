/**
  * File: @Title
  *
  * @Author on 2018/8/30
  * @Copyright @
  * @Descript:
  */
package com.ftoul.bi.clean.impl

import com.ftoul.bi.IClean

class TrimClean extends IClean{
  /**
    * 数据清洗主体函数
    *
    * @param value
    * @return
    */
  override def clean(value: Any):Any = {
    if (value == null || "null".equals(value.toString))  {
      return ""
    }
    return value.toString.trim()
  }
}
