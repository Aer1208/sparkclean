package com.ftoul.bi

/**
  * 清洗转换的接口类
  */
trait IClean {

  /**
    * 数据清洗主体函数
    * @param value
    * @return
    */
  def clean(value:Any):Any;
}
