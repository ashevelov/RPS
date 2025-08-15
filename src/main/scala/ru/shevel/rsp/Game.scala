package ru.shevel.rsp

import cats.effect.*

import scala.concurrent.duration.*

object Game:
  private val playersRepo = new InMemoryUserRepository()
  private val contestService = new ContestService()
  val predictor = new RPSPredictor()
  val bot = new Bot()
  
  def getPlayer(player_id: IO[Long]): IO[Player]={
    for{
      id <- player_id
      player <- playersRepo.getById(id)
    }yield player
  }

  def setPlayerName(player_id: IO[Long], player_name: IO[String]): IO[Unit] = {
    for {
      id <- player_id
      name <- player_name
      player <- playersRepo.getById(id)
      _ <- playersRepo.update(player.copy(name = name))
    } yield ()
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

  def surrender(contest_id: IO[Long], player_id: IO[Long]): IO[Unit] = {
    for {
      con_id <- contest_id
      p_id <- player_id
      _ <- contestService.surrender(con_id, p_id)
    } yield IO.unit
  }

  def joinToOldContest(player_id: IO[Long]):IO[Option[Contest]] = {
    for{
      player <- getPlayer(player_id)
      contest <- contestService.joinIfLongWait(Opponent(player))
    }yield contest
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
    _ <- playersRepo.create(Player(
                                    id = Bot.id, 
                                    name = "Bot:CustomAI", 
                                    win = 0, 
                                    lose = 0
                                  ))
    _ <- (contestService.clean().start *> bot.selectCards().start *> bot.joinToContest().start *> IO.sleep(2.seconds)).foreverM.void
  } yield ()



