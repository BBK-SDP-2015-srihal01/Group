import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import Messages.RenderScene
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

object Trace {

  val AntiAliasingFactor = 4
  val Width = 800
  val Height = 600

  private val rayCount = new AtomicInteger(0)
  private val hitCount = new AtomicInteger(0)
  private val lightCount = new AtomicInteger(0)
  private val darkCount = new AtomicInteger(0)
  
  def incrementRayCount = rayCount.getAndIncrement
  def incrementHitCount = hitCount.getAndIncrement
  def incrementLightCount = lightCount.getAndIncrement
  def incrementDarkCount = darkCount.getAndIncrement

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("usage: scala Trace input.dat output.png")
      System.exit(-1)
    }

    val (infile, outfile) = (args(0), args(1))
    val scene = Scene.fromFile(infile)
    render(scene, outfile, Width, Height)

  }
  
  import Messages._
  import scala.language.postfixOps

  def render(scene: Scene, outfile: String, width: Int, height: Int) = {
    
    val image = new Image(width, height)
    val system = ActorSystem("TracerSystem")
    val coordinator = system.actorOf(Props(new Coordinator(image, outfile)), "coordinator")
    implicit val timeout = Timeout(30 seconds)
    val future = coordinator ? RenderScene(scene, width, height)
    val result = Await.result(future, timeout.duration)
    
    println(f"Rays Cast  = ${rayCount.get()}%,d")
    println(f"Rays Hast  = ${hitCount.get()}%,d")
    println(f"Light      = ${lightCount.get()}%,d")
    println(f"Dark       = ${darkCount.get()}%,d")
    println(result)
    system.shutdown()
    
  }
}