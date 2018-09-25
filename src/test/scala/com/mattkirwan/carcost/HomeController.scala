package com.mattkirwan.carcost

import org.scalatra._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

case class CreateCarValidationForm(text: String)

class HomeController extends ScalatraServlet with FormSupport with I18nSupport {

  val form = mapping()

  get("/") {
    h
  }

  post("/") {

  }

}