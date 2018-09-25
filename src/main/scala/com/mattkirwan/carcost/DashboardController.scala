package com.mattkirwan.carcost

import com.mattkirwan.carcost.Control.using
import com.mattkirwan.carcost.data.Car
import org.json4s.NoTypeHints
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.{Failure, Success, Try}


object Control {
  def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B =
    try {
      f(param)
    } finally {
      param.close()
    }
}

class DashboardController extends ScalatraServlet {

  implicit val formats = Serialization.formats(NoTypeHints)

  val logger =  LoggerFactory.getLogger(getClass)

  def readFileWithTry(filename: String): Try[List[String]] = {
    Try {
      val lines = using(Source.fromFile(filename)) { source =>
        (for (line <- source.getLines) yield line).toList
      }
      lines
    }
  }

  def handleJsonParseException(e: Throwable): Unit = {
    logger.error("error parsing car data json. Reason: {}", e.getMessage)
    halt(500, "Service unavailable")
  }

  get("/:uuid") {

    val dataDirCars = "data/cars/"
    val uuid = params("uuid")

    val filePath = dataDirCars + uuid + ".json"
    val carData = readFileWithTry(filePath)

    val car: Option[Car] = carData match {
      case Success(lines) => {
        try {
          logger.info("Successfully read file {}", filePath)
          Some(parse(lines.mkString).extract[Car])
        } catch {
          case e: Throwable =>
            handleJsonParseException(e)
            None
        }
      }
      case Failure(e) => {
        logger.error("Failed to read file {}", e.getMessage)
        halt(500, "Service unavailable")
      }
    }

    car match {
      case Some(car) =>  html.viewCar(car)
      case None => halt(500)
    }

  }

}
