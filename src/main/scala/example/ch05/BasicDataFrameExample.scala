/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.ch05

// scalastyle:off println
import java.text.SimpleDateFormat

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SQLContext}
import org.apache.spark.sql.functions._

/**
  * An example code of the basic usage of DataFrame
  *
  * Run with
  * {{{
  * spark-submit --class example.ch05.BasicDataFrameExample \
  *     path/to/gihyo-spark-book-example_2.10-1.0.1.jar
  *     201508_station_data.csv 201508_trip_data.csv
  * }}}
  */
object BasicDataFrameExample {

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      new IllegalArgumentException("Invalid arguments")
      System.exit(1)
    }

    val conf = new SparkConf().setAppName("BasicDataFrameExample")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val stationPath = args(0)
    val tripPath = args(1)
    run(sc, sqlContext, stationPath, tripPath)

    sc.stop()
  }

  def run(
    sc: SparkContext,
    sqlContext: SQLContext,
    stationPath: String,
    tripPath: String): Unit = {
    import sqlContext.implicits._

    // Creates a DataFrame for the station data with a case class
    val stationRDD: RDD[(Int, String, Double, Double, Int, String, java.sql.Date)] =
      sc.textFile(stationPath).
        filter(line => !line.contains("station_id")).
        map { line =>
          val dateFormat = new SimpleDateFormat("MM/dd/yyy")

          val elms = line.split(",")
          val id = elms(0).toInt
          val name = elms(1)
          val lat = elms(2).toDouble
          val lon = elms(3).toDouble
          val dockcount = elms(4).toInt
          val landmark = elms(5)
          val parsedInstallation = dateFormat.parse(elms(6))
          val installation = new java.sql.Date(parsedInstallation.getTime)
          (id, name, lat, lon, dockcount, landmark, installation)
        }
    val stationDF = stationRDD.
      map { case (id, name, lat, lon, dockcount, landmark, installation) =>
        Station(id, name, lat, lon, dockcount, landmark, installation)
      }.toDF().cache()

    // Converts a DataFrame to RDD
    val rdd = stationDF.rdd
    // Uses getters of `Row`
    rdd.foreach { row =>
      val id = row.getInt(0)
      val name = row.getString(1)
      println(s"(id, name) = ($id, $name)")
    }
    rdd.foreach(row => println(s"${row.get(1)}"))

    // Shows the schema of the station DataFrame
    stationDF.printSchema()

    val localStation = stationDF.collect()

    stationDF.select('id, $"name", stationDF("landmark"), col("dockcount")).show()
    stationDF.select('name).orderBy(length('name)).show(5)
    stationDF.groupBy('landmark).count().show()
    stationDF.groupBy('landmark).agg(sum('dockcount)).show()
    stationDF.groupBy(year('installation), month('installation)).count().show()

    val tripDF = sc.textFile(tripPath).
      filter(line => !line.contains("Trip ID")).
      map(_.split(",")).
      filter(_.size == 11).
      map { elms =>
        val dateFormat = new SimpleDateFormat("MM/dd/yyy HH:mm")

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
      }.toDF().cache()

    tripDF.printSchema()
    tripDF.count()
  }
}

// scalastyle:on println
