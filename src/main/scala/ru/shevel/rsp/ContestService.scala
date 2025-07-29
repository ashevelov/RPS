package ru.shevel.rsp

import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import cats.syntax.all.*

import scala.util.Random

class ContestService(using Sync[IO]){
  private lazy val contestsRef: Ref[IO, List[Contest]] = Ref.of(List.empty[Contest]).unsafeRunSync()

  private var contestIdCounter:Long = 0

  def getAll: IO[List[Contest]] =
    contestsRef.get

  def getByID(contestId:Long): IO[Contest] =
    for {
      contests <- contestsRef.get
      contest <- IO.fromOption(contests.find(_.id == contestId))(Exception(s"ContestService.getById: contest not found by id:'$contestId'"))
    } yield contest

  def create(opponent: Opponent): IO[Contest] = {
    for {
      contests <- contestsRef.get
      (updatedContests, createdContest) <- findAndUpdateContest(contests, opponent)
      _ <- contestsRef.set(updatedContests)
    } yield createdContest
  }

  private def findAndUpdateContest(
                                    contests: List[Contest],
                                    opponent: Opponent
                                  ): IO[(List[Contest], Contest)] = {
    val (unfilledContests, filledContests) = contests.partition(!_.isFilled)
    unfilledContests.headOption match {
      case Some(contest) =>
        val updatedContest = contest.addOpponent(opponent)
        IO.pure((filledContests :+ updatedContest :++ unfilledContests.tail, updatedContest))

      case None =>
        val newId = synchronized { contestIdCounter += 1; contestIdCounter }
        val newContest = Contest(newId, List(opponent), List.empty)
        IO.pure((contests :+ newContest, newContest))
    }
  }


  def delete(contestId: Long): IO[Unit] = {
    for {
      contests <- contestsRef.get
      _ <- contestsRef.set(contests.filterNot(_.id == contestId))
    } yield IO.unit
  }

  def selectCard(contestId: Long, player_id: Long, card: Card): IO[Unit] = {
    println(s"selectCard: contestId: $contestId, player_id: $player_id, card: $card")
    for {
      contests <- contestsRef.get
      (sought_contest_list, other) = contests.partition(contest => contest.id == contestId && contest.winner.isEmpty)
      updatedContests <- updateContests(sought_contest_list, player_id, card)
      _ <- contestsRef.set(updatedContests ++ other)
    } yield ()
  }

  private def updateContests(sought_contest_list:List[Contest], player_id: Long, card: Card):IO[List[Contest]] = {
    sought_contest_list.map{ contest =>
      for{
        new_sought_contest <- contest.updateCard(player_id, card)
        updatedContest <- checkRound(new_sought_contest)
      } yield {
        updatedContest
      }
    }.sequence
  }

  private def checkRound(contest:Contest): IO[Contest] = {
    if (contest.opponents.forall(_.select != Card.None))
      contest.nextRound()
    else
      IO(contest)
  }

  def joinIfLongWait(opponent: Opponent): IO[Option[Contest]] = {
    contestsRef.get.flatMap { contests =>
      val con = contests.find(c => c.isOld && !c.isFilled)
      val result = con.map(_ => create(opponent))
      result.sequence
    }
  }

  def clean(): IO[Unit] ={
    for {
      contests <- contestsRef.get
      (abandoned, lives) = contests.partition(contest => contest.isAbandoned || (contest.winner != 0 && contest.isOld))
      _ <- contestsRef.set(lives)
      _ <- Game.bot.removeContest(abandoned.map(_.id))
    } yield IO.unit
  }
}

//case class Opponent(player: Player) {
//  var hp: Int = 15
//  var select: Card = None
//  var combo_card: Card = None
//  var combo_factor: Int = 0
//}
//
//case class Contest(id: Long, opponents: List[Opponent])
//
//enum Card:
//  case Rock
//  case Paper
//  case Scissors
//  case None
//
//class ContestService(using Sync[IO]) {
//  private lazy val contestsRef: Ref[IO, List[Contest]] = Ref.of(List.empty[Contest]).unsafeRunSync()
//
//  //выбор карты для следующего рануда
//  def selectCard(contestId: Long, player_id: Long, card: Card): IO[Unit]
//}