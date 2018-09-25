package com.mattkirwan.carcost

import java.util.UUID

import org.json4s.JsonDSL._
import org.json4s.jackson.Serialization.write
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization
import org.scalatra.{BadRequest, ScalatraServlet}
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport
import org.slf4j.{Logger, LoggerFactory}
import com.mattkirwan.carcost.forms.validation.CreateCarValidationForm
import com.mattkirwan.carcost.forms.validation.LoadCarValidationForm
import com.mattkirwan.carcost.helpers.FileHelpers.writeFileWithTry

import scala.util.{Failure, Success}

class HomeController extends ScalatraServlet with FormSupport with I18nSupport {

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  val logger: Logger =  LoggerFactory.getLogger(getClass)

  val createForm: MappingValueType[CreateCarValidationForm] = mapping(
    "car" -> label("Car Make/Model", text(required, maxlength(100))),
    "pricePaid" -> label("Price Paid", number(required))
  )(CreateCarValidationForm.apply)

  val loadForm: MappingValueType[LoadCarValidationForm] = mapping(
    "uuid" -> label("UUID", text(required))
  )(LoadCarValidationForm)

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

  def createCarFile(formData: CreateCarValidationForm): String = {
    val uuid = UUID.randomUUID().toString
    logger.info("Creating file {}", uuid)

    val json = ("car" -> formData.car)  ~ ("pricePaid" -> formData.pricePaid)

    val writeFile = writeFileWithTry("data/cars/" + uuid + ".json", write(json))

    val car: Option[String] = writeFile match {
      case Success(filename) => {
        logger.info("Successful write of file {}", filename)
        Some(filename)
      }
      case Failure(e) => {
        logger.error("Failed to write file {}", e.getMessage)
        None
      }
    }

    car match {
      case Some(filename) => uuid
      case None => halt(500)
    }
  }

}




