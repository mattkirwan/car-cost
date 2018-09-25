package com.mattkirwan.carcost

import java.io.{BufferedWriter, File, FileWriter}
import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.scalatra.{BadRequest, ScalatraServlet}
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

import scala.io.Source
import scala.util.{Failure, Success, Try}


case class CreateCarValidationForm(car: String, pricePaid: Int)
case class LoadCarValidationForm(uuid: String)

case class Car(car: String, pricePaid: Int)

object Control {
  def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B =
    try {
      println("executing second paramater which is a function and calling it")
      f(param)
    } finally {
      println("finally calling close on the firs paramater - an entity with a close method")
      param.close()
    }
}

import Control._

class HomeController extends ScalatraServlet with FormSupport with I18nSupport {

  implicit val formats = Serialization.formats(NoTypeHints)

  val logger =  LoggerFactory.getLogger(getClass)

  val createForm: MappingValueType[CreateCarValidationForm] = mapping(
    "car" -> label("Car Make/Model", text(required, maxlength(100))),
    "pricePaid" -> label("Price Paid", number(required))
  )(CreateCarValidationForm.apply)

  val loadForm: MappingValueType[LoadCarValidationForm] = mapping(
    "uuid" -> label("UUID", text(required))
  )(LoadCarValidationForm)

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

  get("/") {
    html.home()
  }

  post("/create-car") {
    validate(createForm)(
      errors => BadRequest(html.home()),
      createForm => {
        val uuid = createCarFile(createForm)
        redirect("/dashboard/" + uuid)
      }
    )
  }

  post("/load-car") {
    validate(loadForm)(
      errors => BadRequest(html.home()),
      loadForm => redirect("/dashboard/" + loadForm.uuid)
    )
  }

  get("/dashboard/:uuid") {

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

  def createCarFile(formData: CreateCarValidationForm): String = {
    val uuid = UUID.randomUUID().toString

    logger.info("Creating car data file {}", uuid)

    val json = (("car" -> formData.car)  ~ ("pricePaid" -> formData.pricePaid))
    log(println(write(json)).toString)
    val file = new File("data/cars/" + uuid + ".json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(write(json))
    bw.close
    uuid

  }

}




