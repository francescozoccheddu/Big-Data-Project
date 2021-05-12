package image_classifier.utils

import org.apache.spark.ml.linalg.{Vector => MLVector}

object HistogramUtils {

	def computeSparse(data : Seq[Long], codebookSize : Int) : MLVector = {
		import org.apache.spark.ml.linalg.Vectors
		val map = scala.collection.mutable.Map[Int, Double]().withDefaultValue(0)
		for (n <- data) 
			map(n.toInt) += 1
		val entries = map
			.toSeq
			.sortBy(_._1)
			.map { case (i,v)  => (i,v / data.length.toDouble) }
		Vectors.sparse(codebookSize, entries)
	}

	def computeDense(data : TraversableOnce[Long], codebookSize : Int) : MLVector = {
		import org.apache.spark.ml.linalg.Vectors
		val bins = Array.ofDim[Double](codebookSize)
		var sum : Long = 0
		for (n <- data) {
			sum += 1
			bins(n.toInt) += 1
		}
		val dsum = sum.toDouble
		for (i <- 0 until codebookSize) {
			bins(i) /= dsum
		}
		Vectors.dense(bins)
	}

	def compute(data : Seq[Long], codebookSize : Int) : MLVector = {
		val sparseFraction = 0.1
		val minSparseSize = 100
		if (data.length > minSparseSize && data.length < codebookSize.toDouble * sparseFraction)
		computeSparse(data, codebookSize)
		else
		computeDense(data, codebookSize)
	}

}