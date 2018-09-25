package com.mattkirwan.carcost

import java.io.{BufferedWriter, File, FileWriter}
import java.util.UUID

import org.json4s.JsonDSL._
import org.json4s.jackson.Serialization.{write}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.scalatra.{BadRequest, ScalatraServlet}
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport
import org.slf4j.LoggerFactory

import forms.validation.CreateCarValidationForm
import forms.validation.LoadCarValidationForm

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




