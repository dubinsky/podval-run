package org.podval.tools.run

import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

final class RunServlet extends HttpServlet {

  @throws[java.io.IOException]
  @throws[ServletException]
  override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    response.setContentType("text/plain")
    response.getWriter.println(Run.get.fold(_.toString, _.toString))
  }
}
