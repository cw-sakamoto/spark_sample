package example.ch05

/**
  * Created by sakamoto_ryo on 16/09/01.
  */

import java.sql.Timestamp
import java.text.SimpleDateFormat

case class Trip (id: Int, duration: Int,
  startDate: java.sql.Timestamp, startStation: String, startTerminal: Int,
  endDate: java.sql.Timestamp, endStation: String, endTerminal: Int,
  bikeNum: Int, subscriberType: String, zipcode: String)

object Trip {

  def parse(line: String): Trip = {
    val dateFormat = new SimpleDateFormat("MM/dd/yyy HH:mm")
    val elms = line.split(",")

    val id = elms(0).toInt
    val duration = elms(1).toInt
    val startDate = new java.sql.Timestamp(dateFormat.parse(elms(2)).getTime)
    val startStation = elms(3)
    val startTerminal = elms(4).toInt
    val endDate = new java.sql.Timestamp(dateFormat.parse(elms(5)).getTime)
    val endStation = elms(6)
    val endTerminal = elms(7).toInt
    val bikeNum = elms(8).toInt
    val subscriberType = elms(9)
    val zipcode = elms(10)
    Trip(id, duration,
      startDate, startStation, startTerminal,
      endDate, endStation, endTerminal,
      bikeNum, subscriberType, zipcode)
  }
}
