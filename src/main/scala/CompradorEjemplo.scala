import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Flow, Source}

import scala.util.Random

/**
 * Created by abelmeos on 2016/09/29.
 */


object InputComprador {

  def random():InputComprador = {
    InputComprador( s"Nombre${Random.nextInt(1000)} Apellido${Random.nextInt(1000)}")
  }

}

case class InputComprador(nombre:String)
case class OutputComprador(primeroNombre:String, apellido:String)

object CompradorEjemplo extends  App{

  implicit val actorSystem = ActorSystem()
  import actorSystem.dispatcher
  implicit val flowMaterializer = ActorFlowMaterializer()

  val compradores = Source((1 to 100).map(x => InputComprador.random()))

  val normalize = Flow[InputComprador].map(c => c.nombre.split(" ").toList).collect{
    case primerNombre::apellido::Nil => OutputComprador(primerNombre,apellido)
  }

  val writeCompradores = Sink.foreach[OutputComprador]{
    comprador => println(comprador)
  }

  compradores.via(normalize).runWith(writeCompradores).andThen{

    case _ =>
      actorSystem.shutdown()
      actorSystem.awaitTermination()

  }

}
