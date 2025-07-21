package ru.shevel.rsp

import cats.effect.IO

import java.time.LocalDateTime

case class Contest(id:Long, opponents: List[Opponent], roundLog: List[RoundLog], winner:Long = 0){
  def isFilled: Boolean = opponents.size > 1

  def nextRound(): Contest = {
    val (newRoundLog, newOpponents) = determineRoundOutcome(opponents.head, opponents.last)

    
    val loser = newOpponents.find(_.hp <= 0).map(_.player)
    val winner = if(loser.isDefined){
      val p = newOpponents.find(_.hp > 0).map(_.player)
      p.foreach(player => Game.updatePlayer(player.copy(win = player.win + 1)))
      loser.foreach(player => Game.updatePlayer(player.copy(lose = player.lose + 1)))
      p.map(_.id).getOrElse(0L)
    } else 0L
    
    this.copy(opponents = newOpponents, roundLog = newRoundLog :: roundLog, winner = winner)
  }

  private def determineRoundOutcome(opponent1:Opponent, opponent2:Opponent):(RoundLog, List[Opponent]) = {
    if (opponent1.select.tie(opponent2.select)) {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.tie :: opponent2.tie :: Nil
        ),
        List(
          opponent1.reset(0), 
          opponent2.reset(0)
        )
      )
    } else if (opponent1.select.beats(opponent2.select)) {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.winner :: opponent2.loser :: Nil
        ),
        List(
          opponent1.reset(0), 
          opponent2.reset(opponent1.damage())
        )
      )
    } else {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.loser :: opponent2.winner :: Nil
        ),
        List(
          opponent1.reset(opponent2.damage()),
          opponent2.reset(0)
        )
      )
    }
  }

  def addOpponent(opponent: Opponent): Contest =
    this.copy(opponents = opponents :+ opponent)

}
