package ru.shevel.rsp

import cats.effect.{IO, Ref}
import cats.effect.kernel.Sync
import cats.syntax.all.*

import scala.util.Random
import cats.effect.unsafe.implicits.global

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
    println(s"selectCard: contestId: $contestId, player_id: $player_id, card: $player_id")
    for {
      contests <- contestsRef.get
      updatedContests <- IO {
        contests.map { contest =>
          if (contest.id == contestId && contest.winner.isEmpty) {
            val updatedOpponents = contest.opponents.map { opponent =>
              if (opponent.player.id == player_id) {
                opponent.copy(select = card)
              } else {
                opponent
              }
            }
            checkRound(contest.copy(opponents = updatedOpponents))
          } else {
            contest
          }
        }
      }
      _ <- contestsRef.set(updatedContests)
    } yield ()
  }

  private def checkRound(contest:Contest): Contest = {
    if (contest.opponents.forall(_.select != Card.None)) {
      contest.nextRound()
    } else {
      contest
    }
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