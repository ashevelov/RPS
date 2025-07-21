package ru.shevel.rsp

// dependency: "org.http4s" %% "http4s-ember-server" % "1.0.0-M21"
// dependency: "org.http4s" %% "http4s-dsl" % "1.0.0-M21"
// dependency: "ch.qos.logback" % "logback-classic" % "1.2.3",

import cats.effect.*
import com.comcast.ip4s.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.util.Try

object Http extends IOApp.Simple:
  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val gameService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@GET -> Root / "getPlayer" =>
      val player_id = IO.fromOption(req.params("player_id").toLongOption)(Exception("player_id must be a number"))
      Game.getPlayer(player_id).flatMap(player => Ok(player.asJson))
      
    case req@GET -> Root / "findContest" =>
      val player_id = IO.fromOption(req.params("player_id").toLongOption)(Exception("player_id must be a number"))
      Game.findContest(player_id).flatMap(contest => Ok(contest.asJson))

    case req@GET -> Root / "getContest" =>
      val contest_id = IO.fromOption(req.params("contest_id").toLongOption)(Exception("contest_id must be a number"))
      Game.getContest(contest_id).flatMap(contest => Ok(contest.asJson))

    case req@GET -> Root / "selectCard" =>
      val contest_id = IO.fromOption(req.params("contest_id").toLongOption)(Exception("contest_id must be a number"))
      val player_id = IO.fromOption(req.params("player_id").toLongOption)(Exception("player_id must be a number"))
      val card = IO.fromTry(Try(Card.valueOf(req.params("card"))))
      Game.selectCard(contest_id, player_id, card).flatMap(contest => Ok("successful"))
      
    case GET -> Root / "map" =>
      Ok("World.current.regions.asJson")
  }

  // 2: Allocate a route to the service in the router
  private val httpApp = Router("/api" -> gameService).orNotFound

  // 3: Build the actual server
  val server: Resource[IO, Server] = EmberServerBuilder
    .default[IO]
//    .withHost(ipv4"192.168.2.17")
    .withHost(ipv4"192.168.1.87")
    .withPort(port"8080")
    .withHttpApp(httpApp)
    .build

  // 4: Launch the server in the application loop
  val run: IO[Unit] = for {
    _ <- server.allocated
  } yield ()