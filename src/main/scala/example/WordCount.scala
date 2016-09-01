package example

/**
  * Created by sakamoto_ryo on 16/08/25.
  */
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName(this.getClass.toString)
    try {
      conf.get("spark.master")
    } catch {
      case e: NoSuchElementException => conf.setMaster("local[*]")
    }
    val sc = new SparkContext(conf)

    val textFile = sc.textFile("/Users/sakamoto_ryo/Workspace/spark-1.6.2-bin-hadoop2.6/README.md")
    val words = textFile.flatMap(line => line.split(" "))
    val wordCounts = words.map(word => (word, 1)).reduceByKey((a, b) => a + b)

    wordCounts.saveAsTextFile("./wordcounts")
  }

}
