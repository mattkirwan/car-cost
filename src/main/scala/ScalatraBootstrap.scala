import com.mattkirwan.carcost.{DashboardController, HomeController}
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new HomeController, "/*")
    context.mount(new DashboardController, "/dashboard")
  }
}
