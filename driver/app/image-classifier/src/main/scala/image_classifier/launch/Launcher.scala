package image_classifier.launch

object Launcher {
	import org.apache.log4j.{BasicConfigurator, Logger}

	BasicConfigurator.configure()

	import image_classifier.configuration.Config

	private val logger = Logger.getLogger(Launcher.getClass)

	def run(configFile: String): Unit = {
		import java.nio.file.Paths
		val configDir = Paths.get(configFile).getParent.toString
		val config = Config.fromFile(configFile)
		run(config, configDir)
	}

	def run(config: Config, workingDir: String): Unit = {
		import image_classifier.pipeline.Pipeline
		import image_classifier.utils.SparkInstance
		import org.apache.log4j.Level
		Logger.getRootLogger.setLevel(Level.INFO)
		Logger.getLogger("org").setLevel(Level.ERROR)
		SparkInstance.execute(Pipeline.run(config, workingDir)(_))
	}

}