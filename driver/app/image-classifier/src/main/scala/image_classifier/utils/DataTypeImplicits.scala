package image_classifier.utils

import org.apache.spark.sql.types.{DataType, StructType, StructField}

private[image_classifier] object DataTypeImplicits {

	implicit final class DataTypeExtension(dataType: DataType) {

		def getField(name: String): Option[StructField] =
			try {
				val struct = dataType.asInstanceOf[StructType]
				Some(struct(name))
			}
			catch {
				case _: Exception => None
			}

		def requireNoField(name: String) =
			require(dataType.getField(name).isEmpty)

		def requireField(name: String) = {
			require(dataType.getField(name).isDefined)
		}

		def requireField(name: String, requiredDataType: DataType) = {
			val field = dataType.getField(name)
			require(field.isDefined && field.get.dataType == requiredDataType)
		}

	}

	implicit final class StructTypeExtension(structType: StructType) {

		def appendField(name: String, dataType: DataType): StructType = {
			val oldField = structType.getField(name)
			require(oldField.forall(_.dataType == dataType))
			if (oldField.isEmpty)
				StructType(structType.fields :+ StructField(name, dataType))
			else
				structType
		}

	}

}