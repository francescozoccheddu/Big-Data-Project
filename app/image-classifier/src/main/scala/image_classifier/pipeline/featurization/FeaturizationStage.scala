package image_classifier.pipeline.featurization

import image_classifier.configuration.{FeaturizationConfig, Loader}
import image_classifier.pipeline.Columns.colName
import image_classifier.pipeline.LoaderStage
import image_classifier.pipeline.data.DataStage
import image_classifier.pipeline.featurization.FeaturizationStage.{defaultOutputCol, logger}
import image_classifier.utils.DataTypeImplicits.DataTypeExtension
import image_classifier.utils.FileUtils
import org.apache.log4j.Logger
import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.sql.functions.{col, explode, udf}
import org.apache.spark.sql.types.{BinaryType, BooleanType, DataType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

private[pipeline] final class FeaturizationStage(loader: Option[Loader[FeaturizationConfig]], val dataStage: DataStage, val outputCol: String = defaultOutputCol)(implicit spark: SparkSession, fileUtils: FileUtils)
  extends LoaderStage[DataFrame, FeaturizationConfig]("Featurization", loader)(fileUtils) {

	override protected def make(): DataFrame = {
		validate(dataStage.result.schema)
		logger.info("Extracting features")
		val describedData = describe(config, dataStage.result).cache
		logger.info("Creating codebook")
		val codebook = createCodebook(config, describedData)
		logger.info("Computing BOVW")
		val bowv = BOWV.compute(describedData, codebook, config.codebook.size, outputCol)
		bowv
	}

	private def validate(schema: DataType): Unit = {
		schema.requireField(dataStage.imageCol, BinaryType)
		schema.requireField(dataStage.isTestCol, BooleanType)
	}

	private def describe(config: FeaturizationConfig, data: DataFrame): DataFrame = {
		val descriptor = Descriptor(config.descriptor)
		val describe = udf(descriptor.apply: Array[Byte] => Array[MLVector])
		data.withColumn(outputCol, describe(col(dataStage.imageCol)))
	}

	private def createCodebook(config: FeaturizationConfig, data: DataFrame): DataFrame = {
		val training = data.filter(!col(dataStage.isTestCol)).withColumn(outputCol, explode(col(outputCol)))
		BOWV.createCodebook(training, outputCol, config.codebook)
	}

	override protected def load(): DataFrame = {
		if (!FileUtils.isValidHDFSPath(file))
			logger.warn("Loading from a local path hampers parallelization")
		spark.read.format("parquet").load(file)
	}

	override protected def save(result: DataFrame): Unit = result.write.format("parquet").mode(SaveMode.Overwrite).save(file)

}

private[pipeline] object FeaturizationStage {

	val defaultOutputCol: String = colName("features")

	private val logger: Logger = Logger.getLogger(getClass)

}