package com.mattkirwan.carcost

import org.scalatra.test.scalatest._

class SiteServletTests extends ScalatraFunSuite {

  addServlet(classOf[SiteServlet], "/*")

  test("GET / on SiteServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
