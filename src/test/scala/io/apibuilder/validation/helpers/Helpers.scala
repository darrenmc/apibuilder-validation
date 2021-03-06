package io.apibuilder.validation.helpers

import io.apibuilder.spec.v0.models.Service
import io.apibuilder.spec.v0.models.json._
import io.apibuilder.validation.{ApiBuilderService, MultiService}
import play.api.libs.json.Json

trait Helpers {

  def readFile(filename: String): String = {
    scala.io.Source.fromFile("src/test/resources/" + filename, "UTF-8").getLines.mkString("\n")
  }

  def loadService(filename: String): ApiBuilderService = {
    ApiBuilderService(
      Json.parse(readFile(filename)).as[Service]
    )
  }

  def loadMultiService(files: Seq[String]): MultiService = {
    MultiService(files.map(loadService))
  }

  lazy val apibuilderMultiService: MultiService = {
    loadMultiService(
      Seq(
        "apibuilder-explicit-validation-core-service.json",
        "apibuilder-explicit-validation-service.json"
      )
    )
  }

  lazy val flowMultiService: MultiService = {
    loadMultiService(
      Seq(
        "flow-api-service.json",
        "flow-api-internal-service.json"
      )
    )
  }

  def rightOrErrors[K,V](f: Either[K, V]): V = {
    f match {
      case Left(bad) => sys.error(s"Expected valid value but got: $bad")
      case Right(v) => v
    }
  }

}
