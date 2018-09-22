package com.mattkirwan.carcost

import org.scalatra._

class SiteServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

}
