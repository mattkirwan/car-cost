package com.mattkirwan.carcost

import org.scalatra.{BadRequest, ScalatraServlet}
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

case class CreateCarValidationForm(car: String, pricePaid: Int)
case class LoadCarValidationForm(uuid: String)

class HomeController extends ScalatraServlet with FormSupport with I18nSupport {

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
        bleepBleep
        html.result(createForm, "hs2ya75q")
      }
    )
  }

  post("/load-car") {
    validate(loadForm)(
      errors => BadRequest(html.home()),
      loadForm => html.viewCar(loadForm)
    )
  }

  def bleepBleep(): Unit = {
    log("Storing car data...")
  }

}
