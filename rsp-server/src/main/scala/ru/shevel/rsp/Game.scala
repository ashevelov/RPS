package ru.shevel.rsp

import cats.effect.*
import com.comcast.ip4s.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import scala.concurrent.duration._

object Game extends IOApp.Simple:
  private val playersRepo = new InMemoryUserRepository()
  private val contestService = new ContestService()  
  
  def getPlayer(player_id: IO[Long]): IO[Player]={
    for{
      id <- player_id
      player <- playersRepo.getById(id)
    }yield player
  }

  def updatePlayer(player: Player): IO[Unit] = {
    for {
      _ <- playersRepo.update(player)
    } yield ()
  }
  
  def findContest(player_id: IO[Long]):IO[Contest] = {
    for{
      player <- getPlayer(player_id)
      contest <- contestService.create(Opponent(player))
    }yield contest
  }

  def getContest(contest_id: IO[Long]): IO[Contest] = {
    for {
      id <- contest_id
      contest <- contestService.getByID(id)
    } yield contest
  }

  def selectCard(contest_id: IO[Long], player_id: IO[Long], card: IO[Card]): IO[Unit] = {
    for {
      con_id <- contest_id
      p_id <- player_id
      c <- card
      _ <- contestService.selectCard(con_id, p_id, c)
    } yield IO.unit
  }

  def printContests(): IO[Unit] = {
    for {
      list <- contestService.getAll
    } yield IO {
      println("Contests: count " + list.size)
    }
  }

  // 4: Launch the server in the application loop
  val run: IO[Unit] = for {
//    _ <- playersRepo.create(Player(0, "Hel", 3, 0))
//    _ <- playersRepo.create(Player(1, "Krakmal", 5, 0))
//    contest_r1 <- findContest(IO(1))
//    contest_r1 <- findContest(IO(0))
//    _ <- IO(println(s"contest_p1: ${contest_r1.asJson}"))
//
//    _ <- selectCard(IO(contest_r1.id), IO(1), IO(Card.Rock))
//    _ <- selectCard(IO(contest_r1.id), IO(0), IO(Card.Paper))
//    contest_r2 <- getContest(IO(contest_r1.id))
//    _ <- IO(println(s"contest_r2: ${contest_r2.asJson}"))
//
//    _ <- selectCard(IO(contest_r1.id), IO(1), IO(Card.Rock))
//    _ <- selectCard(IO(contest_r1.id), IO(0), IO(Card.Scissors))
//    contest_r3 <- getContest(IO(contest_r1.id))
//    _ <- IO(println(s"contest_r3: ${contest_r3.asJson}"))
//
//    _ <- selectCard(IO(contest_r1.id), IO(1), IO(Card.Rock))
//    _ <- selectCard(IO(contest_r1.id), IO(0), IO(Card.Rock))
//    contest_r4 <- getContest(IO(contest_r1.id))
//    _ <- IO(println(s"contest_r4: ${contest_r4.asJson}"))
//
//    _ <- selectCard(IO(contest_r1.id), IO(1), IO(Card.Rock))
//    _ <- selectCard(IO(contest_r1.id), IO(0), IO(Card.Scissors))
//    contest_r5 <- getContest(IO(contest_r1.id))
//    _ <- IO(println(s"contest_p1_r5: ${contest_r5.asJson}"))

//    allUsers <- playersRepo.getAll
//    _ <- IO(println(s"All users: $allUsers"))
    _ <- (IO(printContests()).start*> IO.sleep(2.seconds)).foreverM.void
  } yield ()



