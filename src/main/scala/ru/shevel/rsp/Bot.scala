package ru.shevel.rsp

import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import cats.syntax.all.*

object Bot {
  val id: Long = -742345782L
}

class Bot(using Sync[IO]){
  private val id_IO:IO[Long] = IO(Bot.id)

  private lazy val contestIDsRef: Ref[IO, List[Long]] = Ref.of(List.empty[Long]).unsafeRunSync()

  //добавляет ид контеста в котором участвует в список contestIDs
  private def addNewContest(contest_ID: Long):IO[Unit] = {
    println("addNewContest " + contest_ID)
    for{
      contests <- contestIDsRef.get
      _ <- contestIDsRef.set(contest_ID ::contests)
    } yield IO.unit
  }

  //удаляет ид контеста в котором больше не участвует из списка contestIDs
  private def removeContest(contest_ID: Long):IO[Unit] =
    for {
      contests <- contestIDsRef.get
      _ <- contestIDsRef.set(contests.filterNot(_== contest_ID))
    } yield IO.unit

  //удаляет ид контеста в котором больше не участвует из списка contestIDs
  def removeContest(ids: List[Long]):IO[Unit] =
    for {
      contests <- contestIDsRef.get
      _ <- contestIDsRef.set(contests.filterNot(id => ids.contains(id)))
    } yield IO.unit

  def selectCards(): IO[Unit] = {
    println("selectCards: ")
    for {
      contestsIDs <- contestIDsRef.get
      contests <- contestsIDs.traverse(i => Game.getContest(IO(i)))
      _ <- IO.whenA(contests.nonEmpty) {
        contests.traverse_ { contest =>
          val opponents = contest.opponents
          val (bot, opponent) = opponents.partition(_.player.id == Bot.id) match {
            case (List(b), List(o)) => (b, o)
            case _ => throw new Exception("Некорректное количество оппонентов")
          }
          if(bot.select == Card.None){
            val prediction = Game.predictor.predict(
              bot.combo_card, bot.combo_factor,
              opponent.combo_card, opponent.combo_factor
            )

            Game.selectCard(
              IO(contest.id),
              id_IO,
              prediction.map(_.stronger)
            )
          } else IO.unit
        }
      }
    } yield ()
  }

  def joinToContest(): IO[Unit] = {
    println("joinToContest: ")
    for {
      some_contest <- Game.joinToOldContest(id_IO)
      _ <- some_contest.map(c => addNewContest(c.id)).getOrElse(IO.unit)
    } yield IO.unit
  }

  def check():IO[Unit] ={
    for{
      _ <- joinToContest()
      _ <- selectCards()
    } yield IO.unit
  }
}
