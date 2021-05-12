package image_classifier

import org.apache.spark.sql.SparkSession

object Main {
	
	def main(args: Array[String]): Unit = {
		import org.apache.log4j.Logger
		import org.apache.log4j.Level
		Logger.getLogger("org").setLevel(Level.OFF)
		val configFile = "/home/fra/Desktop/BD/archive/data.json"
		val configDir = java.nio.file.Paths.get(configFile).getParent.toString
		// TODO Use spark-submit parameters
		val spark = SparkSession
			.builder()
			.appName("Image classification with BOVW")			
			.master("local[*]") 
			.getOrCreate()
		try {
			spark.sparkContext.setLogLevel("WARN")
		}
		finally
		spark.close
	}
	
}
